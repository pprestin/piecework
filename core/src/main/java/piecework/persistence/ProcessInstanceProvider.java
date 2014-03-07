package piecework.persistence;

import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.exception.PieceworkException;
import piecework.model.ProcessInstance;

/**
 * @author James Renfro
 */
public interface ProcessInstanceProvider extends ProcessDeploymentProvider {

    ContentResource diagram() throws PieceworkException;

    ProcessInstance instance() throws PieceworkException;

    ProcessInstance instance(ViewContext context) throws PieceworkException;

}
