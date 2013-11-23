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

    public Form form(FormRequest request, Entity principal, boolean anonymous) throws FormBuildingException {
        return form(request, request.getAction(), principal, MediaType.TEXT_HTML_TYPE, null, null, anonymous);
    }

    public Form form(FormRequest request, ActionType actionType, Entity principal, MediaType mediaType, FormValidation validation, Explanation explanation, boolean anonymous) throws FormBuildingException {
        ViewContext version = versions.getVersion1();
        Activity activity = request.getActivity();

        String formInstanceId = request.getRequestId();
        String processDefinitionKey = request.getProcessDefinitionKey();
        String processInstanceId = request.getProcessInstanceId();
        ProcessInstance instance = request.getInstance();
        Task task = request.getTask();
        boolean unmodifiable = task != null && !task.canEdit(principal);

        if (unmodifiable && actionType != null && actionType == ActionType.CREATE)
            actionType = ActionType.VIEW;

        Form.Builder builder = new Form.Builder()
                .formInstanceId(formInstanceId)
                .processDefinitionKey(processDefinitionKey);

        if (activity != null) {
            Action action = action(builder, activity, task, actionType, mediaType, version, unmodifiable);
            Map<String, Field> fieldMap = activity.getFieldMap();
            Map<String, List<Value>> data = dataFilterService.filter(fieldMap, instance, task, principal, validation);
            Map<String, Field> decoratedFieldMap = decorate(fieldMap, processDefinitionKey, processInstanceId, data, version, unmodifiable);

            container(builder, data, decoratedFieldMap, action);
            builder.data(data);
        }

        builder.taskSubresources(processDefinitionKey, task, version)
                .instance(instance, version)
                .messages(request.getMessages())
                .explanation(explanation)
                .anonymous(anonymous);

        return builder.build(version);
    }

    private void container(Form.Builder builder, Map<String, List<Value>> data, Map<String, Field> decoratedFieldMap, Action action) {
        Container container = null;
        if (action != null) {
            container = action.getContainer();
            String title = container.getTitle();
            if (StringUtils.isNotEmpty(title) && title.contains("{{"))
                title = ProcessInstanceUtility.template(title, data);

            // Rebuild the container with the decorated field map
            container = new Container.Builder(container, passthroughSanitizer, decoratedFieldMap).title(title).readonly(builder.isReadonly()).build();
        }
        builder.container(container);
    }

    private Action action(Form.Builder builder, Activity activity, Task task, ActionType actionType, MediaType mediaType, ViewContext version, boolean unmodifiable) throws FormBuildingException {
        // Can't do any of this processing without an activity
        if (activity == null)
            return null;

        Action action = activity.action(actionType);
        boolean revertToDefaultUI = false;

        // If there is no action defined, then revert to CREATE_TASK
        if (action == null) {
            action = activity.action(ActionType.CREATE);
            // If the action type was VIEW then revert to the default ui, use create as the action, but make it unmodifiable
            if (actionType == ActionType.VIEW) {
                revertToDefaultUI = true;
                builder.readonly();
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
                Form form = builder.build(version);
                throw new InternalFormException(form, action.getLocation());
            }
        }

        // Tacking this on at the end - could be somewhere better
        layout(builder, activity);

        return action;
    }

    private static void addConfirmationNumber(Field.Builder fieldBuilder, String confirmationNumber) {
        String defaultValue = fieldBuilder.getDefaultValue();
        fieldBuilder.defaultValue(defaultValue.replaceAll("\\{\\{ConfirmationNumber\\}\\}", confirmationNumber));
    }

    private static void addStateOptions(Field.Builder fieldBuilder) {
        fieldBuilder.option(new Option.Builder().value("").label("").build())
                .option(new Option.Builder().value("AL").label("Alabama").build())
                .option(new Option.Builder().value("AK").label("Alaska").build())
                .option(new Option.Builder().value("AZ").label("Arizona").build())
                .option(new Option.Builder().value("AR").label("Arkansas").build())
                .option(new Option.Builder().value("CA").label("California").build())
                .option(new Option.Builder().value("CO").label("Colorado").build())
                .option(new Option.Builder().value("CT").label("Connecticut").build())
                .option(new Option.Builder().value("DE").label("Delaware").build())
                .option(new Option.Builder().value("DC").label("District Of Columbia").build())
                .option(new Option.Builder().value("FL").label("Florida").build())
                .option(new Option.Builder().value("GA").label("Georgia").build())
                .option(new Option.Builder().value("HI").label("Hawaii").build())
                .option(new Option.Builder().value("ID").label("Idaho").build())
                .option(new Option.Builder().value("IL").label("Illinois").build())
                .option(new Option.Builder().value("IN").label("Indiana").build())
                .option(new Option.Builder().value("IA").label("Iowa").build())
                .option(new Option.Builder().value("KS").label("Kansas").build())
                .option(new Option.Builder().value("KY").label("Kentucky").build())
                .option(new Option.Builder().value("LA").label("Louisiana").build())
                .option(new Option.Builder().value("ME").label("Maine").build())
                .option(new Option.Builder().value("MD").label("Maryland").build())
                .option(new Option.Builder().value("MA").label("Massachusetts").build())
                .option(new Option.Builder().value("MI").label("Michigan").build())
                .option(new Option.Builder().value("MN").label("Minnesota").build())
                .option(new Option.Builder().value("MS").label("Mississippi").build())
                .option(new Option.Builder().value("MO").label("Missouri").build())
                .option(new Option.Builder().value("MT").label("Montana").build())
                .option(new Option.Builder().value("NE").label("Nebraska").build())
                .option(new Option.Builder().value("NV").label("Nevada").build())
                .option(new Option.Builder().value("NH").label("New Hampshire").build())
                .option(new Option.Builder().value("NJ").label("New Jersey").build())
                .option(new Option.Builder().value("NM").label("New Mexico").build())
                .option(new Option.Builder().value("NY").label("New York").build())
                .option(new Option.Builder().value("NC").label("North Carolina").build())
                .option(new Option.Builder().value("ND").label("North Dakota").build())
                .option(new Option.Builder().value("OH").label("Ohio").build())
                .option(new Option.Builder().value("OK").label("Oklahoma").build())
                .option(new Option.Builder().value("OR").label("Oregon").build())
                .option(new Option.Builder().value("PA").label("Pennsylvania").build())
                .option(new Option.Builder().value("RI").label("Rhode Island").build())
                .option(new Option.Builder().value("SC").label("South Carolina").build())
                .option(new Option.Builder().value("SD").label("South Dakota").build())
                .option(new Option.Builder().value("TN").label("Tennessee").build())
                .option(new Option.Builder().value("TX").label("Texas").build())
                .option(new Option.Builder().value("UT").label("Utah").build())
                .option(new Option.Builder().value("VT").label("Vermont").build())
                .option(new Option.Builder().value("VA").label("Virginia").build())
                .option(new Option.Builder().value("WA").label("Washington").build())
                .option(new Option.Builder().value("WV").label("West Virginia").build())
                .option(new Option.Builder().value("WI").label("Wisconsin").build())
                .option(new Option.Builder().value("WY").label("Wyoming").build());
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

    private void layout(Form.Builder builder, Activity activity) {
        if (activity == null)
            return;

        ActivityUsageType usageType = activity.getUsageType() != null ? activity.getUsageType() : ActivityUsageType.USER_FORM;
        switch (usageType) {
            case MULTI_PAGE:
                builder.layout("multipage");
                break;
            case MULTI_STEP:
                builder.layout("multistep");
                break;
            case REVIEW_PAGE:
                builder.layout("review");
                break;
            default:
                builder.layout("normal");
                break;
        }
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
