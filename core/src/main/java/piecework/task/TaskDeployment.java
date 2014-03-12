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

import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.model.Task;

import java.util.Map;

/**
 * @author James Renfro
 */
public class TaskDeployment {

    private final ProcessDeployment deployment;
    private final ProcessInstance instance;
    private final Task task;
    private final Map<String, Object> instanceData;

    public TaskDeployment(ProcessDeployment deployment, ProcessInstance instance, Task task) {
        this.deployment = deployment;
        this.instance = instance;
        this.task = task;
        this.instanceData = null;
    }

    public TaskDeployment(ProcessDeployment deployment, ProcessInstance instance, Task task, Map<String, Object> instanceData) {
        this.deployment = deployment;
        this.instance = instance;
        this.task = task;
        this.instanceData = instanceData;
    }

    public ProcessDeployment getDeployment() {
        return deployment;
    }

    public ProcessInstance getInstance() {
        return instance;
    }

    public Task getTask() {
        return task;
    }

    public Map<String, Object> getInstanceData() {
        return instanceData;
    }
}
