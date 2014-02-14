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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.model.*;
import piecework.security.AccessTracker;
import piecework.security.EncryptionService;
import piecework.security.config.DataFilterTestConfiguration;
import piecework.settings.UserInterfaceSettings;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DataFilterTestConfiguration.class})
@ActiveProfiles("test")
public class DecorateValuesFilterTest {

    @Autowired
    UserInterfaceSettings settings;

    private DecorateValuesFilter filter;

    @Before
    public void setup() {
        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("1234")
                .build();
        Entity principal = new User.Builder()
                .userId("testuser")
                .build();
        Set<Field> fields = new HashSet<Field>();

        this.filter = new DecorateValuesFilter(instance, fields, settings, principal, "v0");
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

}
