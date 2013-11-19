/*
 * Copyright 2013 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.form;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.exception.FormBuildingException;
import piecework.exception.InternalFormException;
import piecework.exception.RemoteFormException;
import piecework.model.*;
import piecework.security.DataFilterService;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ConstraintUtil;
import piecework.util.ProcessInstanceUtility;
import piecework.validation.FormValidation;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class FormFactory {

    private static final Logger LOG = Logger.getLogger(FormFactory.class);

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    Versions versions;

    private final PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

    public Form form(FormRequest request, ActionType actionType, Entity principal, MediaType mediaType, FormValidation validation, Explanation explanation, boolean anonymous) throws FormBuildingException {
        ViewContext version = versions.getVersion1();
        Activity activity = request.getActivity();

        String formInstanceId = request.getRequestId();
        String processDefinitionKey = request.getProcessDefinitionKey();
        String processInstanceId = request.getProcessInstanceId();
        ProcessInstance instance = request.getInstance();
        Task task = request.getTask();
        boolean unmodifiable = task != null && !task.canEdit(principal);
        boolean revertToDefaultUI = false;

        Action action = activity.action(actionType);

        // If there is no action defined, then revert to CREATE
        if (action == null) {
            action = activity.action(ActionType.CREATE);
            // If the action type was VIEW then revert to the default ui, use create as the action, but make it unmodifiable
            if (actionType == ActionType.VIEW) {
                revertToDefaultUI = true;
                unmodifiable = true;
            }
        }

        if (action == null)
            throw new FormBuildingException("Action is null for this activity and type " + actionType);

        URI uri = safeUri(action, task);
        boolean external = isExternal(uri);

        if (mediaType.equals(MediaType.TEXT_HTML_TYPE) && action.getStrategy() == DataInjectionStrategy.INCLUDE_SCRIPT && !revertToDefaultUI) {
            if (external)
                throw new RemoteFormException(uri);
            if (action.getLocation() != null) {
                Form form = new Form.Builder().processDefinitionKey(processDefinitionKey).formInstanceId(formInstanceId).build(version);
                throw new InternalFormException(form, action.getLocation());
            }
        }

        Map<String, Field> fieldMap = activity.getFieldMap();
        Map<String, List<Value>> data = dataFilterService.filter(fieldMap, instance, task, principal, false);
        Map<String, Field> decoratedFieldMap = decorate(fieldMap, processDefinitionKey, processInstanceId, data, version, unmodifiable);

        if (validation != null) {
            Map<String, List<Value>> validationData = validation.getData();
            if (validationData != null) {
                for (Map.Entry<String, List<Value>> entry : validationData.entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
            }
        }

        Container container = action.getContainer();
        String title = container.getTitle();

        if (StringUtils.isNotEmpty(title) && title.contains("{{")) {
            title = ProcessInstanceUtility.template(title, data);
        }

        Form.Builder builder = new Form.Builder()
                .formInstanceId(formInstanceId)
                .processDefinitionKey(processDefinitionKey)
                .taskSubresources(processDefinitionKey, task, version)
                .container(new Container.Builder(container, passthroughSanitizer, decoratedFieldMap).title(title).readonly(unmodifiable).build())
                .data(data)
                .messages(request.getMessages())
                .explanation(explanation);

        if (anonymous)
            builder.anonymous();

        ActivityUsageType usageType = activity.getUsageType() != null ? activity.getUsageType() : ActivityUsageType.USER_FORM;
        switch (usageType) {
            case MULTI_PAGE:
                builder.layout("multipage");
                break;
            case MULTI_STEP:
                builder.layout("multistep");
                break;
            default:
                builder.layout("normal");
                break;
        }

        if (instance != null)
            builder.instance(instance, version);

        return builder.build(version);
    }

    private static void addConfirmationNumber(Field.Builder fieldBuilder, String confirmationNumber) {
        String defaultValue = fieldBuilder.getDefaultValue();
        fieldBuilder.defaultValue(defaultValue.replaceAll("\\{\\{ConfirmationNumber\\}\\}", confirmationNumber));
    }

    private static void addStateOptions(Field.Builder fieldBuilder) {
        fieldBuilder.option(new Option.Builder().value("").name("").build())
                .option(new Option.Builder().value("AL").name("Alabama").build())
                .option(new Option.Builder().value("AK").name("Alaska").build())
                .option(new Option.Builder().value("AZ").name("Arizona").build())
                .option(new Option.Builder().value("AR").name("Arkansas").build())
                .option(new Option.Builder().value("CA").name("California").build())
                .option(new Option.Builder().value("CO").name("Colorado").build())
                .option(new Option.Builder().value("CT").name("Connecticut").build())
                .option(new Option.Builder().value("DE").name("Delaware").build())
                .option(new Option.Builder().value("DC").name("District Of Columbia").build())
                .option(new Option.Builder().value("FL").name("Florida").build())
                .option(new Option.Builder().value("GA").name("Georgia").build())
                .option(new Option.Builder().value("HI").name("Hawaii").build())
                .option(new Option.Builder().value("ID").name("Idaho").build())
                .option(new Option.Builder().value("IL").name("Illinois").build())
                .option(new Option.Builder().value("IN").name("Indiana").build())
                .option(new Option.Builder().value("IA").name("Iowa").build())
                .option(new Option.Builder().value("KS").name("Kansas").build())
                .option(new Option.Builder().value("KY").name("Kentucky").build())
                .option(new Option.Builder().value("LA").name("Louisiana").build())
                .option(new Option.Builder().value("ME").name("Maine").build())
                .option(new Option.Builder().value("MD").name("Maryland").build())
                .option(new Option.Builder().value("MA").name("Massachusetts").build())
                .option(new Option.Builder().value("MI").name("Michigan").build())
                .option(new Option.Builder().value("MN").name("Minnesota").build())
                .option(new Option.Builder().value("MS").name("Mississippi").build())
                .option(new Option.Builder().value("MO").name("Missouri").build())
                .option(new Option.Builder().value("MT").name("Montana").build())
                .option(new Option.Builder().value("NE").name("Nebraska").build())
                .option(new Option.Builder().value("NV").name("Nevada").build())
                .option(new Option.Builder().value("NH").name("New Hampshire").build())
                .option(new Option.Builder().value("NJ").name("New Jersey").build())
                .option(new Option.Builder().value("NM").name("New Mexico").build())
                .option(new Option.Builder().value("NY").name("New York").build())
                .option(new Option.Builder().value("NC").name("North Carolina").build())
                .option(new Option.Builder().value("ND").name("North Dakota").build())
                .option(new Option.Builder().value("OH").name("Ohio").build())
                .option(new Option.Builder().value("OK").name("Oklahoma").build())
                .option(new Option.Builder().value("OR").name("Oregon").build())
                .option(new Option.Builder().value("PA").name("Pennsylvania").build())
                .option(new Option.Builder().value("RI").name("Rhode Island").build())
                .option(new Option.Builder().value("SC").name("South Carolina").build())
                .option(new Option.Builder().value("SD").name("South Dakota").build())
                .option(new Option.Builder().value("TN").name("Tennessee").build())
                .option(new Option.Builder().value("TX").name("Texas").build())
                .option(new Option.Builder().value("UT").name("Utah").build())
                .option(new Option.Builder().value("VT").name("Vermont").build())
                .option(new Option.Builder().value("VA").name("Virginia").build())
                .option(new Option.Builder().value("WA").name("Washington").build())
                .option(new Option.Builder().value("WV").name("West Virginia").build())
                .option(new Option.Builder().value("WI").name("Wisconsin").build())
                .option(new Option.Builder().value("WY").name("Wyoming").build());
    }

    private Map<String, Field> decorate(Map<String, Field> fieldMap, String processDefinitionKey, String processInstanceId, Map<String, List<Value>> data, ViewContext version, boolean unmodifiable) {
        Map<String, Field> decoratedFieldMap = new HashMap<String, Field>();
        if (fieldMap != null) {
            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                String fieldName = entry.getKey();
                Field field = entry.getValue();

                Field.Builder fieldBuilder = new Field.Builder(field, passthroughSanitizer)
                        .processDefinitionKey(processDefinitionKey)
                        .processInstanceId(processInstanceId);

                List<Constraint> constraints = field.getConstraints();
                if (constraints != null) {
                    if (!ConstraintUtil.checkAll(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN, null, data, constraints))
                        fieldBuilder.invisible();
                    if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_STATE, constraints))
                        addStateOptions(fieldBuilder);
                    if (StringUtils.isNotEmpty(processInstanceId) && ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER, constraints))
                        addConfirmationNumber(fieldBuilder, processInstanceId);
                }

                if (unmodifiable)
                    fieldBuilder.readonly();

                decoratedFieldMap.put(fieldName, fieldBuilder.build(version));
            }
        }
        return decoratedFieldMap;
    }

    private boolean isExternal(URI uri) {

        if (uri != null) {
            String scheme = uri.getScheme();
            return StringUtils.isNotEmpty(scheme) && (scheme.equals("http") || scheme.equals("https"));
        }

        return false;
    }

    private URI safeUri(Action action, Task task) {
        URI uri = null;
        try {
            uri = action.getUri(task);
        } catch (IllegalArgumentException iae) {
            LOG.error("Failed to convert location into uri:" + action.getLocation(), iae);
        }
        return uri;
    }

}
