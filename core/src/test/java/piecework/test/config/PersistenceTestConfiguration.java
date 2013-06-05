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
package piecework.test.config;

import org.springframework.context.annotation.*;
import piecework.config.MongoConfiguration;
import piecework.persistence.ContentRepository;
import piecework.persistence.concrete.GridFSContentRepository;

/**
 * @author James Renfro
 */
@Configuration
@Profile("test")
@Import({EmbeddedMongoConfiguration.class, MongoConfiguration.class})
@PropertySource("classpath:META-INF/mongo.test.properties")
public class PersistenceTestConfiguration {

    @Bean
    public ContentRepository contentRepository() {
        return new GridFSContentRepository();
    }

}
