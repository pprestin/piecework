package piecework.repository;

import piecework.content.ContentResource;
import piecework.exception.PieceworkException;
import piecework.persistence.ContentProfileProvider;

import java.io.IOException;

/**
 * @author James Renfro
 */
public interface ContentRepository {

    /*
     * Retrieves content from a location and marks it as 'checked out' so it cannot be edited until it is
     * checked back in again. This location can include a prefix to indicate the
     * appropriate scheme, such as file: or classpath:, but in the case where a scheme is
     * provided, a ContentProvider to handle that scheme must also be included in the application
     * context or an exception will be thrown.
     */
    ContentResource checkoutByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException;

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
     * Releases checked out content
     */
    boolean releaseByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException;

    /*
     * Replaces an existing content resource, and assuming that the underlying provider is able to handle it, creates
     * a new version
     */
    ContentResource replace(ContentProfileProvider modelProvider, ContentResource contentResource, String location) throws PieceworkException, IOException;

    /*
     * Stores content specific to a process, or if the processDefinitionKey is left null,
     * in a general purpose location.
     */
    ContentResource save(ContentProfileProvider modelProvider, ContentResource contentResource) throws PieceworkException, IOException;

}
