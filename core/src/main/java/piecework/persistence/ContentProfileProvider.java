package piecework.persistence;

import piecework.content.ContentPolicy;
import piecework.exception.PieceworkException;
import piecework.model.ContentProfile;

/**
 * @author James Renfro
 */
public interface ContentProfileProvider extends ProcessProvider {

    ContentProfile contentProfile() throws PieceworkException;

}
