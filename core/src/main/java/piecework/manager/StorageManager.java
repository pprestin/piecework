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
package piecework.manager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.OperationResult;
import piecework.enumeration.ActionType;
import piecework.enumeration.OperationType;
import piecework.exception.InternalServerError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.repository.AttachmentRepository;
import piecework.repository.ContentRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.util.ProcessInstanceUtility;
import piecework.validation.Validation;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class StorageManager {

    private static final Logger LOG = Logger.getLogger(StorageManager.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    public ProcessInstance archive(ProcessInstance instance, Map<String, List<Value>> data) {
        String deploymentId = instance.getDeploymentId();
        ProcessDeployment deployment = StringUtils.isNotEmpty(deploymentId) ? deploymentRepository.findOne(deploymentId) : null;
        String completionStatus = null;
        if (deployment != null)
            completionStatus = deployment.getCompletionStatus();

        return processInstanceRepository.update(instance.getProcessInstanceId(), Constants.ProcessStatuses.COMPLETE, completionStatus, data);
    }

    public boolean expire(ContentProfileProvider modelProvider, String location) throws PieceworkException {
        try {
            return contentRepository.expireByLocation(modelProvider, location);
        } catch (java.io.IOException e) {
            LOG.error("Failed to expire", e);
            InternalServerError error = new InternalServerError(Constants.ExceptionCodes.system_misconfigured, e.getMessage());
            if (modelProvider instanceof ProcessDeploymentProvider)
                error.setModelProvider(ProcessDeploymentProvider.class.cast(modelProvider));
            throw error;
        }
    }

    public ProcessInstance get(String processInstanceId) {
        return processInstanceRepository.findOne(processInstanceId);
    }

    public ProcessInstance minusAttachment(ProcessInstance instance, String attachmentId, Entity principal) {
        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance);
        builder.removeAttachment(attachmentId);
        // TODO: Add audit trail to indicate that principal removed attachment
        return processInstanceRepository.save(builder.build());
    }

    public ProcessInstance create(Process process, ProcessDeployment deployment, Map<String, List<Value>> data, Collection<Attachment> attachments, Submission submission, String initiatorId) {

        if (attachments != null && !attachments.isEmpty()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Persisting " + attachments.size() + " attachments");
            attachments = attachmentRepository.save(attachments);
        }

        String submissionLabel = submission != null ? submission.getProcessInstanceLabel() : null;
        String label = ProcessInstanceUtility.processInstanceLabel(process, null, data, submissionLabel);
        String initiationStatus = deployment.getInitiationStatus();

        ProcessInstance.Builder builder = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .processInstanceLabel(label)
                .deploymentId(deployment.getDeploymentId())
                .data(data)
                .submission(submission)
                .startTime(new Date())
                .initiatorId(initiatorId)
                .processStatus(Constants.ProcessStatuses.OPEN)
                .attachments(attachments)
                .applicationStatus(initiationStatus);

        if (process.isAllowPerInstanceActivities())
            builder.activityMap(submission.getActivityMap());

        return processInstanceRepository.save(builder.build());
    }

    public boolean store(String processInstanceId, String engineInstanceId) {
        return processInstanceRepository.update(processInstanceId, engineInstanceId);
    }

    public ProcessInstance store(String label, ProcessInstance instance, Map<String, List<Value>> data, Submission submission) {
        return processInstanceRepository.update(instance.getProcessInstanceId(), label, data, null, null, submission, null);
    }

    public ProcessInstance store(ProcessInstance instance, Map<String, List<Value>> data, Map<String, List<Message>> messages, Submission submission, String applicationStatusExplanation) {
        return processInstanceRepository.update(instance.getProcessInstanceId(), null, data, messages, null, submission, applicationStatusExplanation);
    }

    public ProcessInstance store(OperationType operationType, OperationResult result, ProcessInstance instance, Entity principal) {
        ProcessInstance.Builder modified = new ProcessInstance.Builder(instance);
        String applicationStatus = instance.getApplicationStatus();
        String processStatus = instance.getProcessStatus();
        String applicationStatusExplanation = instance.getApplicationStatusExplanation();

        if (result != null) {
            if (StringUtils.isNotEmpty(applicationStatus))
                applicationStatus = result.getDefaultApplicationStatus();

            if (result.getProcessStatus() != null)
                processStatus = result.getProcessStatus();

            if (result.getApplicationStatusExplanation() != null)
                applicationStatusExplanation = result.getApplicationStatusExplanation();

            if (result.getPreviousApplicationStatus() != null)
                modified.previousApplicationStatus(result.getPreviousApplicationStatus());
        }

        String actingAsUserId = principal != null ? principal.getActingAsId() : null;
        Set<Task> tasks = null;

        if (operationType != OperationType.ASSIGNMENT && operationType != OperationType.UPDATE)
            tasks = ProcessInstanceUtility.tasks(instance.getTasks(), operationType);

        return processInstanceRepository.update(instance.getProcessInstanceId(), new Operation(UUID.randomUUID().toString(), operationType, result.getOperationDescription(), new Date(), actingAsUserId), applicationStatus, applicationStatusExplanation, processStatus, tasks);
    }

    public ProcessInstance store(ProcessInstanceProvider instanceProvider, Validation validation, ActionType actionType) throws PieceworkException {
        String applicationStatusExplanation = validation.getApplicationStatusExplanation();
        Map<String, List<Message>> messages = validation.getResults();
        List<Attachment> attachments = validation.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Persisting " + attachments.size() + " attachments");
            attachments = attachmentRepository.save(attachments);
        }

        boolean isAttachment = actionType == ActionType.ATTACH;

        Process process = instanceProvider.process();
        ProcessInstance instance = instanceProvider.instance();
        String processInstanceId = instance.getProcessInstanceId();
        Submission submission = validation.getSubmission();
        Map<String, List<Value>> data = isAttachment ? null : validation.getData();
        String submissionLabel = submission != null ? submission.getProcessInstanceLabel() : null;
        String label = ProcessInstanceUtility.processInstanceLabel(process, instance, data, submissionLabel);

        return processInstanceRepository.update(processInstanceId, label, data, messages, attachments, submission, applicationStatusExplanation);
    }

//    private ProcessInstance store(Process process, ProcessInstance instance, Validation validation, boolean isAttachment) throws PieceworkException {
//        return store(process, instance, validation, validation.getSubmission(), isAttachment);
//    }
//
//    private ProcessInstance store(Process process, ProcessInstance instance, Validation validation, Submission submission, boolean isAttachment) throws PieceworkException {
//        String applicationStatusExplanation = validation.getApplicationStatusExplanation();
//        Map<String, List<Message>> messages = validation.getResults();
//        List<Attachment> attachments = validation.getAttachments();
//        if (attachments != null && !attachments.isEmpty()) {
//            if (LOG.isDebugEnabled())
//                LOG.debug("Persisting " + attachments.size() + " attachments");
//            attachments = attachmentRepository.save(attachments);
//        }
//
//        Map<String, List<Value>> data = isAttachment ? null : validation.getData();
//        String label = ProcessInstanceUtility.processInstanceLabel(process, null, data, submission.getProcessInstanceLabel());
//
//        return processInstanceRepository.update(instance.getProcessInstanceId(), label, data, messages, attachments, submission, applicationStatusExplanation);
//    }

    public boolean store(ProcessInstance instance, Task task) {
        if (task == null)
            return false;

        return processInstanceRepository.update(instance.getProcessInstanceId(), task);
    }

    public ProcessInstance store(ProcessInstance instance) {
        if (instance == null)
            return null;

        return processInstanceRepository.save(instance);
    }
}
