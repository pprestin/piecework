package piecework.persistence;

import piecework.exception.PieceworkException;
import piecework.model.Activity;
import piecework.model.ProcessDeployment;

/**
 * @author James Renfro
 */
public interface ProcessDeploymentProvider extends ProcessProvider, ContentProfileProvider {

    Activity activity() throws PieceworkException;

    ProcessDeployment deployment() throws PieceworkException;

}
