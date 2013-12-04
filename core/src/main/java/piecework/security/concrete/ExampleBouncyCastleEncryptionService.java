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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import piecework.common.UuidGenerator;
import piecework.model.Secret;
import piecework.security.EncryptionKeyProvider;
import piecework.security.SecretKeyRing;

import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;

/**
 * This implementation is provided purely for the purposes of demonstration and is
 * not intended to be used for production releases.
 *
 * @author James Renfro
 */
public class ExampleBouncyCastleEncryptionService extends BaseEncryptionService {

    private static final Logger LOG = Logger.getLogger(ExampleBouncyCastleEncryptionService.class);

    @Autowired
    Environment environment;

    @Autowired
    EncryptionKeyProvider keyProvider;

    @Autowired
    UuidGenerator uuidGenerator;

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
    }

    @Override
    public Secret encrypt(String text) throws InvalidCipherTextException, UnsupportedEncodingException, GeneralSecurityException {
        BufferedBlockCipher cipher = cipher();

        SecretKeyRing secretKeyRing = keyProvider.getEncryptionKeyRing(null, null);

        byte[] key = secretKeyRing.getSecretKey().getEncoded();
        byte[] iv = new byte[cipher.getBlockSize()];

        // Generate a random initialization vector for this encryption
        random.nextBytes(iv);

        cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] clear = text.getBytes("UTF-8");
        int outputSize = cipher.getOutputSize(clear.length);
        byte[] hidden = new byte[outputSize];
        int bytesProcessed = cipher.processBytes(clear, 0, clear.length, hidden, 0);
        bytesProcessed += cipher.doFinal(hidden, bytesProcessed);

        if (bytesProcessed != hidden.length)
            throw new GeneralSecurityException("Unable to correctly encrypt input data");

        return new Secret.Builder()
                .id(uuidGenerator.getNextId())
                .name(secretKeyRing.getKeyName())
                .date(new Date())
                .ciphertext(Base64.encode(hidden))
                .iv(Base64.encode(iv)).build();
    }

    @Override
    public String decrypt(Secret secret) throws InvalidCipherTextException, GeneralSecurityException, UnsupportedEncodingException {
        SecretKey secretKey = keyProvider.getDecryptionKey(secret.getName());

        BufferedBlockCipher cipher = cipher();
        byte[] key = secretKey.getEncoded();
        byte[] iv = Base64.decode(secret.getIv());
        cipher.init(false, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] hidden = Base64.decode(secret.getCiphertext());
        byte[] temporary;

        int outputLength = cipher.getOutputSize(hidden.length);
        temporary = new byte[outputLength];
        int bytesProcessed = cipher.processBytes(hidden, 0, hidden.length, temporary, 0);
        bytesProcessed += cipher.doFinal(temporary, bytesProcessed);

        byte[] clear = new byte[bytesProcessed];

        System.arraycopy(temporary, 0, clear, 0, bytesProcessed);

        return new String(clear, "UTF-8");
    }

    @Override
    public String generateKey(int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
        LOG.info("Generating a new encryption key of size " + keySize);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keySize);
        SecretKey secretKey = keyGen.generateKey();
        return new String(Base64.encode(secretKey.getEncoded()), "UTF-8");
    }

    private BufferedBlockCipher cipher() {
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        cipher.reset();
        return cipher;
    }

}
