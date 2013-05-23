package piecework.engine;

import piecework.engine.exception.ProcessEngineException;
import piecework.model.*;
import piecework.model.Process;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public interface ProcessEngineCapabilities {

    String start(piecework.model.Process process, String alias, Map<String, ?> data) throws ProcessEngineException;

    boolean cancel(Process process, String processInstanceId, String alias, String reason) throws ProcessEngineException;

    ProcessExecution findExecution(ProcessExecutionCriteria criteria) throws ProcessEngineException;

    ProcessExecutionResults findExecutions(ProcessExecutionCriteria criteria) throws ProcessEngineException;

    Task findTask(TaskCriteria criteria) throws ProcessEngineException;

    TaskResults findTasks(TaskCriteria criteria) throws ProcessEngineException;

    boolean completeTask(Process process, String taskId) throws ProcessEngineException;

}
