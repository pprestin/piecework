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

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.google.common.io.Files;
import com.mongodb.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import piecework.common.UuidGenerator;
import piecework.model.ProcessInstance;
import piecework.persistence.EmbeddedMongoInstance;
import piecework.util.SSLSocketFactoryWrapper;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author James Renfro
 */
@Configuration
@EnableMongoRepositories(basePackages="piecework.persistence")
public class MongoConfiguration extends AbstractMongoConfiguration {

	private static final Logger LOG = Logger.getLogger(MongoConfiguration.class);

    @Autowired
    Environment environment;

    @Autowired
    UuidGenerator uuidGenerator;

    private EmbeddedMongoInstance mongoInstance;
    private String databaseName;

	@Bean
    public Mongo mongo() throws Exception {
        if (environment.acceptsProfiles("embedded-mongo")) {
            mongoInstance = embeddedMongo();
            mongoInstance.startEmbeddedMongo();
            mongoInstance.importData();
        }
        MongoClientOptions options = new MongoClientOptions.Builder().socketFactory(SSLSocketFactory.getDefault()).build();
        return new MongoClient(getServerAddresses(), options);
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        String bucket = environment.getProperty("mongo.gridfs.bucket");
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter(), bucket);
    }

    @Override
    protected String getDatabaseName() {
        return environment.getProperty("mongo.db");
    }

    @Override
    protected String getMappingBasePackage() {
        return ProcessInstance.class.getPackage().getName();
    }

    @Override
    protected UserCredentials getUserCredentials() {
        String mongoUsername = environment.getProperty("mongo.username");
        String mongoPassword = environment.getProperty("mongo.password");
        return new UserCredentials(mongoUsername, mongoPassword);
    }

    private EmbeddedMongoInstance embeddedMongo() {
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

	private List<ServerAddress> getServerAddresses() throws UnknownHostException {
        String mongoServerAddresses = environment.getProperty("mongo.server.addresses");
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
