package piecework.content;

import piecework.model.*;
import piecework.model.Process;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentReceiver {

    Content save(Process process, ProcessInstance instance, Content content, Entity principal) throws IOException;

    String getKey();

}
