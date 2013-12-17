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

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import piecework.content.ContentProvider;
import piecework.enumeration.Scheme;
import piecework.model.*;

/**
 * @author James Renfro
 */
@Service
public class ClasspathContentProvider implements ContentProvider {

    @Override
    public Content findByPath(piecework.model.Process process, String location) {
        if (!location.startsWith("classpath:"))
            return null;

        String classpathLocation = location.substring("classpath:".length());
        ClassPathResource resource = new ClassPathResource(classpathLocation);

        String contentType = null;

        if (location.endsWith(".css"))
            contentType = "text/css";
        else if (location.endsWith(".js"))
            contentType = "application/json";
        else if (location.endsWith(".html"))
            contentType = "text/html";

        return new Content.Builder().resource(resource).contentType(contentType).location(location).filename(location).build();
    }

    @Override
    public Scheme getScheme() {
        return Scheme.CLASSPATH;
    }

}
