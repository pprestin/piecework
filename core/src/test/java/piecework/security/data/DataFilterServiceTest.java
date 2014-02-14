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

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.common.ManyMap;
import piecework.model.AccessEvent;
import piecework.model.ProcessInstance;
import piecework.model.User;
import piecework.model.Value;
import piecework.persistence.AccessEventRepository;
import piecework.security.EncryptionService;
import piecework.security.config.DataFilterTestConfiguration;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DataFilterTestConfiguration.class})
@ActiveProfiles("test")
public class DataFilterServiceTest {

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    AccessEventRepository mockAccessEventRepository;

    @Test
    public void applicationContext() {
        Assert.assertTrue(true);
    }

    @Test
    public void allInstanceDataDecryptedNoRestricted() {
        reset(mockAccessEventRepository);
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-key-1", new User.Builder()
            .userId("test-value-1")
            .displayName("Joe Test")
            .build());
        original.putOne("test-key-2", new Value("test-value-2"));
        original.putOne("test-key-3", new Value("test-value-3"));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("123")
                .data(original)
                .build();

        Map<String, List<Value>> data = dataFilterService.allInstanceDataDecrypted(instance, "testing");
        Assert.assertEquals(original, data);
        Assert.assertEquals(3, data.size());
        verify(mockAccessEventRepository, never()).save(any(AccessEvent.class));
    }

    @Test
    public void allInstanceDataDecryptedOneRestricted() throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        reset(mockAccessEventRepository);
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-key-1", new User.Builder()
                .userId("test-value-1")
                .displayName("Joe Test")
                .build());
        original.putOne("test-key-2", new Value("test-value-2"));
        original.putOne("test-key-3", encryptionService.encrypt("test-value-3"));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("123")
                .data(original)
                .build();

        Map<String, List<Value>> data = dataFilterService.allInstanceDataDecrypted(instance, "testing");
        Assert.assertEquals(3, data.size());
        Assert.assertEquals("test-value-3", data.get("test-key-3").iterator().next().toString());
        verify(mockAccessEventRepository, times(1)).save(any(AccessEvent.class));
    }



}
