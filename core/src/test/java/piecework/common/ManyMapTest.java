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
package piecework.common;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ManyMapTest {

    @Test(expected = UnsupportedOperationException.class)
    public void verify() {
        ManyMap<String, String> map = new ManyMap<String, String>();
        map.putOne("TEST", "VALUE-1");
        Assert.assertEquals(1, map.get("TEST").size());
        map.putOne("TEST", "VALUE-2");
        Assert.assertEquals(2, map.get("TEST").size());
        Assert.assertEquals("VALUE-1", map.getOne("TEST"));

        Map<String, List<String>> unmodifiableMap = map.unmodifiableMap();
        unmodifiableMap.put("TEST", Collections.singletonList("VALUE-3"));
    }

}
