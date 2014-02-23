package piecework.persistence;

import piecework.common.ViewContext;
import piecework.exception.PieceworkException;
import piecework.model.History;

/**
 * @author James Renfro
 */
public interface HistoryProvider extends ProcessInstanceProvider {

    History history(ViewContext context) throws PieceworkException;

}
