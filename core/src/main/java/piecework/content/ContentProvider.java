package piecework.content;

import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.model.Process;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentProvider {

    Content findByPath(Process process, String base, String location, Entity principal) throws IOException;

    Scheme getScheme();

    String getKey();

}
