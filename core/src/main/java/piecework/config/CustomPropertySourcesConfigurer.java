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

import org.apache.log4j.Logger;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;
import piecework.util.AcceptablePropertiesFilenameFilter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author James Renfro
 */
public class CustomPropertySourcesConfigurer extends PropertySourcesPlaceholderConfigurer {

    private static final Logger LOG = Logger.getLogger(CustomPropertySourcesConfigurer.class);

    public CustomPropertySourcesConfigurer() {
        super();
    }

    public void setCustomLocations(Environment environment) {
        List<Resource> resources = new ArrayList<Resource>();
        resources.add(new ClassPathResource("META-INF/piecework/default.properties"));

//        propertySources.addFirst(new ResourcePropertySource(new ClassPathResource("META-INF/piecework/default.properties")));

        // Check if there is a system property pointing to another config location
        String location = System.getProperty("piecework.config.location");

        if (location == null)
            location = "/etc/piecework";

        File configDirectory = new File(location);

        // Check to make sure the location actually exists
        if (configDirectory.exists()) {
            if (configDirectory.isFile()) {
                // If the location that was passed in is a file, then go ahead and use it as the single properties file
//                propertySources.addFirst(new ResourcePropertySource(new FileSystemResource(configDirectory)));
                resources.add(new FileSystemResource(configDirectory));
            } else {
                // Otherwise, read all of the properties files, and exclude ones that have a -dev, -test, etc... if those are not active profiles
                File[] files = configDirectory.listFiles(new AcceptablePropertiesFilenameFilter(environment));
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isFile())
//                            propertySources.addFirst(new ResourcePropertySource(new FileSystemResource(file)));
                            resources.add(new FileSystemResource(file));
                        else if (LOG.isDebugEnabled())
                            LOG.debug("Cannot read configuration file " + file.getAbsolutePath() + " because it's actually a directory");
                    }
                }
            }
        } else {
            LOG.warn("No configuration properties found at " + location);
        }

        setLocations(resources.toArray(new Resource[resources.size()]));

        // Unfortunately, because spring has made the Environment-based @PropertySource annotation independent of the @Value stuff,
        // we have to wire things up here twice
        for (Resource resource : resources) {
            ConfigurableEnvironment configurableEnvironment = ConfigurableEnvironment.class.cast(environment);
            URL resourceUrl = null;
            try {
                resourceUrl = resource.getURL();
                configurableEnvironment.getPropertySources().addFirst(new ResourcePropertySource(resource));
            } catch (IOException ioe) {
                LOG.error("Failed to add the following resource to the list of property sources: " + resourceUrl.toString());
            }
        }
    }


}
