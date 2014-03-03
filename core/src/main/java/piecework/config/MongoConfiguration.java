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
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import piecework.model.ProcessInstance;
import piecework.repository.concrete.EmbeddedMongoInstance;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author James Renfro
 */
@Configuration
@EnableMongoRepositories(basePackages="piecework.repository",repositoryImplementationPostfix="CustomImpl")
public class MongoConfiguration extends AbstractMongoConfiguration {

	private static final Logger LOG = Logger.getLogger(MongoConfiguration.class);

    @Autowired
    Environment environment;

    private EmbeddedMongoInstance mongoInstance;

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoDbFactory(), mappingMongoConverter());
    }

    @Bean
    @Primary
    public SimpleMongoDbFactory mongoDbFactory() throws Exception {

        UserCredentials credentials = getUserCredentials();

        if (credentials == null) {
            return new SimpleMongoDbFactory(mongo(), getDatabaseName());
        } else {
            return new SimpleMongoDbFactory(mongo(), getDatabaseName(), credentials);
        }
    }

	@Bean
    @Primary
    public Mongo mongo() throws Exception {
        if (environment.acceptsProfiles("embedded-mongo")) {
            mongoInstance = embeddedMongo();
            mongoInstance.startEmbeddedMongo();
            mongoInstance.importData();
            return new MongoClient(Collections.singletonList(new ServerAddress("127.0.0.1", 37017)));
        }
        MongoClientOptions.Builder optionsBuilder = new MongoClientOptions.Builder();
        if (environment.getProperty("mongo.use.ssl", Boolean.class, Boolean.FALSE))
            optionsBuilder.socketFactory(SSLSocketFactory.getDefault());

        return new MongoClient(getServerAddresses(), optionsBuilder.build());
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
		List<ServerAddress> serverAddresses = new LinkedList<ServerAddress>();
        String mongoServerAddresses = environment.getProperty("mongo.server.addresses", "127.0.0.1:27017");
		String[] addresses = mongoServerAddresses.split(",");
		for (String address : addresses) {
			String ip = null;
			int port = 27017;
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
