package piecework.persistence;

import piecework.common.ViewContext;
import piecework.exception.PieceworkException;
import piecework.model.Process;

/**
 * @author James Renfro
 */
public interface ProcessProvider extends ModelProvider {

    Process process() throws PieceworkException;

    Process process(ViewContext context) throws PieceworkException;

    String processDefinitionKey();

}
