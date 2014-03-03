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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

/**
 * @author James Renfro
 */
@Configuration
@Profile("data")
public class DataConfiguration {

	private static final Logger LOG = Logger.getLogger(DataConfiguration.class);
			
	@Autowired
    @Qualifier(value="mongoTemplate")
	MongoTemplate mongoTemplate;

    @Autowired
    Environment environment;

	@PostConstruct
	public void importDatabase() throws UnknownHostException {
        String mongoDb = environment.getProperty("mongo.db");
        String mongoFilesystem = environment.getProperty("mongo.filesystem");
		
		File directory = new File(mongoFilesystem, "dbs");
		
		if (!directory.exists()) {
			LOG.debug("No startup data exists under path " + directory.getAbsolutePath());
			return;
		}
		
		File dbDirectory = new File(directory, mongoDb);
		
		if (!dbDirectory.exists()) {
			LOG.debug("No startup data exists for " + mongoDb);
			return;
		}

		File[] collectionFiles = dbDirectory.listFiles();
		
		for (File collectionFile : collectionFiles) {
            
			if (collectionFile.isFile() && collectionFile.exists()) {

				String collectionName = collectionFile.getName();

                LOG.debug("Loading collection '" + collectionName + "'...");

                DBCollection dbCollection = mongoTemplate.getCollection(collectionName);

                try {
	                LOG.debug("Loading JSON from file '" + collectionFile.getName() + "'");
	                importCollection(dbCollection, new FileInputStream(collectionFile));
                } catch (FileNotFoundException fnfe) {
                	LOG.warn("Unable to initialize local mongo with data from " + collectionFile.getAbsolutePath(), fnfe);
                }
			}
        }
	}
	
	private void importCollection(final DBCollection collection, final InputStream jsonStream) {
		@SuppressWarnings("unchecked")
		String entry = new Scanner(jsonStream).useDelimiter("\\A").next();
		final List<DBObject> list = (List<DBObject>) JSON.parse(entry);

		LOG.debug("Have " + list.size() + " objects to load...");

//		collection.insert(list);
		for (DBObject object : list) 
			collection.save(object);
	}
	
}
