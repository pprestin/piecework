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
package piecework.ldap;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 * @author James Renfro
 */
public class DirectoryServerLauncher {

	private final static String ROOT = "dc=springframework,dc=org";
	private final static String LDIF_LOCATION = "classpath*:META-INF/piecework/demo.ldif";
	
	@Value("33389")
	private static int port;
	
	public static ApacheDSContainer launchDirectoryServer() throws Exception {
		
		File workingDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "piecework-standalone-directory");
		FileUtils.deleteDirectory(workingDirectory);

		String ldifs = LDIF_LOCATION;
		
		ApacheDSContainer container = new ApacheDSContainer(ROOT, ldifs);
		container.setPort(port);
		container.setWorkingDirectory(workingDirectory);
		
		return container;
	}

	public static void main(String[] args) throws Exception {
		launchDirectoryServer();
	}

}
