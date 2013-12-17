package piecework.content;

import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.model.Process;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentProvider {

    Content findByPath(Process process, String location) throws IOException;

    Scheme getScheme();

}
