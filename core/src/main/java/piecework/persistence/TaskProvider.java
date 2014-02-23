package piecework.persistence;

import piecework.common.ViewContext;
import piecework.exception.PieceworkException;
import piecework.model.Task;
/**
 * @author James Renfro
 */
public interface TaskProvider extends ProcessInstanceProvider {

    Task task() throws PieceworkException;

    Task task(ViewContext context, boolean limitToActive) throws PieceworkException;

}
