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

import org.springframework.beans.factory.annotation.Value;
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
public class ExampleEncryptionService implements EncryptionService {

    @Value("${encryption.cipher.algorithm}")
    private String encryptionCipherAlgorithm;

    @Value("${encryption.key.algorithm}")
    private String encryptionKeyAlgorithm;

    @Value("${encryption.factory.algorithm}")
    private String encryptionFactoryAlgorithm;

    @Value("${encryption.pseudorandom.generator}")
    private String encryptionPseudoRandomGenerator;

    @Value("${encryption.key.name}")
    private String encryptionKeyName;

    @Value("${encryption.key.value}")
    private String encryptionKeyValue;

    @Value("${encryption.key.size}")
    private int encryptionKeySize;

    private SecretKey secretKey;

    @Override
    public Secret encrypt(String text) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance(encryptionCipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = cipher.doFinal(text.getBytes("UTF-8"));
        return new Secret.Builder().name(encryptionKeyName).date(new Date()).ciphertext(ciphertext).iv(iv).build();
    }

    @Override
    public String decrypt(Secret secret) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance(encryptionCipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(secret.getIv()));
        return new String(cipher.doFinal(secret.getCiphertext()), "UTF-8");
    }

    @PostConstruct
    public void init() throws GeneralSecurityException {
        byte[] salt = new byte[32];
        SecureRandom random = SecureRandom.getInstance(encryptionPseudoRandomGenerator);
        random.nextBytes(salt);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(encryptionFactoryAlgorithm);
        int iterationCount = random.nextInt(3001) + 1000;
        KeySpec spec = new PBEKeySpec(encryptionKeyValue.toCharArray(), salt, iterationCount, encryptionKeySize);
        SecretKey tmp = factory.generateSecret(spec);
        secretKey = new SecretKeySpec(tmp.getEncoded(), encryptionKeyAlgorithm);
    }

}