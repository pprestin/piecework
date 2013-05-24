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
package piecework.config;

import com.google.common.io.Files;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import piecework.persistence.EmbeddedMongoInstance;

import java.io.File;

/**
 * @author James Renfro
 */
@Configuration
@Profile("embedded-mongo")
public class EmbeddedMongoConfiguration {

    @Autowired
    Environment environment;

	@Bean
	public EmbeddedMongoInstance mongoInstance() {
        String mongoDb = environment.getProperty("mongo.db");
        String mongoUsername = environment.getProperty("mongo.username");
        String mongoPassword = environment.getProperty("mongo.password");
        String mongoFilesystem = environment.getProperty("mongo.filesystem");

        if (StringUtils.isEmpty(mongoFilesystem)) {
            File temporaryDirectory = Files.createTempDir();
            mongoFilesystem = temporaryDirectory.getAbsolutePath();
        }

        return new EmbeddedMongoInstance("127.0.0.1", 37017, mongoDb, mongoUsername, mongoPassword, mongoFilesystem);
	}

}
