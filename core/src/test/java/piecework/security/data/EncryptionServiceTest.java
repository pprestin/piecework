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
package piecework.security.data;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.model.Secret;
import piecework.security.EncryptionService;
import piecework.security.config.EncryptionTestConfiguration;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={EncryptionTestConfiguration.class})
@ActiveProfiles("test")
public class EncryptionServiceTest {

    @Autowired
    EncryptionService encryptionService;

    @Test
    public void testEncryptAndDecrypt() throws Exception {

        Secret secret = encryptionService.encrypt("This is a simple test");

        Assert.assertNotSame("This is a simple test".getBytes(), secret.getCiphertext());

        String cleartext = encryptionService.decrypt(secret);

        Assert.assertEquals("This is a simple test", cleartext);
    }

}
