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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.OperationType;
import piecework.exception.ConflictError;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.service.IdentityService;
import piecework.model.*;
import piecework.CommandExecutor;
import piecework.identity.IdentityHelper;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ProcessInstanceUtility;

import java.util.*;

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

    public ProcessInstance execute(CommandExecutor commandExecutor) throws StatusCodeError {
        if (LOG.isDebugEnabled())
            LOG.debug("Executing instance state command " + this.toString());

        ProcessEngineFacade facade = commandExecutor.getFacade();
        IdentityHelper helper = commandExecutor.getHelper();
        IdentityService identityService = commandExecutor.getIdentityService();
        ProcessInstanceRepository processInstanceRepository = commandExecutor.getProcessInstanceRepository();
        DeploymentRepository deploymentRepository = commandExecutor.getDeploymentRepository();

        try {
            ProcessInstance.Builder modified = new ProcessInstance.Builder(instance);
            String processStatus = null;
            String applicationStatusExplanation = null;

            if (operation != OperationType.UPDATE) {
                ProcessDeployment deployment = process.getDeployment();
                if (instance.getDeploymentId() != null && !instance.getDeploymentId().equals(process.getDeploymentId()))
                    deployment = deploymentRepository.findOne(instance.getDeploymentId());

                if (deployment == null)
                    throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

                String defaultApplicationStatus;
                switch(operation) {
                    case ACTIVATION:
                        if (!facade.activate(process, deployment, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = instance.getPreviousApplicationStatus();
                        processStatus = Constants.ProcessStatuses.OPEN;
                        applicationStatusExplanation = reason;
                        break;
                    case ASSIGNMENT:
                        boolean isAssignmentRestricted = process.isAssignmentRestrictedToCandidates();
                        if (isAssignmentRestricted && StringUtils.isNotEmpty(reason)) {
                            Set<String> candidateAssigneeIds = task.getCandidateAssigneeIds();
                            if (!candidateAssigneeIds.contains(reason))
                                throw new ForbiddenError(Constants.ExceptionCodes.invalid_assignment);
                        }
                        if (!facade.assign(process, deployment, task.getTaskInstanceId(), identityService.getUserByAnyId(reason)))
                            throw new ForbiddenError(Constants.ExceptionCodes.invalid_assignment);
                        defaultApplicationStatus = instance.getPreviousApplicationStatus();
                        break;
                    case CANCELLATION:
                        if (!facade.cancel(process, deployment, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = deployment.getCancellationStatus();
                        processStatus = Constants.ProcessStatuses.CANCELLED;
                        applicationStatusExplanation = reason;
                        break;
                    case SUSPENSION:
                        if (!facade.suspend(process, deployment, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = deployment.getSuspensionStatus();
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

            Entity principal = helper.getPrincipal();
            String actingAsUserId = principal.getActingAsId();
            Set<Task> tasks = null;

            if (operation != OperationType.ASSIGNMENT && operation != OperationType.UPDATE)
                tasks = ProcessInstanceUtility.tasks(instance.getTasks(), operation);

            processInstanceRepository.update(instance.getProcessInstanceId(), new Operation(UUID.randomUUID().toString(), operation, reason, new Date(), actingAsUserId), applicationStatus, applicationStatusExplanation, processStatus, tasks);

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

}
