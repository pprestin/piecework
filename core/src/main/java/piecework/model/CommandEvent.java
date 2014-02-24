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
package piecework.model;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.exception.PieceworkException;
import piecework.persistence.ProcessProvider;
import piecework.util.ModelUtility;

import java.util.Date;

/**
 * @author James Renfro
 */
@Document(collection = "command.event")
public class CommandEvent {

    private static final Logger LOG = Logger.getLogger(CommandEvent.class);

    @Id
    private final String commandEventId;

    private final String commandDescription;

    private final String processDefinitionKey;

    private final String deploymentId;

    private final String processInstanceId;

    private final String taskId;

    private final String entityId;

    private final boolean completed;

    private final Date commandDate;

    private CommandEvent() {
        this(null, null, false);
    }

    public CommandEvent(String commandDescription, ProcessProvider processProvider, boolean completed) {
        this.commandEventId = null;
        this.commandDescription = commandDescription;
        this.entityId = processProvider.principal() != null ? processProvider.principal().getEntityId() : null;
        this.completed = completed;
        this.commandDate = new Date();

        Process process = null;
        ProcessDeployment deployment = null;
        ProcessInstance instance = null;
        Task task = null;

        try {
            process = processProvider.process();
            deployment = ModelUtility.deployment(processProvider);
            instance = ModelUtility.instance(processProvider);
            task = ModelUtility.allowedTask(processProvider);
        } catch (PieceworkException e) {
            LOG.error("Caught exception getting data model objects", e);
        }

        this.processDefinitionKey = process != null ? process.getProcessDefinitionKey() : null;
        this.processInstanceId = instance != null ? instance.getProcessInstanceId() : null;
        this.deploymentId = deployment != null ? deployment.getDeploymentId() : null;
        this.taskId = task != null ? task.getTaskInstanceId() : null;
    }

    public String getCommandEventId() {
        return commandEventId;
    }

    public String getCommandDescription() {
        return commandDescription;
    }

    public String getEntityId() {
        return entityId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Date getCommandDate() {
        return commandDate;
    }
}
