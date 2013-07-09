package piecework.engine;

import piecework.engine.exception.ProcessEngineException;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;

import java.util.Map;

/**
 * @author James Renfro
 */
public interface ProcessEngineCapabilities {

    String start(Process process, String alias, Map<String, ?> data) throws ProcessEngineException;

    boolean activate(Process process, ProcessInstance instance, String reason) throws ProcessEngineException;

    boolean cancel(Process process, ProcessInstance instance, String reason) throws ProcessEngineException;

    boolean suspend(Process process, ProcessInstance instance, String reason) throws ProcessEngineException;

    ProcessExecution findExecution(ProcessInstanceSearchCriteria criteria) throws ProcessEngineException;

    ProcessExecutionResults findExecutions(ProcessInstanceSearchCriteria criteria) throws ProcessEngineException;

    Task findTask(TaskCriteria criteria) throws ProcessEngineException;

    TaskResults findTasks(TaskCriteria criteria) throws ProcessEngineException;

    boolean completeTask(Process process, String taskId, String action) throws ProcessEngineException;

    void deploy(Process process, String name, ProcessModelResource ... resources) throws ProcessEngineException;

}
