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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import piecework.content.ContentResource;
import piecework.content.Version;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public abstract class AbstractContentResource<R extends Resource> implements ContentResource {

    private static final Logger LOG = Logger.getLogger(AbstractContentResource.class);
    private final String contentId;
    protected final R resource;

    public AbstractContentResource(String contentId, R resource) {
        this.contentId = contentId;
        this.resource = resource;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public String getFilename() {
        return resource.getFilename();
    }

    @Override
    public String getDescription() {
        return resource.getDescription();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public long contentLength() {
        try {
            return resource.contentLength();
        } catch (IOException ioe) {
            LOG.warn("Unable to retrieve content length for classpath resource " + getLocation(), ioe);
            return -1;
        }
    }

    @Override
    public long lastModified() {
        try {
            return resource.lastModified();
        } catch (IOException ioe) {
            LOG.warn("Unable to retrieve last modified for classpath resource " + getLocation(), ioe);
            return -1;
        }
    }

    @Override
    public boolean publish() {
        return false;
    }

    @Override
    public String lastModifiedBy() {
        return null;
    }

    @Override
    public List<Version> versions() {
        return Collections.<Version>emptyList();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String eTag() {
        return null;
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        InputStream input = getInputStream();
        try {
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    protected abstract String getPrefix();

}
