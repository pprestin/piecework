package piecework.persistence;

import piecework.model.Content;
import piecework.model.Process;

import java.io.IOException;
import java.util.List;

/**
 * @author James Renfro
 */
public interface ContentRepository {

    /*
     * Retrieves content from a full path location. This should only be called in cases where
     * the application is determining the location without incorporating any user input, since
     * creative use of ../ could conceivably allow an end-user to retrieve something they're
     * not supposed to see.
     */
    Content findByLocation(Process process, String location);

    /*
     * Retrieves content from a location relative to a base path -- only below that path, for
     * security reasons, since in some cases the location portion may be provided by the
     * end-user.
     *
     */
    Content findByLocation(Process process, String base, String location);

    /*
     * Stores content specific to a process, or if the processDefinitionKey is left null,
     * in a general purpose location.
     */
    Content save(Process process, Content content) throws IOException;

}
