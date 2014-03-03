package piecework.notification;

import piecework.model.User;

import java.util.List;

/**
 * @author James Renfro
 */
public interface EmailDispatcher {

    boolean dispatch(String senderEmail, String senderName, List<User> recipients, List<User> bcc, String subject, String body);

}
