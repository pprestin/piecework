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
package piecework.util;

import piecework.exception.PieceworkException;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.ProcessProvider;
import piecework.persistence.TaskProvider;

/**
 * @author James Renfro
 */
public class ModelUtility {

    public static <P extends ProcessProvider> String instanceId(P modelProvider) {
        try {
            ProcessInstance instance = instance(modelProvider);
            return instance != null ? instance.getProcessInstanceId() : null;
        } catch (PieceworkException e) {
            return null;
        }
    }

    public static <P extends ProcessProvider> ProcessInstance instance(P modelProvider) throws PieceworkException {
        if (modelProvider instanceof ProcessInstanceProvider) {
            ProcessInstanceProvider instanceProvider = ProcessInstanceProvider.class.cast(modelProvider);
            return instanceProvider.instance();
        }
        return null;
    }

    public static <P extends ProcessProvider> Task task(P modelProvider) throws PieceworkException {
        if (modelProvider instanceof TaskProvider) {
            TaskProvider taskProvider = TaskProvider.class.cast(modelProvider);
            return taskProvider.task();
        }
        return null;
    }

    public static <P extends ProcessProvider> Task allowedTask(P modelProvider) throws PieceworkException {
        if (modelProvider instanceof TaskProvider) {
            TaskProvider taskProvider = TaskProvider.class.cast(modelProvider);
            return taskProvider.task();
        } else if (modelProvider instanceof AllowedTaskProvider) {
            AllowedTaskProvider allowedTaskProvider = AllowedTaskProvider.class.cast(modelProvider);
            return allowedTaskProvider.allowedTask(true);
        }
        return null;
    }

}
