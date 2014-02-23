package piecework.persistence;

import piecework.common.ViewContext;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.ui.Streamable;
import piecework.ui.streaming.StreamingAttachmentContent;

/**
 * @author James Renfro
 */
public interface ProcessInstanceProvider extends ProcessDeploymentProvider {

    Streamable diagram() throws PieceworkException;

    ProcessInstance instance() throws PieceworkException;

    ProcessInstance instance(ViewContext context) throws PieceworkException;

}
