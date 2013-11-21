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
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.security.DataFilterService;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public class TaskFilter {

    private final DataFilterService dataFilterService;
    private final Entity principal;
    private final Set<String> overseerProcessDefinitionKeys;
    private final boolean wrapWithForm;
    private final boolean includeData;

    public TaskFilter(DataFilterService dataFilterService, Entity principal, Set<String> overseerProcessDefinitionKeys, boolean wrapWithForm, boolean includeData) {
        this.dataFilterService = dataFilterService;
        this.principal = principal;
        this.overseerProcessDefinitionKeys = overseerProcessDefinitionKeys;
        this.wrapWithForm = wrapWithForm;
        this.includeData = includeData;
    }

    public Object result(TaskDeployment deployment, Map<String, User> userMap, ViewContext version1) {
        Task rebuilt = TaskFactory.task(deployment.getTask(), new PassthroughSanitizer(), userMap, version1);
        if (wrapWithForm) {
            ProcessDeployment processDeployment = deployment.getDeployment();
            Activity activity = processDeployment != null ? processDeployment.getActivity(rebuilt.getTaskDefinitionKey()) : null;
            Action createAction = activity != null ? activity.action(ActionType.CREATE) : null;
            boolean external = createAction != null && StringUtils.isNotEmpty(createAction.getLocation());

            Map<String, List<Value>> data = null;

            if (includeData)
                data = dataFilterService.filter(activity.getFieldMap(), deployment.getInstance(), null, principal, false);

            return new Form.Builder()
                    .formInstanceId(rebuilt.getTaskInstanceId())
                    .taskSubresources(rebuilt.getProcessDefinitionKey(), rebuilt, version1)
                    .processDefinitionKey(rebuilt.getProcessDefinitionKey())
                    .instanceSubresources(rebuilt.getProcessDefinitionKey(),
                            rebuilt.getProcessInstanceId(), null, 0, version1)
                    .data(data)
                    .external(external)
                    .build(version1);
        }
        return rebuilt;
    }

    public boolean include(Task task, String processStatus, String taskStatus) {
        if (!processStatus.equals(Constants.ProcessStatuses.ALL) &&
                !processStatus.equalsIgnoreCase(task.getTaskStatus()))
            return false;

        if (!taskStatus.equals(Constants.TaskStatuses.ALL) &&
                !taskStatus.equalsIgnoreCase(task.getTaskStatus()))
            return false;

        return overseerProcessDefinitionKeys.contains(task.getProcessDefinitionKey())
                || task.isCandidateOrAssignee(principal);
    }

    public boolean isWrapWithForm() {
        return wrapWithForm;
    }
}
