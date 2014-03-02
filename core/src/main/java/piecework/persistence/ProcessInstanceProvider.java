package piecework.persistence;

import org.springframework.core.io.Resource;
import piecework.common.ViewContext;
import piecework.content.ContentReceiver;
import piecework.content.ContentResource;
import piecework.exception.PieceworkException;
import piecework.model.ProcessInstance;
import piecework.ui.Streamable;

/**
 * @author James Renfro
 */
public interface ProcessInstanceProvider extends ProcessDeploymentProvider {

    ContentResource diagram() throws PieceworkException;

    ProcessInstance instance() throws PieceworkException;

    ProcessInstance instance(ViewContext context) throws PieceworkException;

}
