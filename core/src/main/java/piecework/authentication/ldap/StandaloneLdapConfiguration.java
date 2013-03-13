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
package piecework.authentication.ldap;

import java.io.File;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 * @author James Renfro
 */
@Configuration
@Profile("standalone")
public class StandaloneLdapConfiguration {

	private final static String ROOT = "o=example";
	private final static String LDIFS = "classpath:META-INF/piecework/ldif.properties";
	
	@Value("10389")
	private int port;
	
	@Bean 
	public ApacheDSContainer directoryServer() throws Exception {
		File workingDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "piecework-standalone-directory");
				
		ApacheDSContainer container = new ApacheDSContainer(ROOT, LDIFS);
		container.setPort(port);
		container.setWorkingDirectory(workingDirectory);
		
		return container;
	}
		
//	@PostConstruct
//	public void startDirectoryServer() throws Exception {
//		directoryServer().start();
//	}

	@PreDestroy
	public void stopDirectoryServer() throws Exception {
		directoryServer().stop();
	}
	
}
