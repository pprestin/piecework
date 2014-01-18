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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import piecework.content.ContentProvider;
import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.model.Process;

/**
 * @author James Renfro
 */
@Service
public class ClasspathContentProvider implements ContentProvider {

    private static final Logger LOG = Logger.getLogger(ClasspathContentProvider.class);

    @Override
    public Content findByPath(Process process, String base, String path) {
        if (StringUtils.isEmpty(base)) {
            LOG.warn("Cannot retrieve a classpath resource without a base path");
            return null;
        }

        if (!base.startsWith("classpath:")) {
            LOG.error("Should not be looking for a classpath resource without the classpath prefix");
            return null;
        }

        String classpathBase = base.substring("classpath:".length());

        // Make sure that the base ends with a slash
        if (!classpathBase.endsWith("/"))
            classpathBase += "/";

        String location = org.springframework.util.StringUtils.applyRelativePath(classpathBase, path);
        ClassPathResource resource = new ClassPathResource(location);

        location = "classpath:" + resource.getPath();

        String contentType = null;

        if (!resource.exists()) {
            LOG.warn("No classpath resource exists for " + location);
            return null;
        }

        if (location.endsWith(".css"))
            contentType = "text/css";
        else if (location.endsWith(".js"))
            contentType = "application/json";
        else if (location.endsWith(".html"))
            contentType = "text/html";

        return new Content.Builder().resource(resource).contentType(contentType).location(location).filename(resource.getFilename()).build();
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
