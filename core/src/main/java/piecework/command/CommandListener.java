package piecework.command;

import piecework.exception.AbortCommandException;
import piecework.exception.BadRequestError;
import piecework.persistence.ProcessProvider;

/**
 * @author James Renfro
 */
public interface CommandListener {

    <T, C extends AbstractCommand<T, P>, P extends ProcessProvider> C before(C command) throws AbortCommandException, BadRequestError;

    String getProcessDefinitionKey();

}
