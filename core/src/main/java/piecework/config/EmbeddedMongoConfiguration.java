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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import piecework.persistence.EmbeddedMongoInstance;

/**
 * @author James Renfro
 */
@Configuration
@Profile("embedded-mongo")
public class EmbeddedMongoConfiguration {

	@Value("${mongo.db}")
	private String mongoDb;
	
	@Value("${mongo.username}")
	private String mongoUsername;
	
	@Value("${mongo.password}")
	private String mongoPassword;
	
	@Value("${mongo.filesystem}")
	private String mongoFilesystem;
		
	@Bean
	public EmbeddedMongoInstance mongoInstance() {
		return new EmbeddedMongoInstance("127.0.0.1", 37017, mongoDb, mongoUsername, mongoPassword, mongoFilesystem);
	}
}
