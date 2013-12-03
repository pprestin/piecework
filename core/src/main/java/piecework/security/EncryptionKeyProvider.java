package piecework.security;

/**
 * @author James Renfro
 */
public interface EncryptionKeyProvider {

    String getCurrentKey(String processDefinitionKey, String processInstanceId);

    String getKey(String keyName);

}
