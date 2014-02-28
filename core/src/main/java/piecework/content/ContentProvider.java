package piecework.content;

import piecework.enumeration.Scheme;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentProvider {

    Content findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException;

    Scheme getScheme();

    String getKey();

}
