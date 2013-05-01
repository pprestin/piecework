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

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

/**
 * @author James Renfro
 */
@Configuration
public class MongoConfiguration {

	@Autowired 
	Environment env;
	
	@Value("${mongo.size}")
	private int mongoNumberOfInstances;
	
	@Value("${mongo.db}")
	private String mongoDb;

	@Value("${mongo.username}")
	private String mongoUsername;
	
	@Value("${mongo.password}")
	private String mongoPassword;
	
	@Bean
	public MongoTemplate mongoTemplate() throws UnknownHostException {

		List<ServerAddress> serverAddresses = new LinkedList<ServerAddress>();
		for (int i=1;i<=mongoNumberOfInstances;i++) {
			String mongoBindIp = env.getProperty("mongo.bindip." + i);
			int mongoPort = env.getProperty("mongo.port." + i, Integer.class, Integer.valueOf(27017)).intValue();
			
			serverAddresses.add(new ServerAddress(mongoBindIp, mongoPort));
		}

		Mongo mongo = new Mongo(serverAddresses);
		UserCredentials credentials = new UserCredentials(mongoUsername, mongoPassword);
		
		return new MongoTemplate(new SimpleMongoDbFactory(mongo, mongoDb, credentials));
	}
	
}
