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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Net;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Storage;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Timeout;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;

/**
 * 
 * @author Radu Banica
 */
public class EmbeddedMongoInstance {

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
		MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, new Net(bindIp, port, ipv6), new Storage(storePath, replSetName, oplogSize), new Timeout());
		//mongodConfig = new MongodConfig(Version.Main.V2_0, port, false, storePath);

		IDirectory artifactStorePath = new FixedPath(storePath);
		ITempNaming executableNaming = new UUIDTempNaming(); // new
																// UserTempNaming();
		RuntimeConfig runtimeConfig = new RuntimeConfig();
		runtimeConfig.getDownloadConfig().setArtifactStorePathNaming(
				artifactStorePath);
		runtimeConfig.setExecutableNaming(executableNaming);
		runtimeConfig.setProcessOutput(new ProcessOutput(Processors.logTo(java.util.logging.Logger.global,
						java.util.logging.Level.FINEST), Processors.logTo(java.util.logging.Logger.global,
						java.util.logging.Level.FINEST), Processors.logTo(java.util.logging.Logger.global,
						java.util.logging.Level.FINEST)));
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
		    
		} catch (IOException ex) {
			LOG.error("Failed to start embedded MongoDB on port " + port + ": " + ex.toString());
		}
	}

	@PreDestroy
	public void stopEmbeddedMongo() {
		if ((mongod != null) && (mongodExecutable != null)) {
			LOG.debug("Stopping embedded MongoDB...");
			mongod.stop();
			mongodExecutable.stop();
			LOG.debug("Embedded MongoDB processes stopped");
		} else {
			LOG.debug("No mongod process instance to stop");
		}
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