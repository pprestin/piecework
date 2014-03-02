package piecework.repository;

import piecework.exception.PieceworkException;
import piecework.content.ContentResource;
import piecework.persistence.ContentProfileProvider;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentRepository {

    /*
     * Retrieves content from a location. This location can include a prefix to indicate the
     * appropriate scheme, such as file: or classpath:, but in the case where a scheme is
     * provided, a ContentProvider to handle that scheme must also be included in the application
     * context or an exception will be thrown.
     */
    ContentResource findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException;

    /*
     * Expires a piece of content so it is no longer available to be found
     */
    boolean expireByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException;

    /*
     * Stores content specific to a process, or if the processDefinitionKey is left null,
     * in a general purpose location.
     */
    ContentResource save(ContentProfileProvider modelProvider, ContentResource contentResource) throws PieceworkException, IOException;

}
