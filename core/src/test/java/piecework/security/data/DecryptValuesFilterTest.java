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

import junit.framework.Assert;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AccessEventRepository;
import piecework.security.AccessTracker;
import piecework.security.EncryptionService;
import piecework.security.config.DataFilterTestConfiguration;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration Test of {@see DecryptValuesFilter}
 *
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DataFilterTestConfiguration.class})
@ActiveProfiles("test")
public class DecryptValuesFilterTest {

    @Autowired
    AccessTracker accessTracker;

    @Autowired
    AccessEventRepository mockAccessEventRepository;

    @Autowired
    EncryptionService encryptionService;

    private DecryptValuesFilter filter;

    @Before
    public void setup() {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();
        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("1234")
                .build();
        Entity principal = new User.Builder()
                .userId("testuser")
                .build();

        String reason = "testing";
        this.filter = new DecryptValuesFilter(process, instance, principal, reason, accessTracker, encryptionService, false);
    }

    @Test
    public void applicationContext() {
        Assert.assertTrue(true);
    }

    @Test
    public void filterNullValues() {
        List<Value> values =  filter.filter("test-key-1", null);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterEmptyValues() {
        List<Value> values =  filter.filter("test-key-1", Collections.<Value>emptyList());
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterUnrestrictedValues() {
        reset(mockAccessEventRepository);
        List<Value> original = new ArrayList<Value>();
        original.add(new Value("test-value-1"));
        original.add(new Value("test-value-2"));
        List<Value> values =  filter.filter("test-key-1", original);
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Iterator<Value> iterator = values.iterator();
        Assert.assertEquals("test-value-1", iterator.next().toString());
        Assert.assertEquals("test-value-2", iterator.next().toString());

        verify(mockAccessEventRepository, never()).save(any(AccessEvent.class));
    }

    @Test
    public void filterOneRestrictedOneUnrestrictedValues() throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        reset(mockAccessEventRepository);

        List<Value> original = new ArrayList<Value>();
        original.add(new Value("test-value-1"));
        original.add(encryptionService.encrypt("test-value-2"));
        List<Value> values =  filter.filter("test-key-1", original);
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Iterator<Value> iterator = values.iterator();
        Assert.assertEquals("test-value-1", iterator.next().toString());
        Assert.assertEquals("test-value-2", iterator.next().toString());

        verify(mockAccessEventRepository, times(1)).save(any(AccessEvent.class));
    }

    @Test
    public void filterMultipleRestrictedDifferentLengths() throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        reset(mockAccessEventRepository);

        List<Value> original = new ArrayList<Value>();
        original.add(encryptionService.encrypt("test-val-1"));
        original.add(encryptionService.encrypt("test-value-2"));
        original.add(encryptionService.encrypt("tv3"));
        List<Value> values =  filter.filter("test-key-1", original);
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Iterator<Value> iterator = values.iterator();
        Assert.assertEquals("test-val-1", iterator.next().toString());
        Assert.assertEquals("test-value-2", iterator.next().toString());
        Assert.assertEquals("tv3", iterator.next().toString());

        verify(mockAccessEventRepository, times(3)).save(any(AccessEvent.class));
    }



}
