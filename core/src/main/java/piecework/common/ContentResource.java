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
import piecework.model.Content;
import piecework.ui.DatedByteArrayResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author James Renfro
 */
public class ContentResource extends AbstractResource {
    private static final Logger LOG = Logger.getLogger(ContentResource.class);

    private final Content content;
    private byte[] byteArray;
    private long lastModified;

    public ContentResource(Content content) {
        this.content = content;
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public String getFilename() {
        return content.getFilename();
    }

    @Override
    public String getDescription() {
        return content.getLocation();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (byteArray == null) {
            InputStream input = content.getResource().getInputStream();
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
        return content.getResource() != null ? content.getResource().lastModified() : lastModified;
    }
}
