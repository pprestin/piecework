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
package piecework.task;

import org.apache.commons.lang.StringUtils;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.engine.EngineTask;
import piecework.enumeration.ActionType;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.model.User;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public class TaskFactory {

    public static Task task(Task task, Sanitizer sanitizer, Map<String, User> userMap, ViewContext version) {
        Task.Builder builder = new Task.Builder(task, sanitizer);

        if (StringUtils.isNotEmpty(task.getAssigneeId())) {
            User assignee = userMap.get(task.getAssigneeId());
            builder.assignee(assignee);
        }
        Set<String> candidateAssigneeIds = task.getCandidateAssigneeIds();
        if (candidateAssigneeIds != null && !candidateAssigneeIds.isEmpty()) {
            for (String candidateAssigneeId : candidateAssigneeIds) {
                User user = userMap.get(candidateAssigneeId);
                if ( user != null ) {
                    builder.candidateAssignee(user);
                } else {
                    builder.candidateAssigneeId(candidateAssigneeId);  // group ID
                }
            }
        }

        return builder.build(version);
    }

    public static Task task(Task task, EngineTask delegateTask, boolean isComplete) {
        if (task == null)
            return null;

        ActionType action = delegateTask.getActionType();
        String taskStatus = action == ActionType.REJECT ? Constants.TaskStatuses.REJECTED : Constants.TaskStatuses.COMPLETE;

        Task.Builder taskBuilder = new Task.Builder(task, new PassthroughSanitizer());

        if (isComplete)
            taskBuilder.taskAction(action.name())
                .taskStatus(taskStatus)
                .finished()
                .endTime(new Date());

        taskBuilder.assigneeId(delegateTask.getAssigneeId());
        taskBuilder.candidateAssigneeIds(delegateTask.getCandidateAssigneeIds(), delegateTask.getCandidateGroupIds());

        return taskBuilder.build();
    }

    public static Task task(piecework.model.Process process, ProcessInstance processInstance, EngineTask delegateTask) {
        Task.Builder taskBuilder = new Task.Builder()
                .taskInstanceId(delegateTask.getTaskId())
                .taskDefinitionKey(delegateTask.getTaskDefinitionKey())
                .processInstanceId(processInstance.getProcessInstanceId())
                .processInstanceAlias(processInstance.getAlias())
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .processInstanceLabel(processInstance.getProcessInstanceLabel())
                .engineProcessInstanceId(delegateTask.getProcessInstanceId())
                .taskLabel(delegateTask.getName())
                .taskDescription(delegateTask.getDescription())
                .taskStatus(Constants.TaskStatuses.OPEN)
                .taskAction(delegateTask.getActionType().name())
                .startTime(delegateTask.getStartTime())
                .dueDate(delegateTask.getDueDate())
                .priority(delegateTask.getPriority())
                .assigneeId(delegateTask.getAssigneeId())
                .candidateAssigneeIds(delegateTask.getCandidateAssigneeIds(), delegateTask.getCandidateGroupIds())
                .active();

        return taskBuilder.build();
    }

}
