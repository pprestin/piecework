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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Net;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Storage;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Timeout;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;

/**
 * 
 * @author Radu Banica
 */
public class EmbeddedMongoInstance implements DisposableBean {

	private static final Logger LOG = Logger.getLogger(EmbeddedMongoInstance.class);
	private MongodExecutable mongodExecutable;
	private MongodProcess mongod;
	private final String bindIp;
	private final int port;
	private final String dbName;
	private final String username;
	private final String password;
	private final String storePath;
	
	private boolean ipv6 = false;
	private String replSetName = null;
	private int oplogSize = 0;

	public EmbeddedMongoInstance(String bindIp, int port, String db, String username, String password, String storePath) {
		this.bindIp = bindIp;
		this.port = port;
		this.dbName = db;
		this.username = username;
		this.password = password;
		this.storePath = storePath;
	}
	
	/**
	 * Need to enable <context:annotation-config /> in spring configuration file
	 * 
	 * @throws IOException
	 */
	@PostConstruct
	public void startEmbeddedMongo() throws IOException {
		LOG.debug("Starting embedded Mongodb on port " + port + "...");

		String storageDirectory = storePath + File.separator + "storage";

        File databaseDirectory = new File(storageDirectory);
        databaseDirectory.deleteOnExit();

		MongodConfig mongodConfig = new MongodConfig(Version.Main.PRODUCTION, new Net(bindIp, port, ipv6), new Storage(storageDirectory, replSetName, oplogSize), new Timeout());
		IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).build();
		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

		mongodExecutable = null;
		mongod = null;

		try {
			mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();
			LOG.debug("Mongodb started");
			
			Mongo mongo = new Mongo(bindIp, port);
		    DB db = mongo.getDB(dbName);
		    db.addUser(username, password.toCharArray());
		    
//		    importData(db);
		    
		    mongo.close();
		    
		} catch (IOException ex) {
			LOG.error("Failed to start embedded MongoDB on port " + port + ": " + ex.toString());
		}
	}

	@PreDestroy
	public void destroy() {
		if ((mongod != null) && (mongodExecutable != null)) {
			LOG.debug("Stopping embedded MongoDB...");
			mongod.stop();
			mongodExecutable.stop();
			LOG.debug("Embedded MongoDB processes stopped");
		} else {
			LOG.debug("No mongod process instance to stop");
		}
	}
	
	private void importData(final DB db) {
		
		File directory = new File(storePath, "dbs");
		
		if (!directory.exists()) {
			LOG.debug("No startup data exists");
			return;
		}
		
		File dbDirectory = new File(directory, dbName);
		
		if (!dbDirectory.exists()) {
			LOG.debug("No startup data exists for " + dbName);
			return;
		}
		
		File[] collectionFiles = dbDirectory.listFiles();
		
		for (File collectionFile : collectionFiles) {
            
			if (collectionFile.isFile() && collectionFile.exists()) {

				String collectionName = collectionFile.getName();

                LOG.debug("Loading collection '" + collectionName + "'...");

                DBCollection dbCollection = db.getCollection(collectionName);

                try {
	                LOG.debug("Loading JSON from file '" + collectionFile.getName() + "'");
	                importCollection(dbCollection, new FileInputStream(collectionFile));
                } catch (FileNotFoundException fnfe) {
                	LOG.warn("Unable to initialize local mongo with data from " + collectionFile.getAbsolutePath(), fnfe);
                }
			}
        }
	}
	
	/**
	 * Imports data into a collection
	 * 
	 * @param collection
	 * @param jsonStream
	 */
	private void importCollection(final DBCollection collection, final InputStream jsonStream) {
		@SuppressWarnings("unchecked")
		final List<DBObject> list = (List<DBObject>) JSON.parse(new Scanner(jsonStream).useDelimiter("\\A").next());

		LOG.debug("Have " + list.size() + " obejcts to load...");

//		collection.insert(list);
		for (DBObject object : list) 
			collection.save(object);
	}

	public int getPort() {
		return port;
	}

	public String getStorePath() {
		return storePath;
	}

	public boolean isIpv6() {
		return ipv6;
	}

	public void setIpv6(boolean ipv6) {
		this.ipv6 = ipv6;
	}

	public String getReplSetName() {
		return replSetName;
	}

	public void setReplSetName(String replSetName) {
		this.replSetName = replSetName;
	}

	public int getOplogSize() {
		return oplogSize;
	}

	public void setOplogSize(int oplogSize) {
		this.oplogSize = oplogSize;
	}
}