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
package piecework.security;

import org.bouncycastle.crypto.InvalidCipherTextException;
import piecework.model.Secret;
import piecework.model.Value;
import piecework.util.ManyMap;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public interface EncryptionService {

    Secret encrypt(String text) throws InvalidCipherTextException, UnsupportedEncodingException, GeneralSecurityException;

    String decrypt(Secret secret) throws InvalidCipherTextException, GeneralSecurityException, UnsupportedEncodingException;

    ManyMap<String, Value> decrypt(Map<String, List<Value>> data);

    List<Value> decrypt(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException;

    ManyMap<String, Value> mask(Map<String, List<Value>> original);

}
