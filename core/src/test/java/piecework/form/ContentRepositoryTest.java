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
package piecework.form;

import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.MongodProcess;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.test.config.PersistenceTestConfiguration;
import piecework.test.config.UnitTestConfiguration;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={PersistenceTestConfiguration.class})
@ActiveProfiles("test")
public class ContentRepositoryTest {

    private MongodProcess mongod;
    private Mongo mongo;

    @Test
    public void testFindOne() throws Exception {

    }

    @Test
    public void testFindByLocation() throws Exception {

    }

    @Test
    public void testFindByLocationPattern() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }
}
