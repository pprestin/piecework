package piecework.persistence;

import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.model.ProcessDeployment;

/**
 * @author James Renfro
 */
public interface ProcessDeploymentProvider extends ProcessProvider {

    ProcessDeployment deployment() throws PieceworkException;

}
