/*
 * Copyright 2013 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.security.concrete;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.codec.Base64;
import piecework.security.EncryptionKeyProvider;
import piecework.security.SecretKeyRing;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Example of an encryption key provider implementation that retrieves keys from
 * properties using the following convention and initializes maps to store them.
 * This provider would need to be provided to the Spring Application Context as
 * a bean in order to be found and autowired into the example encryption service
 * provided.
 *
 * encryption.keys.active = KEYNAME3:BASE64ENCODEDENCRYPTIONKEY3,KEYNAME4:BASE64ENCODEDENCRYPTIONKEY4,...
 * encryption.keys.historic = KEYNAME1:BASE64ENCODEDENCRYPTIONKEY1,KEYNAME2:BASE64ENCODEDENCRYPTIONKEY2,...
 *
 * @author James Renfro
 */
public class DefaultEncryptionKeyProvider implements EncryptionKeyProvider {

    private static final Logger LOG = Logger.getLogger(DefaultEncryptionKeyProvider.class);

    @Autowired
    Environment environment;

    private final List<String> encryptKeyNames = new ArrayList<String>();
    private final Map<String, SecretKey> encryptKeyMap = new LinkedHashMap<String, SecretKey>();
    private final Map<String, SecretKey> decryptKeyMap = new LinkedHashMap<String, SecretKey>();
    private SecureRandom random;

    @PostConstruct
    public void init() throws GeneralSecurityException, UnsupportedEncodingException {
        String encryptionPseudoRandomGenerator = environment.getProperty("encryption.pseudorandom.generator");
        if (StringUtils.isNotEmpty(encryptionPseudoRandomGenerator))
            this.random = SecureRandom.getInstance(encryptionPseudoRandomGenerator);
        else
            this.random = new SecureRandom();

        String seed = environment.getProperty("encryption.key.seed");
        if (StringUtils.isNotEmpty(seed))
            this.random.setSeed(Base64.decode(seed.getBytes("UTF-8")));

        // These are keys that will be used to encrypt new data
        String encryptionKeysActive = environment.getProperty("encryption.keys.active");
        // These are keys that will be consulted when decrypting data -- allowing keys
        // to be deactivated for future use without having to migrate all existing data
        // to one of the newer keys
        String encryptionKeysHistoric = environment.getProperty("encryption.keys.historic");

        Map<String, SecretKey> activeKeyMap = parseKeys(encryptionKeysActive);
        encryptKeyNames.addAll(activeKeyMap.keySet());
        encryptKeyMap.putAll(activeKeyMap);

        Map<String, SecretKey> historicKeyMap = parseKeys(encryptionKeysHistoric);
        decryptKeyMap.putAll(activeKeyMap);
        decryptKeyMap.putAll(historicKeyMap);
    }

    @Override
    public SecretKeyRing getEncryptionKeyRing(String processDefinitionKey, String processInstanceId) throws GeneralSecurityException {
        String encryptionKeyName = null;

        // If encryption key name is not provided, then
        if (StringUtils.isEmpty(encryptionKeyName)) {
            int numberOfEncryptionKeys = encryptKeyNames.size();
            if (numberOfEncryptionKeys <= 0)
                throw new GeneralSecurityException("No encryption keys provided");

            int index = numberOfEncryptionKeys == 1 ? 0 : random.nextInt(numberOfEncryptionKeys);
            encryptionKeyName = encryptKeyNames.get(index);
        }

        SecretKey secretKey = encryptKeyMap.get(encryptionKeyName);
        if (secretKey == null)
            throw new GeneralSecurityException("Misconfigured - encryption key with no secret key in map");

        return new SecretKeyRing(encryptionKeyName, secretKey);
    }

    @Override
    public SecretKey getDecryptionKey(String encryptionKeyName) throws GeneralSecurityException {
        SecretKey secretKey = decryptKeyMap.get(encryptionKeyName);
        if (secretKey == null)
            throw new GeneralSecurityException("Misconfigured - decryption key with no secret key in map");

        return secretKey;
    }

    private Map<String, SecretKey> parseKeys(String keyPairList) {
        String encryptionKeyAlgorithm = environment.getProperty("encryption.key.algorithm", "AES");
        Map<String, SecretKey> map = new LinkedHashMap<String, SecretKey>();
        if (StringUtils.isNotEmpty(keyPairList)) {
            String[] keyPairs = keyPairList.split(",");
            for (String keyPair : keyPairs) {
                String[] tuple = keyPair.split(":");
                if (tuple != null && tuple.length == 2) {
                    String name = tuple[0];
                    String value = tuple[1];
                    if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                        byte[] keyBytes = org.bouncycastle.util.encoders.Base64.decode(value);
                        SecretKey secretKey = new SecretKeySpec(keyBytes, encryptionKeyAlgorithm);
                        map.put(name, secretKey);
                        LOG.debug("Parsed secret key for " + name);
                    }
                }
            }
        }
        return map;
    }
}
