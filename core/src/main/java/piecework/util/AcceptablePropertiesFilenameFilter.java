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
package piecework.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.core.env.Environment;

/**
 * @author James Renfro
 */
public class AcceptablePropertiesFilenameFilter implements FilenameFilter {

	private static final Set<String> VALID_EXTENSIONS;
	
	private static final Logger LOG = Logger.getLogger(AcceptablePropertiesFilenameFilter.class);
	
	static {
		VALID_EXTENSIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("properties")));
	}
	
	private Environment environment;
	
	public AcceptablePropertiesFilenameFilter(Environment environment) {
		this.environment = environment;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		
		int periodIndex = name.lastIndexOf('.');
		int dashIndex = name.lastIndexOf('-');
		
		if (periodIndex != -1 && dashIndex != -1 && dashIndex < periodIndex) {
			String profile = name.substring(dashIndex+1, periodIndex);
			
			// Exclude if the profile provided is not 
			if (! environment.acceptsProfiles(profile)) {
				if (LOG.isDebugEnabled())
					LOG.debug("Excluding " + name + " from consideration since " + profile + " is not an active profile");
				return false;
			}
		}
		
		if (periodIndex != -1 && periodIndex > 1) {
			String extension = name.substring(periodIndex+1);
			
			if (VALID_EXTENSIONS.contains(extension))
				return true;
			else if (LOG.isDebugEnabled())
				LOG.debug("Excluding " + name + " from consideration since " + extension + " is not a valid extension");
		}
		
		return false;
	}

}
