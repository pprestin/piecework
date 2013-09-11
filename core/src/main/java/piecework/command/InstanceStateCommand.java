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

import com.mongodb.WriteResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import piecework.Constants;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.OperationType;
import piecework.exception.ConflictError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityService;
import piecework.model.*;
import piecework.Toolkit;
import piecework.identity.IdentityHelper;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class InstanceStateCommand extends InstanceCommand {

    private static final Logger LOG = Logger.getLogger(InstanceStateCommand.class);

    protected final OperationType operation;

    protected String applicationStatus;
    protected String reason;
    protected Task task;

    public InstanceStateCommand(piecework.model.Process process, ProcessInstance instance, OperationType operation) {
        super(process, instance);
        this.operation = operation;
    }

    public InstanceStateCommand applicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
        return this;
    }

    public InstanceStateCommand reason(String reason) {
        this.reason = reason;
        return this;
    }

    public InstanceStateCommand task(Task task) {
        this.task = task;
        return this;
    }

    public ProcessInstance execute(Toolkit toolkit) throws StatusCodeError {
        if (LOG.isDebugEnabled())
            LOG.debug("Executing instance state command " + this.toString());

        ProcessEngineFacade facade = toolkit.getFacade();
        IdentityHelper helper = toolkit.getHelper();
        IdentityService identityService = toolkit.getIdentityService();
        MongoTemplate operations = toolkit.getMongoOperations();

        try {
            ProcessInstance.Builder modified = new ProcessInstance.Builder(instance);
            String processStatus = null;
            String applicationStatusExplanation = null;

            if (operation != OperationType.UPDATE) {
                String defaultApplicationStatus;
                switch(operation) {
                    case ACTIVATION:
                        if (!facade.activate(process, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = instance.getPreviousApplicationStatus();
                        processStatus = Constants.ProcessStatuses.OPEN;
                        applicationStatusExplanation = reason;
                        break;
                    case ASSIGNMENT:
                        if (!facade.assign(process, task.getTaskInstanceId(), identityService.getUserByAnyId(reason)))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_assignment);
                        defaultApplicationStatus = instance.getPreviousApplicationStatus();
                        break;
                    case CANCELLATION:
                        if (!facade.cancel(process, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = process.getCancellationStatus();
                        processStatus = Constants.ProcessStatuses.CANCELLED;
                        applicationStatusExplanation = reason;
                        break;
                    case SUSPENSION:
                        if (!facade.suspend(process, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = process.getSuspensionStatus();
                        modified.previousApplicationStatus(instance.getApplicationStatus());
                        processStatus = Constants.ProcessStatuses.SUSPENDED;
                        applicationStatusExplanation = reason;
                        break;
                    default:
                        throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                }

                if (StringUtils.isNotEmpty(applicationStatus))
                    applicationStatus = defaultApplicationStatus;
            }

            String userId = helper.getAuthenticatedSystemOrUserId();

            Update update = new Update();

            if (applicationStatus != null)
                update.set("applicationStatus", applicationStatus);
            if (applicationStatusExplanation != null)
                update.set("applicationStatusExplanation", applicationStatusExplanation);
            if (processStatus != null)
                update.set("processStatus", processStatus);

            update.set("tasks", tasks(instance.getTasks(), operation));
            update.push("operations", new Operation(UUID.randomUUID().toString(), operation, reason, new Date(), userId));

            WriteResult result = operations.updateFirst(new Query(where("_id").is(instance.getProcessInstanceId())),
                    update,
                    ProcessInstance.class);

            String error = result.getError();
            if (StringUtils.isNotEmpty(error))
                LOG.error("Unable to correctly save applicationStatus " + applicationStatus + ", processStatus " + processStatus + ", and reason " + reason + " for " + instance.getProcessInstanceId() + ": " + error);

            if (LOG.isDebugEnabled())
                LOG.debug("Executed instance state command " + this.toString());

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to cancel execution ", e);
            throw new InternalServerError();
        }

        return null;
    }

    public String toString() {
        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : "";
        String processInstanceId = instance != null ? instance.getProcessInstanceId() : "";

        return "{ processDefinitionKey: \"" + processDefinitionKey + "\", processInstanceId: \"" + processInstanceId + "\", operation: \"" + operation + "\" }";
    }

    private static List<Task> tasks(List<Task> originals, OperationType operation) {
        List<Task> tasks = new ArrayList<Task>();
        if (originals != null && !originals.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            for (Task original : originals) {
                Task.Builder builder = new Task.Builder(original, passthroughSanitizer);
                switch (operation) {
                    case ACTIVATION:
                        if (original.getTaskStatus().equals(Constants.TaskStatuses.SUSPENDED))
                            builder.taskStatus(Constants.TaskStatuses.OPEN);
                        break;
                    case CANCELLATION:
                        if (original.getTaskStatus().equals(Constants.TaskStatuses.OPEN))
                            builder.taskStatus(Constants.TaskStatuses.CANCELLED);
                        break;
                    case SUSPENSION:
                        if (original.getTaskStatus().equals(Constants.TaskStatuses.OPEN))
                            builder.taskStatus(Constants.TaskStatuses.SUSPENDED);
                        break;
                }
                tasks.add(builder.build());
            }
        }

        return tasks;
    }

}
