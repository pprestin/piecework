package piecework.engine;

import java.util.Map;

/**
 * @author James Renfro
 */
public interface EngineContext {

    Map<String, String> getStartFormProperties();

    Map<String, String> getTaskFormProperties(String taskId);

    <T> T getInstanceVariable(String name);

    <T> T getTaskVariable(String taskId, String name);

}
