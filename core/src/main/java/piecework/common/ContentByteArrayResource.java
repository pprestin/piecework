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
package piecework.common;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import piecework.content.ContentResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author James Renfro
 */
public class ContentByteArrayResource extends AbstractResource {
    private static final Logger LOG = Logger.getLogger(ContentByteArrayResource.class);

    private final Resource contentResource;
    private byte[] byteArray;
    private long lastModified;

    public ContentByteArrayResource(Resource contentResource) {
        this.contentResource = contentResource;
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public String getFilename() {
        return contentResource.getFilename();
    }

    @Override
    public String getDescription() {
        return null; //contentResource.getLocation();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (byteArray == null) {
            InputStream input = contentResource.getInputStream();
            try {
                byteArray = IOUtils.toByteArray(input);
            } finally {
                IOUtils.closeQuietly(input);
            }
        }
        return new ByteArrayInputStream(byteArray);
    }

    @Override
    public long lastModified() throws IOException {
        return contentResource.lastModified();
    }
}
