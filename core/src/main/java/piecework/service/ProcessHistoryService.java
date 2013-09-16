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
package piecework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.enumeration.EventType;
import piecework.enumeration.OperationType;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityDetails;
import piecework.model.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class ProcessHistoryService {

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    TaskService taskService;

    @Autowired
    Versions versions;

    public History read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
        piecework.model.Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);

        List<Operation> operations = instance.getOperations();
        Set<Task> tasks = instance.getTasks();

        User initiator = identityService.getUser(instance.getInitiatorId());

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
                User user = identityService.getUser(userId);
                if (operation.getType() == OperationType.ASSIGNMENT) {
                    User assignee = identityService.getUser(operation.getReason());
                    String assigneeName = assignee != null ? assignee.getDisplayName() + " (" + assignee.getVisibleId() + ") " : operation.getReason();
                    operation = new Operation(operation.getId(), operation.getType(), assigneeName, operation.getDate(), operation.getUserId());
                }
                history.event(new Event.Builder().id(id).type(EventType.OPERATION).operation(operation).date(operation.getDate()).user(user).build());
                i++;
            }
        }

        if (tasks != null) {
            for (Task task : tasks) {
                String id = "task-" + i;
                Date date = task.getStartTime();
                history.event(new Event.Builder().id(id).type(EventType.TASK).task(task).date(date).user(task.getAssignee()).build());
                i++;
            }
        }

        return history.build(versions.getVersion1());
    }

}
