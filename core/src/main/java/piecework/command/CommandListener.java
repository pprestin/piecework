package piecework.command;

import piecework.Command;
import piecework.exception.AbortCommandException;
import piecework.exception.BadRequestError;

/**
 * @author James Renfro
 */
public interface CommandListener {

    <T> Command<T> before(Command<T> command) throws AbortCommandException, BadRequestError;

    String getProcessDefinitionKey();

}
