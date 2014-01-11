package piecework.engine;

import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;
import piecework.validation.Validation;

/**
 * @author James Renfro
 */
public interface ProcessEngineCapabilities {

    String start(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException;

    boolean activate(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException;

    boolean assign(Process process, ProcessDeployment deployment, String taskId, User user) throws ProcessEngineException;

    boolean cancel(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException;

    boolean suspend(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException;

    ProcessExecution findExecution(ProcessInstanceSearchCriteria criteria) throws ProcessEngineException;

    ProcessExecutionResults findExecutions(ProcessInstanceSearchCriteria criteria) throws ProcessEngineException;

    Task findTask(Process process, ProcessDeployment deployment, String taskId, boolean limitToActive) throws ProcessEngineException;

    TaskResults findTasks(TaskCriteria ... criterias) throws ProcessEngineException;

    boolean completeTask(Process process, ProcessDeployment deployment, String taskId, ActionType action, Validation validation, Entity principal) throws ProcessEngineException;

    Task createSubTask(Process process, ProcessDeployment deployment, String parentTaskId, ProcessInstance instance, Validation validation) throws ProcessEngineException;

    ProcessDeployment deploy(Process process, ProcessDeployment deployment, Content content) throws ProcessEngineException;

    ProcessDeploymentResource resource(Process process, ProcessDeployment deployment, String contentType) throws ProcessEngineException;

    ProcessDeploymentResource resource(Process process, ProcessDeployment deployment, ProcessInstance instance, String contentType) throws ProcessEngineException;

}
