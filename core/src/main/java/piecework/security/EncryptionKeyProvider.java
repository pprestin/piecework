package piecework.security;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

/**
 * @author James Renfro
 */
public interface EncryptionKeyProvider {

    SecretKeyRing getEncryptionKeyRing(String processDefinitionKey, String processInstanceId) throws GeneralSecurityException;

    SecretKey getDecryptionKey(String keyName) throws GeneralSecurityException;

}
