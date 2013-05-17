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

import org.apache.log4j.Logger;
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

	private static final Logger LOG = Logger.getLogger(MongoConfiguration.class);
		
	@Value("${mongo.server.addresses}")
	private String mongoServerAddresses;
	
	@Value("${mongo.db}")
	private String mongoDb;

	@Value("${mongo.username}")
	private String mongoUsername;
	
	@Value("${mongo.password}")
	private String mongoPassword;
	
	@Bean
	public MongoTemplate mongoTemplate(Environment env) throws UnknownHostException {
		Mongo mongo = new Mongo(getServerAddresses());
		UserCredentials credentials = new UserCredentials(mongoUsername, mongoPassword);
		
		return new MongoTemplate(new SimpleMongoDbFactory(mongo, mongoDb, credentials));
	}
	
	private List<ServerAddress> getServerAddresses() throws UnknownHostException {
		List<ServerAddress> serverAddresses = new LinkedList<ServerAddress>();
		String[] addresses = mongoServerAddresses.split(",");
		for (String address : addresses) {
			String ip = null;
			int port = 37017;
			String[] tokens = address.split(":");
			if (tokens.length > 0) {
				ip = tokens[0];
				if (tokens.length > 1) {
					port = Integer.parseInt(tokens[1]);
				}
			}
			serverAddresses.add(new ServerAddress(ip, port));
		}
		return serverAddresses;
	}
	
}
