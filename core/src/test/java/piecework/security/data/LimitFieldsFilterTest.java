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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.model.*;
import piecework.security.config.DataFilterTestConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Integration Test of {@see LimitFieldsFilter}
 *
 * @author James Renfro
 */
public class LimitFieldsFilterTest {

    @Test
    public void filterNullValuesIncludeFlagFalse() {
        LimitFieldsFilter filter = new LimitFieldsFilter(null, false);
        List<Value> values = filter.filter("test-key-1", null);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterEmptyValuesIncludeFlagFalse() {
        LimitFieldsFilter filter = new LimitFieldsFilter(null, false);
        List<Value> values = filter.filter("test-key-1", Collections.<Value>emptyList());
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterNoMatchingValuesIncludeFlagFalse() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
            .name("non-matching-key-0")
            .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, false);
        List<Value> values = filter.filter("test-key-1", null);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterOneMatchingValuesIncludeFlagFalse() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-key-1")
                .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, false);
        List<Value> values = filter.filter("test-key-1", Collections.singletonList(new Value("test-value-1")));
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("test-value-1", values.iterator().next().toString());
    }

    @Test
    public void filterOneMatchingRestrictedValuesIncludeFlagFalse() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-key-1")
                .restricted()
                .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, false);
        List<Value> values = filter.filter("test-key-1", Collections.singletonList(new Value("test-value-1")));
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterOneMatchingRestrictedOneNotValuesIncludeFlagFalse() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-key-1")
                .restricted()
                .build());
        fields.add(new Field.Builder()
                .name("test-key-2")
                .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, false);
        List<Value> values = filter.filter("test-key-2", Collections.singletonList(new Value("test-value-2")));
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("test-value-2", values.iterator().next().toString());
    }

    @Test
    public void filterNullValuesIncludeFlagTrue() {
        LimitFieldsFilter filter = new LimitFieldsFilter(null, false);
        List<Value> values = filter.filter("test-key-1", null);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterEmptyValuesIncludeFlagTrue() {
        LimitFieldsFilter filter = new LimitFieldsFilter(null, false);
        List<Value> values = filter.filter("test-key-1", Collections.<Value>emptyList());
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterNoMatchingValuesIncludeFlagTrue() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("non-matching-key-0")
                .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, true);
        List<Value> values = filter.filter("test-key-1", null);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void filterOneMatchingValuesIncludeFlagTrue() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-key-1")
                .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, true);
        List<Value> values = filter.filter("test-key-1", Collections.singletonList(new Value("test-value-1")));
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("test-value-1", values.iterator().next().toString());
    }

    @Test
    public void filterOneMatchingRestrictedValuesIncludeFlagTrue() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-key-1")
                .restricted()
                .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, true);
        List<Value> values = filter.filter("test-key-1", Collections.singletonList(new Value("test-value-1")));
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("test-value-1", values.iterator().next().toString());
    }

    @Test
    public void filterOneMatchingRestrictedOneNotValuesIncludeFlagTrue() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-key-1")
                .restricted()
                .build());
        fields.add(new Field.Builder()
                .name("test-key-2")
                .build());
        LimitFieldsFilter filter = new LimitFieldsFilter(fields, false);
        List<Value> values = filter.filter("test-key-2", Collections.singletonList(new Value("test-value-2")));
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("test-value-2", values.iterator().next().toString());
    }

}
