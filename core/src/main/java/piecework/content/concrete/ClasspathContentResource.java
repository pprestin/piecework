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

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import piecework.content.ContentResource;

/**
 * @author James Renfro
 */
public class ClasspathContentResource extends AbstractContentResource<ClassPathResource> implements ContentResource {

    private static final Logger LOG = Logger.getLogger(ClasspathContentResource.class);
    public static final String PREFIX = "classpath:";

    public ClasspathContentResource(ClassPathResource resource) {
        super(null, resource);
    }

    @Override
    public String contentType() {
        String path = resource.getPath();
        String contentType = null;
        if (path.endsWith(".css"))
            contentType = "text/css";
        else if (path.endsWith(".js"))
            contentType = "application/json";
        else if (path.endsWith(".html"))
            contentType = "text/html";
        return contentType;
    }

    @Override
    public String getLocation() {
        return PREFIX + resource.getPath();
    }

    @Override
    protected String getPrefix() {
        return PREFIX;
    }
}
