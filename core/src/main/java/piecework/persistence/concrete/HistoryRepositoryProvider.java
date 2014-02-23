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
package piecework.persistence.concrete;

import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.EventType;
import piecework.enumeration.OperationType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.HistoryProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.AttachmentRepository;
import piecework.repository.ContentRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.IdentityService;
import piecework.task.TaskFactory;

import java.util.*;

/**
 * @author James Renfro
 */
public class HistoryRepositoryProvider extends ProcessInstanceRepositoryProvider implements HistoryProvider{

    private final IdentityService identityService;

    public HistoryRepositoryProvider(ProcessProvider processProvider, ProcessInstanceRepository processInstanceRepository, ProcessEngineFacade facade, AttachmentRepository attachmentRepository, ContentRepository contentRepository, DeploymentRepository deploymentRepository, IdentityService identityService, String processInstanceId) {
        super(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, processInstanceId);
        this.identityService = identityService;
    }

    public History history(ViewContext context) throws PieceworkException {
        Process process = process();
        ProcessInstance instance = instance();

        List<Operation> operations = instance.getOperations();
        Set<Task> tasks = instance.getTasks();

        User initiator = instance.getInitiatorId() != null ? identityService.getUser(instance.getInitiatorId()) : null;

        History.Builder history = new History.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .processInstanceId(instance.getProcessInstanceId())
                .processInstanceLabel(instance.getProcessInstanceLabel())
                .startTime(instance.getStartTime())
                .endTime(instance.getEndTime())
                .initiator(initiator);

        int i = 1;
        if (operations != null) {
            for (Operation operation : operations) {
                String id = "operation-" + i;
                String userId = operation.getUserId();
                User user = userId != null ? identityService.getUser(userId) : null;
                if (operation.getType() == OperationType.ASSIGNMENT) {
                    User assignee = operation.getReason() != null ? identityService.getUser(operation.getReason()) : null;
                    String assigneeName = assignee != null ? assignee.getDisplayName() + " (" + assignee.getVisibleId() + ") " : operation.getReason();
                    operation = new Operation(operation.getId(), operation.getType(), assigneeName, operation.getDate(), operation.getUserId());
                }
                history.event(new Event.Builder().id(id).type(EventType.OPERATION).operation(operation).date(operation.getDate()).user(user).build());
                i++;
            }
        }

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        if (tasks != null) {
            // Loop through first to gather the set of all user ids that we want to look up
            Set<String> userIds = new HashSet<String>();
            for (Task task : tasks) {
                userIds.addAll(task.getAssigneeAndCandidateAssigneeIds());
            }
            Map<String, User> userMap = identityService.findUsers(userIds);
            for (Task task : tasks) {
                String id = "task-" + i;
                Date date = task.getStartTime();
                Task decoratedTask = TaskFactory.task(task, passthroughSanitizer, userMap, context);
                history.event(new Event.Builder().id(id).type(EventType.TASK).task(decoratedTask).date(date).user(decoratedTask.getAssignee()).build());
                i++;
            }
        }

        return history.build(context);
    }

}
