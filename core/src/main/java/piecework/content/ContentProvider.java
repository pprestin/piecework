package piecework.content;

import piecework.enumeration.Scheme;
import piecework.exception.PieceworkException;
import piecework.persistence.ContentProfileProvider;

/**
 * @author James Renfro
 */
public interface ContentProvider {

    ContentResource findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException;

    Scheme getScheme();

    String getKey();

}
