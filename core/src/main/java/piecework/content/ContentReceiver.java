package piecework.content;

import piecework.model.Content;
import piecework.model.Entity;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentReceiver {

    Content save(Content content, Entity principal) throws IOException;

    String getKey();

}
