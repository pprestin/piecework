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
import piecework.common.ManyMap;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.security.data.DataFilterService;
import piecework.settings.UserInterfaceSettings;
import piecework.util.*;
import piecework.validation.Validation;
import piecework.repository.BucketListRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class FormFactory {

    private static final Logger LOG = Logger.getLogger(FormFactory.class);

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    UserInterfaceSettings settings;

    @Autowired
    BucketListRepository bucketListRepository;

    private final PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

    public <P extends ProcessDeploymentProvider> Form form(P modelProvider, FormRequest request, ActionType actionType, Validation validation, Explanation explanation, boolean includeRestrictedData, boolean anonymous, String version) throws PieceworkException {
        ViewContext context = new ViewContext(settings, version);
        Activity activity = modelProvider.activity();

        String formInstanceId = request.getRequestId();
        String processDefinitionKey = request.getProcessDefinitionKey();
        String processInstanceId = request.getProcessInstanceId();

        Entity principal = modelProvider.principal();
        Task task = ModelUtility.task(modelProvider);
                //request.getTask();
        boolean unmodifiable = task != null && !task.canEdit(principal);

        if (unmodifiable && actionType != null && actionType == ActionType.CREATE)
            actionType = ActionType.VIEW;

        Process process = modelProvider.process();
        Form.Builder builder = new Form.Builder()
                .process(process)
                .formInstanceId(formInstanceId)
                .processDefinitionKey(processDefinitionKey)
                .actionType(actionType);

        if (unmodifiable)
            builder.readonly();

        if (activity != null) {
            FormDisposition formDisposition = FormUtility.disposition(modelProvider, activity, actionType, context, builder);

            builder.disposition(formDisposition);

            Action action = formDisposition.getAction();

            Map<String, Field> fieldMap = activity.getFieldMap();
            // Never include instance data if the user is anonymous -- shouldn't ever happen anyway, since anonymous users can only submit data,
            // but just in case
            boolean includeInstanceData = !anonymous;
            Map<String, List<Value>> data;
            Set<Field> fields = SecurityUtility.fields(activity, action);

            boolean isAllowAny = activity.isAllowAny();

            if (anonymous || task == null)
                data = dataFilterService.allValidationData(validation, fields, isAllowAny, principal);
            else {
                // This reason will only be used if the user actually has been assigned the task -- the dataFilterService
                // will verify that
                String reason = "User is viewing an assigned task: " + task.getTaskInstanceId();

                data = dataFilterService.authorizedInstanceAndValidationData(modelProvider, validation, fields, version, reason, isAllowAny);
            }

            // If an activity is set up to allow "any" input then it also has to be provided with the full set of data currently stored for the instance
            // but just to be safe, exclude any restricted data that could have been attached
//            data = dataFilterService.filter(fieldMap, instance, task, principal, validation, includeRestrictedData, includeInstanceData, activity.isAllowAny());

            Map<String, Field> decoratedFieldMap = decorate(fieldMap, processDefinitionKey, processInstanceId, data, context, unmodifiable);
            container(builder, data, decoratedFieldMap, action);
            builder.data(data);
            builder.allowAttachments(activity.isAllowAttachments());
        }

        if (principal instanceof User)
            builder.currentUser(User.class.cast(principal));

        explanation = explanation == null ? request.getExplanation() : explanation;

        ManyMap<String, Message> messages = new ManyMap<String, Message>();
        ProcessInstance instance = ModelUtility.instance(modelProvider);
        if (instance != null && instance.getMessages() != null && !instance.getMessages().isEmpty())
            messages.putAll(instance.getMessages());
        if (request.getMessages() != null && !request.getMessages().isEmpty())
            messages.putAll(request.getMessages());
        if (validation != null && validation.getResults() != null && !validation.getResults().isEmpty())
            messages.putAll(validation.getResults());

        builder.taskSubresources(processDefinitionKey, task, context)
                .instance(instance, context)
                .messages(messages)
                .explanation(explanation)
                .anonymous(anonymous);

        if (actionType != ActionType.COMPLETE && instance != null)
            builder.applicationStatusExplanation(instance.getApplicationStatusExplanation());

        // handle buckets
        if ( process != null ) {
            String pg = process.getProcessGroup();
            if ( StringUtils.isNotEmpty(pg) ) {
                BucketList bucketList = bucketListRepository.findOne(pg);
                builder.bucketList(bucketList);
            }
        }

        return builder.build(context);
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

//    private static Action action(Form.Builder builder, ProcessDeployment deployment, Activity activity, Task task, ActionType actionType, MediaType mediaType, ViewContext version, boolean unmodifiable) throws FormBuildingException {
//        // Can't do any of this processing without an activity
//        if (activity == null)
//            return null;
//
//        FormDisposition formDisposition = FormUtility.disposition(builder, deployment, activity, actionType);
//
//        builder.disposition(formDisposition);
//
//        return formDisposition.getAction();
//    }

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
