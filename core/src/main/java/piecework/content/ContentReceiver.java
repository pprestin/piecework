package piecework.content;

import piecework.model.Content;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentReceiver {

    Content save(Content content) throws IOException;

    String getKey();

}
