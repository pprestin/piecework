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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
public class ExampleEncryptionService extends BaseEncryptionService {

    @Autowired
    Environment environment;

    private SecretKey secretKey;
    private SecureRandom random;

    @Override
    public Secret encrypt(String text) throws GeneralSecurityException, UnsupportedEncodingException {
        String encryptionKeyName = environment.getProperty("encryption.key.name");
        String encryptionCipherAlgorithm = environment.getProperty("encryption.cipher.algorithm");

        Cipher cipher = Cipher.getInstance(encryptionCipherAlgorithm);
        byte[] iv = new byte[cipher.getBlockSize()];
        // Generate a random initialization vector for this encryption
        random.nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        AlgorithmParameters params = cipher.getParameters();
        byte[] ciphertext = cipher.doFinal(text.getBytes("UTF-8"));

        return new Secret.Builder().name(encryptionKeyName).date(new Date()).ciphertext(ciphertext).iv(iv).build();
    }

    @Override
    public String decrypt(Secret secret) throws GeneralSecurityException, UnsupportedEncodingException {
        String encryptionCipherAlgorithm = environment.getProperty("encryption.cipher.algorithm");

        Cipher cipher = Cipher.getInstance(encryptionCipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(secret.getIv()));
        return new String(cipher.doFinal(secret.getCiphertext()));
    }

    @PostConstruct
    public void init() throws GeneralSecurityException {
        String encryptionFactoryAlgorithm = environment.getProperty("encryption.factory.algorithm");
        String encryptionPseudoRandomGenerator = environment.getProperty("encryption.pseudorandom.generator");
        String encryptionKeyAlgorithm = environment.getProperty("encryption.key.algorithm");
        String encryptionKeyValue = environment.getProperty("encryption.key.value");
        int encryptionKeySize = environment.getProperty("encryption.key.size", Integer.class, 128);

        byte[] salt = new byte[20];
        random = SecureRandom.getInstance(encryptionPseudoRandomGenerator);
        random.nextBytes(salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(encryptionFactoryAlgorithm);
        int iterationCount = random.nextInt(3001) + 1000;
        KeySpec spec = new PBEKeySpec(encryptionKeyValue.toCharArray(), salt, iterationCount, encryptionKeySize);

        SecretKey tmp = factory.generateSecret(spec);
        secretKey = new SecretKeySpec(tmp.getEncoded(), encryptionKeyAlgorithm);
    }

//    @PostConstruct
//    public void init() throws GeneralSecurityException {
//        byte[] salt = new byte[32];
//        SecureRandom random = SecureRandom.getInstance(encryptionPseudoRandomGenerator);
//        random.nextBytes(salt);
//        SecretKeyFactory factory = SecretKeyFactory.getInstance(encryptionFactoryAlgorithm);
//        int iterationCount = random.nextInt(3001) + 1000;
//        KeySpec spec = new PBEKeySpec(encryptionKeyValue.toCharArray(), salt, iterationCount, encryptionKeySize);
//        SecretKey tmp = factory.generateSecret(spec);
//        secretKey = new SecretKeySpec(tmp.getEncoded(), encryptionKeyAlgorithm);
//    }

}