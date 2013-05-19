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
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import piecework.model.ProcessInstance;

/**
 * @author James Renfro
 */
@Configuration
public class MongoConfiguration extends AbstractMongoConfiguration {

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
    public Mongo mongo() throws Exception {
         return new Mongo(getServerAddresses());
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }

    @Override
    protected String getDatabaseName() {
        return mongoDb;
    }

    @Override
    protected String getMappingBasePackage() {
        return ProcessInstance.class.getPackage().getName();
    }

    @Override
    protected UserCredentials getUserCredentials() {
        return new UserCredentials(mongoUsername, mongoPassword);
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
