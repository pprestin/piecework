package piecework.content;

import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentReceiver {

    boolean expire(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException;

    Content save(ContentProfileProvider modelProvider, Content content) throws PieceworkException, IOException;

    String getKey();

}
