package piecework.command;

import piecework.Command;
import piecework.exception.AbortCommandException;
import piecework.exception.BadRequestError;

/**
 * @author James Renfro
 */
public interface CommandListener {

    <T, C extends AbstractCommand<T>> C before(C command) throws AbortCommandException, BadRequestError;

    String getProcessDefinitionKey();

}
