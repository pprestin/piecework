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
package piecework.command;

import org.apache.log4j.Logger;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceRepository;
import piecework.CommandExecutor;

/**
 * @author James Renfro
 */
public class UpdateInstanceCommand extends InstanceCommand {

    private static final Logger LOG = Logger.getLogger(UpdateInstanceCommand.class);

    public UpdateInstanceCommand(Process process, ProcessInstance instance) {
        super(process, instance);
    }

    @Override
    public ProcessInstance execute(CommandExecutor commandExecutor) throws StatusCodeError {

        if (LOG.isDebugEnabled())
            LOG.debug("Executing update instance command " + this.toString());

        ProcessInstance instance = this.instance;

        if (instance == null)
            throw new InternalServerError();

        ProcessInstanceRepository repository = commandExecutor.getProcessInstanceRepository();
        instance = repository.update(instance.getProcessInstanceId(), label, data, messages, attachments, submission, applicationStatusExplanation);

        if (LOG.isDebugEnabled())
            LOG.debug("Executed update instance command " + this.toString());

        return instance;
    }

    public String toString() {
        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : "";
        String processInstanceId = instance != null ? instance.getProcessInstanceId() : "";

        return "{ processDefinitionKey: \"" + processDefinitionKey + "\", processInstanceId: \"" + processInstanceId + "\" }";
    }

}
