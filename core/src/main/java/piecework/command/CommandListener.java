package piecework.command;

import piecework.Command;
import piecework.exception.AbortCommandException;

/**
 * @author James Renfro
 */
public interface CommandListener {

    <T> Command<T> before(Command<T> command) throws AbortCommandException;

    String getProcessDefinitionKey();

}
