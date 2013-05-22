package piecework.engine;

import piecework.model.*;
import piecework.model.Process;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public interface ProcessEngineCapabilities {

    String start(piecework.model.Process process, String alias, Map<String, ?> data);

    boolean cancel(Process process, String processInstanceId, String alias, String reason);

    ProcessExecution findExecution(ProcessExecutionCriteria criteria);

    List<ProcessExecution> findExecutions(ProcessExecutionCriteria criteria);

    Task findTask(TaskCriteria criteria);

    List<Task> findTasks(TaskCriteria criteria);

    void completeTask(Process process, String taskId);

}
