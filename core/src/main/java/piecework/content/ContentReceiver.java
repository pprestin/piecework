package piecework.content;

import piecework.exception.PieceworkException;
import piecework.persistence.ContentProfileProvider;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentReceiver {

    ContentResource checkout(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException;

    boolean expire(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException;

    ContentResource replace(ContentProfileProvider modelProvider, ContentResource contentResource, String location) throws PieceworkException, IOException;

    boolean release(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException;

    ContentResource save(ContentProfileProvider modelProvider, ContentResource contentResource) throws PieceworkException, IOException;

    String getKey();

}
