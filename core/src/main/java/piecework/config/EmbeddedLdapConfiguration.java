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

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 * @author James Renfro
 */
@Configuration
@Profile({"embedded-ldap","ldap"})
public class EmbeddedLdapConfiguration {

	private final static String ROOT = "dc=springframework,dc=org";
	private final static String LDIF_LOCATION = "classpath*:META-INF/piecework/demo.ldif";
	private final static String TEST_LDIF_LOCATION = "classpath*:META-INF/piecework/test.ldif";
	
	@Value("33389")
	private int port;
	
	@Bean(destroyMethod="destroy")
	public ApacheDSContainer directoryServer(Environment env) throws Exception {
		
		File workingDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "piecework-standalone-directory");
		FileUtils.deleteDirectory(workingDirectory);

		String ldifs = LDIF_LOCATION;
		
		if (env.acceptsProfiles("test"))
			ldifs = TEST_LDIF_LOCATION;
		
		ApacheDSContainer container = new ApacheDSContainer(ROOT, ldifs);
		container.setPort(port);
		container.setWorkingDirectory(workingDirectory);
		
		return container;
	}
	
}
