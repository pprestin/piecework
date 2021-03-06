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
package piecework.submission;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.common.Registry;
import piecework.enumeration.ActionType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.TaskProvider;
import piecework.service.UserInterfaceService;
import piecework.util.ActivityUtility;
import piecework.util.ProcessUtility;
import piecework.util.ValidationUtility;
import piecework.validation.ValidationRule;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class SubmissionTemplateFactory {

    @Autowired(required=false)
    Registry registry;

    @Autowired(required=false)
    UserInterfaceService userInterfaceService;

    public SubmissionTemplate submissionTemplate(TaskProvider taskProvider, FormRequest formRequest, ActionType actionType) throws PieceworkException {
        return submissionTemplate(taskProvider, actionType, formRequest, null);
    }

    public SubmissionTemplate submissionTemplate(TaskProvider taskProvider, ActionType actionType, FormRequest formRequest, String validationId) throws PieceworkException {
        ProcessDeployment deployment = taskProvider.deployment();
        if (deployment == null)
            throw new MisconfiguredProcessException("Deployment not specified in submission");

        Activity activity = taskProvider.activity();
        if (activity == null) {
            Task task = taskProvider.task();
            String taskDefinitionKey = task != null ? task.getTaskDefinitionKey() : deployment.getStartActivityKey();
            activity = deployment.getActivity(taskDefinitionKey);
        }
        return submissionTemplate(taskProvider, formRequest, actionType, activity, validationId);
    }

    public SubmissionTemplate submissionTemplate(ProcessDeploymentProvider deploymentProvider, Field field, FormRequest formRequest, boolean allowAny) throws PieceworkException {
        Process process = deploymentProvider.process();
        ProcessDeployment deployment = deploymentProvider.deployment();

        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder(process, deployment);
        if (formRequest != null)
            builder.requestId(formRequest.getRequestId()).taskId(formRequest.getTaskId()).actAsUser(formRequest.getActAsUser());

        if (allowAny)
            builder.allowAny(allowAny);
        else if (field != null)
            addField(builder, field);

        return builder.build();
    }

    public SubmissionTemplate submissionTemplate(ProcessDeploymentProvider deploymentProvider, FormRequest formRequest, ActionType actionType, String validationId) throws PieceworkException {
        return submissionTemplate(deploymentProvider, formRequest, actionType, deploymentProvider.activity(), validationId);
    }

    private static final Set<ActionType> EXCLUDE_FIELDS_ACTION_TYPES = Sets.newHashSet(ActionType.ATTACH, ActionType.REJECT, ActionType.REMOVE, ActionType.VIEW, ActionType.UPDATE);

    /*
     * Takes an activity and generates the appropriate submission template for it,
     * limiting to a specific section id
     */
    private SubmissionTemplate submissionTemplate(ProcessDeploymentProvider deploymentProvider, FormRequest formRequest, ActionType actionType, Activity activity, String validationId) throws PieceworkException {
        Process process = deploymentProvider.process();
        ProcessDeployment deployment = deploymentProvider.deployment();

        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder(process, deployment);

        boolean includeFields = !EXCLUDE_FIELDS_ACTION_TYPES.contains(actionType);
        if (formRequest != null)
            builder.requestId(formRequest.getRequestId()).taskId(formRequest.getTaskId()).actAsUser(formRequest.getActAsUser());

        builder.attachmentTemplate(actionType == ActionType.ATTACH);

        builder.allowAny(activity.isAllowAny());

        if (activity.isAllowAttachments()) {
            builder.allowAttachments();
            builder.maxAttachmentSize(activity.getMaxAttachmentSize());
        }

        if (includeFields) {
            Container parentContainer = ActivityUtility.parent(activity, ActionType.CREATE);
            Container container = ActivityUtility.child(activity, ActionType.CREATE, parentContainer);

            Set<Field> fields = new TreeSet<Field>();

            Action action = activity.action(ActionType.CREATE);
            if (action != null && action.getStrategy() != null && action.getStrategy() == DataInjectionStrategy.REMOTE && activity.isAllowAny()) {
                Set<Field> remoteFields = userInterfaceService != null ?  userInterfaceService.getRemoteFields(deploymentProvider, action, parentContainer, container, null) : null;
                if (remoteFields != null && !remoteFields.isEmpty())
                    fields.addAll(remoteFields);
            }

            Set<Field> localFields = getLocalFields(builder, activity, validationId);
            if (localFields != null && !localFields.isEmpty())
                fields.addAll(localFields);

            // If we're not validating a single container, or if we weren't able to find the container to validate,
            // then simply validate all fields for this activity
            if (fields.isEmpty())
                fields = activity.getFields();

            if (fields != null) {
                for (Field field : fields) {
                    addField(builder, field);
                }
            }
        }
        return builder.build();
    }

    private void addField(SubmissionTemplate.Builder builder, Field field) {
        if (!field.isDeleted() && field.isEditable()) {
            builder.rules(field, new ArrayList<ValidationRule>(ValidationUtility.validationRules(field, registry)));
            builder.field(field);
        }
    }

    private Set<Field> getLocalFields(SubmissionTemplate.Builder builder, Activity activity, String validationId) {
        Set<Field> fields = null;
        Container parentContainer = ActivityUtility.parent(activity, ActionType.CREATE);
        Container container = ActivityUtility.child(activity, ActionType.CREATE, parentContainer);
        if (container != null) {
            if (StringUtils.isNotEmpty(validationId))
                container = ProcessUtility.container(container, validationId);

            if (container != null) {
                Map<String, Field> fieldMap = activity.getFieldMap();
                List<String> fieldIds = ActivityUtility.fieldIds(container, parentContainer);

                if (fieldIds != null) {
                    fields = new TreeSet<Field>();
                    for (String fieldId : fieldIds) {
                        Field field = fieldMap.get(fieldId);
                        if (field != null)
                            fields.add(field);
                    }
                }

                // Only add buttons to the validation from the top-level container, or from
                // the particular validation container that is selected
                List<Button> buttons = parentContainer.getButtons();
                if (buttons != null) {
                    for (Button button : buttons) {
                        if (button == null)
                            continue;
                        builder.button(button);

                        // get child buttons as well
                        List<Button> children = button.getChildren();
                        if ( children != null ) {
                            for (Button child : children) {
                                if ( child != null ) {
                                    builder.button(child);
                                }
                            }
                        }
                    }
                }
            }
        }
        return fields;
    }


}
