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

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.model.Secret;
import piecework.security.EncryptionService;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Date;

/**
 * This implementation is provided purely for the purposes of demonstration and is
 * not intended to be used for production releases.
 *
 * @author James Renfro
 */
public class ExampleBouncyCastleEncryptionService extends BaseEncryptionService {

    @Autowired
    Environment environment;

    private SecretKey secretKey;
    private SecureRandom random;

    @Override
    public Secret encrypt(String text) throws InvalidCipherTextException, UnsupportedEncodingException, GeneralSecurityException {
        String encryptionKeyName = environment.getProperty("encryption.key.name");
        BufferedBlockCipher cipher = cipher();

        byte[] key = secretKey.getEncoded();
        byte[] iv = new byte[cipher.getBlockSize()];

        // Generate a random initialization vector for this encryption
        random.nextBytes(iv);

        cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] clear = text.getBytes("UTF-8");
        byte[] hidden = new byte[cipher.getOutputSize(clear.length)];
        int bytesProcessed = cipher.processBytes(clear, 0, clear.length, hidden, 0);
        bytesProcessed += cipher.doFinal(hidden, bytesProcessed);

        if (bytesProcessed != hidden.length)
            throw new GeneralSecurityException("Unable to correctly encrypt input data");

        return new Secret.Builder().name(encryptionKeyName).date(new Date()).ciphertext(hidden).iv(iv).build();
    }

    @Override
    public String decrypt(Secret secret) throws InvalidCipherTextException, GeneralSecurityException, UnsupportedEncodingException {
        BufferedBlockCipher cipher = cipher();
        byte[] key = secretKey.getEncoded();
        byte[] iv = secret.getIv();
        cipher.init(false, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] hidden = secret.getCiphertext();
        byte[] temporary;

        temporary = new byte[cipher.getOutputSize(hidden.length)];
        int bytesProcessed = cipher.processBytes(hidden, 0, hidden.length, temporary, 0);
        bytesProcessed += cipher.doFinal(temporary, bytesProcessed);

        byte[] clear = new byte[bytesProcessed];

        System.arraycopy(temporary, 0, clear, 0, bytesProcessed);

        return new String(clear, "UTF-8");
    }

    @PostConstruct
    public void init() throws GeneralSecurityException {
        String encryptionFactoryAlgorithm = environment.getProperty("encryption.factory.algorithm");
        String encryptionPseudoRandomGenerator = environment.getProperty("encryption.pseudorandom.generator");
        String encryptionKeyAlgorithm = environment.getProperty("encryption.key.algorithm");
        String encryptionKeyValue = environment.getProperty("encryption.key.value");
        int encryptionKeySize = environment.getProperty("encryption.key.size", Integer.class, 256);

        byte[] salt = new byte[8];
        random = SecureRandom.getInstance(encryptionPseudoRandomGenerator);
        random.nextBytes(salt);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(encryptionFactoryAlgorithm);
        int iterationCount = random.nextInt(3001) + 1000;
        KeySpec spec = new PBEKeySpec(encryptionKeyValue.toCharArray(), salt, iterationCount, encryptionKeySize);
        SecretKey tmp = factory.generateSecret(spec);
        secretKey = new SecretKeySpec(tmp.getEncoded(), encryptionKeyAlgorithm);
    }

    private BufferedBlockCipher cipher() {
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        cipher.reset();
        return cipher;
    }

}
