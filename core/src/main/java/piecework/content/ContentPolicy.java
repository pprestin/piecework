package piecework.content;

import piecework.enumeration.Scheme;

/**
 * @author James Renfro
 */
public interface ContentPolicy {

    boolean isAcceptable(Scheme scheme, String contentKey, String location);

}
