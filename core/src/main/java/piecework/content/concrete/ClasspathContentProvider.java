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
package piecework.content.concrete;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import piecework.content.ContentProvider;
import piecework.content.ContentResource;
import piecework.enumeration.AlarmSeverity;
import piecework.enumeration.CacheName;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.ContentProfileProvider;
import piecework.security.AccessTracker;
import piecework.service.CacheService;
import piecework.util.ContentUtility;

/**
 * @author James Renfro
 */
@Service
public class ClasspathContentProvider implements ContentProvider {

    private static final Logger LOG = Logger.getLogger(ClasspathContentProvider.class);

    @Autowired
    AccessTracker accessTracker;

    @Override
    public ContentResource findByLocation(ContentProfileProvider modelProvider, String rawPath) throws PieceworkException {

        if (!rawPath.startsWith(ClasspathContentResource.PREFIX)) {
            LOG.error("Should not be looking for a classpath resource without the classpath prefix");
            return null;
        }

        String path = rawPath.substring(ClasspathContentResource.PREFIX.length());

        ContentProfile contentProfile = modelProvider.contentProfile();
        // Show never use ClasspathContentProvider unless the content profile explicitly
        // specifies a base classpath
        if (contentProfile == null || StringUtils.isEmpty(contentProfile.getBaseClasspath()))
            return null;

        String baseClasspath = contentProfile.getBaseClasspath();

        if (!ContentUtility.validateClasspath(baseClasspath, path)) {
            accessTracker.alarm(AlarmSeverity.MINOR, "Attempt to access classpath " + path + " outside of " + baseClasspath + " forbidden", modelProvider.principal());
            throw new ForbiddenError();
        }

        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            LOG.warn("No classpath resource exists for " + resource.getPath());
            return null;
        }

        return new ClasspathContentResource(resource);
    }

    @Override
    public Scheme getScheme() {
        return Scheme.CLASSPATH;
    }

    @Override
    public String getKey() {
        return "default-classpath";
    }

}
