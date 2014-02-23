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
import piecework.Constants;
import piecework.Registry;
import piecework.enumeration.ActionType;
import piecework.enumeration.FieldTag;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.persistence.TaskProvider;
import piecework.util.ActivityUtil;
import piecework.form.OptionResolver;
import piecework.util.ProcessUtility;
import piecework.util.ValidationUtility;
import piecework.validation.Validation;
import piecework.validation.ValidationRule;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
@Service
public class SubmissionTemplateFactory {

    @Autowired(required=false)
    Registry registry;

    public SubmissionTemplate submissionTemplate(TaskProvider taskProvider, FormRequest formRequest) throws PieceworkException {
        return submissionTemplate(taskProvider, formRequest, null);
    }

    public SubmissionTemplate submissionTemplate(TaskProvider taskProvider, FormRequest formRequest, String validationId) throws PieceworkException {
        ProcessDeployment deployment = taskProvider.deployment();
        if (deployment == null)
            throw new MisconfiguredProcessException("Deployment not specified in submission");

        Activity activity = formRequest.getActivity();
        if (activity == null) {
            Task task = taskProvider.task();
            String taskDefinitionKey = task != null ? task.getTaskDefinitionKey() : deployment.getStartActivityKey();
            activity = deployment.getActivity(taskDefinitionKey);
        }
        return submissionTemplate(taskProvider, formRequest, activity, validationId);
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

//    public SubmissionTemplate submissionTemplate(ProcessDeploymentProvider deploymentProvider, FormRequest formRequest) throws PieceworkException {
//        return submissionTemplate(deploymentProvider, formRequest, formRequest.getActivity(), null);
//    }

    public SubmissionTemplate submissionTemplate(ProcessDeploymentProvider deploymentProvider, FormRequest formRequest, String validationId) throws PieceworkException {
        return submissionTemplate(deploymentProvider, formRequest, formRequest.getActivity(), validationId);
    }

    /*
     * Takes an activity and generates the appropriate submission template for it,
     * limiting to a specific section id
     */
    private SubmissionTemplate submissionTemplate(ProcessDeploymentProvider deploymentProvider, FormRequest formRequest, Activity activity, String validationId) throws PieceworkException {
        Set<Field> fields = null;
        Process process = deploymentProvider.process();
        ProcessDeployment deployment = deploymentProvider.deployment();

        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder(process, deployment);

        boolean includeFields = false;
        if (formRequest != null) {
            builder.requestId(formRequest.getRequestId()).taskId(formRequest.getTaskId()).actAsUser(formRequest.getActAsUser());
            ActionType actionType = formRequest.getAction();
            includeFields = actionType == ActionType.CREATE || actionType == ActionType.COMPLETE || actionType == ActionType.VALIDATE || actionType == ActionType.SAVE;
            builder.attachmentTemplate(actionType == ActionType.ATTACH);
        }

        builder.allowAny(activity.isAllowAny());

        if (activity.isAllowAttachments()) {
            builder.allowAttachments();
            builder.maxAttachmentSize(activity.getMaxAttachmentSize());
        }

        if (includeFields) {
            Container parentContainer = ActivityUtil.parent(activity, ActionType.CREATE);
            Container container = ActivityUtil.child(activity, ActionType.CREATE, parentContainer);
            if (container != null) {
                if (StringUtils.isNotEmpty(validationId))
                    container = ProcessUtility.container(container, validationId);

                if (container != null) {
                    Map<String, Field> fieldMap = activity.getFieldMap();
                    List<String> fieldIds = ActivityUtil.fieldIds(container, parentContainer);

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
                        }
                    }
                }
            }

            // If we're not validating a single container, or if we weren't able to find the container to validate,
            // then simply validate all fields for this activity
            if (fields == null)
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


}
