package piecework.content;

import piecework.enumeration.ContentHandlerPriority;

/**
 * Similar to the ContentProviderVoter, if this interface is implemented it will
 * be injected into the ContentHandlerRepository, which will use it to make
 * decisions about which ContentReceiver to make primary, backup, or ignored.
 *
 * @author James Renfro
 */
public interface ContentReceiverVoter {

    <R extends ContentReceiver> ContentHandlerPriority vote(R receiver);

}
