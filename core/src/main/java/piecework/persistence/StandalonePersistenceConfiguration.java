/*
 * Copyright 2012 University of Washington
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
package piecework.persistence;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

/**
 * @author James Renfro
 */
@Configuration
@Profile("standalone")
public class StandalonePersistenceConfiguration {

	@Value("${mongo_db}")
	private String mongoDb;
	
	@Value("${mongo_bindip}")
	private String mongoBindIp;
	
	@Value("${mongo_port}")
	private int mongoPort;
	
	@Value("${mongo_username}")
	private String mongoUsername;
	
	@Value("${mongo_password}")
	private String mongoPassword;
	
	@Value("${mongo_filesystem}")
	private String mongoFilesystem;
	
	@Bean
	public MongoTemplate mongoTemplate() throws UnknownHostException {
		Mongo mongo = new Mongo(new ServerAddress(mongoBindIp, mongoPort)); 
		UserCredentials credentials = new UserCredentials(mongoUsername, mongoPassword);
		
		return new MongoTemplate(new SimpleMongoDbFactory(mongo, mongoDb, credentials));
	}
	
	@Bean
	public EmbeddedMongoInstance mongoInstance() {
		return new EmbeddedMongoInstance(mongoBindIp, mongoPort, mongoDb, mongoUsername, mongoPassword, mongoFilesystem);
	}
}
