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
package piecework.model;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import piecework.content.concrete.RemoteResource;
import piecework.ui.Streamable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction on GridFSFile or GridFSResource -- not a document, but to make GridFS resources
 * look like other model objects for the purpose of keeping the code readable.
 *
 * @author James Renfro
 */
public class Content implements Serializable, Streamable {

    private static final Logger LOG = Logger.getLogger(Content.class);

    private final String contentId;
    private final String contentType;
    private final String fieldName;
    private final String filename;
    private final String location;
    private final InputStream inputStream;
    private final Resource resource;
    private final String md5;
    private final Date lastModified;
    private final Long length;
    private final Map<String, String> metadata;

    private Content() {
        this(new Builder());
    }

    private Content(Builder builder) {
        this.contentId = builder.contentId;
        this.contentType = builder.contentType;
        this.fieldName = builder.fieldName;
        this.filename = builder.filename;
        this.location = builder.location;
        this.inputStream = builder.inputStream;
        this.resource = builder.resource;
        this.md5 = builder.md5;
        this.lastModified = builder.lastModified;
        this.length = builder.length;
        this.metadata = Collections.unmodifiableMap(builder.metadata);
    }

    public String getContentId() {
        return contentId;
    }

    public String getContentType() {
        if (resource != null && resource instanceof RemoteResource)
            return ((RemoteResource) resource).contentType();
        return contentType;
    }

    public String getName() {
        return filename;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFilename() {
        if (resource != null && resource instanceof RemoteResource)
            return ((RemoteResource) resource).getFilename();

        return filename;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    // Use getResource instead where possible
    public InputStream getInputStream() {
        if (resource != null) {
            try {
                return resource.getInputStream();
            } catch (IOException ioe) {
                LOG.error("Caught an io exception trying to grab the input stream from the resource object", ioe);
                return null;
            }
        }
        return inputStream;
    }

    public Resource getResource() {
        return resource;
    }

    public String getMd5() {
        return md5;
    }

    public Date getLastModified() {
        if (resource != null && resource instanceof RemoteResource) {
            try {
                return new Date(((RemoteResource) resource).lastModified());
            } catch (IOException ioe) {
                // Ignore
                LOG.info("Caught io exception checking last modified on remote resource");
            }
        }
        return lastModified;
    }

    public Long getLength() {
        if (resource != null && resource instanceof RemoteResource) {
            try {
                return ((RemoteResource) resource).contentLength();
            } catch (IOException ioe) {
                // Ignore
                LOG.info("Caught io exception checking content length on remote resource");
            }
        }
        return length;
    }

    public final static class Builder {

        private String contentId;
        private String contentType;
        private String fieldName;
        private String filename;
        private String location;
        private InputStream inputStream;
        private Resource resource;
        private String md5;
        private Date lastModified;
        private Long length;
        private Map<String, String> metadata;

        public Builder() {
            this.metadata = new HashMap<String, String>();
        }

        public Builder(Content content) {
            this.contentId = content.contentId;
            this.contentType = content.contentType;
            this.fieldName = content.fieldName;
            this.filename = content.filename;
            this.location = content.location;
            this.inputStream = content.inputStream;
            this.resource = content.resource;
            this.md5 = content.md5;
            this.lastModified = content.lastModified;
            this.length = content.length;
            this.metadata = content.metadata != null ? new HashMap<String, String>(content.metadata) : new HashMap<String, String>();
        }

        public Content build() {
            return new Content(this);
        }

        public Builder contentId(String contentId) {
            this.contentId = contentId;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Builder resource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public Builder md5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder lastModified(long lastModified) {
            this.lastModified = new Date(lastModified);
            return this;
        }

        public Builder length(Long length) {
            this.length = length;
            return this;
        }

        public Builder metadata(String name, String value) {
            this.metadata.put(name, value);
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            if (metadata != null)
                this.metadata = new HashMap<String, String>(metadata);
            return this;
        }
    }

}
