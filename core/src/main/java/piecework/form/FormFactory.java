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
import piecework.exception.FormBuildingException;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.DataFilterService;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ConstraintUtil;
import piecework.util.FormUtility;
import piecework.util.ProcessInstanceUtility;
import piecework.validation.Validation;

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

    public Form form(Process process, ProcessDeployment deployment, FormRequest request, ActionType actionType, Entity principal, MediaType mediaType, Validation validation, Explanation explanation, boolean includeRestrictedData, boolean anonymous) throws FormBuildingException {
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
                .process(process)
                .formInstanceId(formInstanceId)
                .processDefinitionKey(processDefinitionKey);

        if (unmodifiable)
            builder.readonly();

        if (activity != null) {
            Action action = action(builder, deployment, activity, task, actionType, mediaType, version, unmodifiable);
            Map<String, Field> fieldMap = activity.getFieldMap();
            // Never include instance data if the user is anonymous -- shouldn't ever happen anyway, since anonymous users can only submit data,
            // but just in case
            boolean includeInstanceData = !anonymous;
            Map<String, List<Value>> data;
            // If an activity is set up to allow "any" input then it also has to be provided with the full set of data currently stored for the instance
            // but just to be safe, exclude any restricted data that could have been attached
            data = dataFilterService.filter(fieldMap, instance, task, principal, validation, includeRestrictedData, includeInstanceData, activity.isAllowAny());

            Map<String, Field> decoratedFieldMap = decorate(fieldMap, processDefinitionKey, processInstanceId, data, version, unmodifiable);
            container(builder, data, decoratedFieldMap, action);
            builder.data(data);
            builder.allowAttachments(activity.isAllowAttachments());
        }

        if (principal instanceof User)
            builder.currentUser(User.class.cast(principal));

        explanation = explanation == null ? request.getExplanation() : explanation;

        builder.taskSubresources(processDefinitionKey, task, version)
                .instance(instance, version)
                .messages(request.getMessages())
                .explanation(explanation)
                .anonymous(anonymous);

        if (actionType != ActionType.COMPLETE && instance != null)
            builder.applicationStatusExplanation(instance.getApplicationStatusExplanation());

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

    private Action action(Form.Builder builder, ProcessDeployment deployment, Activity activity, Task task, ActionType actionType, MediaType mediaType, ViewContext version, boolean unmodifiable) throws FormBuildingException {
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

        URI uri = FormUtility.safeUri(action, task);
        boolean external = FormUtility.isExternal(uri);

        FormDisposition formDisposition = null;

        if (mediaType.equals(MediaType.TEXT_HTML_TYPE) && !revertToDefaultUI) {
            switch (action.getStrategy()) {
                case DECORATE_HTML:
                    formDisposition = new FormDisposition(deployment.getBase(), action.getLocation(), action.getStrategy());
                    break;
                case INCLUDE_DIRECTIVES:
                case INCLUDE_SCRIPT:
                    if (external)
                        formDisposition = new FormDisposition(uri, action.getStrategy());
                    else if (action.getLocation() != null)
                        formDisposition = new FormDisposition(deployment.getBase(), action.getLocation(), action.getStrategy());
                    break;
            }
        }

        // Tacking this on at the end - could be somewhere better
        if (formDisposition == null) {
            formDisposition = new FormDisposition();
            FormUtility.layout(builder, activity);
        }

        builder.disposition(formDisposition);

        return action;
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
                        FormUtility.addStateOptions(fieldBuilder);
                    if (StringUtils.isNotEmpty(processInstanceId) && ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER, constraints))
                        FormUtility.addConfirmationNumber(fieldBuilder, processInstanceId);
                }

                if (unmodifiable)
                    fieldBuilder.readonly();

                decoratedFieldMap.put(fieldName, fieldBuilder.build(version));
            }
        }
        return decoratedFieldMap;
    }

}
