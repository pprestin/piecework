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

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import piecework.content.ContentResource;
import piecework.content.Version;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
public class BasicContentResource implements ContentResource, Serializable {

    private static final Logger LOG = Logger.getLogger(BasicContentResource.class);

    private final String contentId;
    private final String contentType;
    private final String name;
    private final String filename;
    private final String description;
    private final String location;
    private final InputStream inputStream;
    private final String eTag;
    private final long lastModified;
    private final String lastModifiedBy;
    private final long length;
    private final Map<String, String> metadata;
    private final List<Version> versions;

    private BasicContentResource() {
        this(new Builder());
    }

    private BasicContentResource(Builder builder) {
        this.contentId = builder.contentId;
        this.contentType = builder.contentType;
        this.name = builder.name;
        this.filename = builder.filename;
        this.description = builder.description;
        this.location = builder.location;
        this.inputStream = builder.inputStream;
        this.eTag = builder.eTag;
        this.lastModified = builder.lastModified;
        this.lastModifiedBy = builder.lastModifiedBy;
        this.length = builder.length;
        this.metadata = Collections.unmodifiableMap(builder.metadata);
        this.versions = Collections.unmodifiableList(builder.versions);
    }

    public String getContentId() {
        return contentId;
    }

    public String contentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String eTag() {
        return eTag;
    }

    public long lastModified() {
        return lastModified;
    }

    @Override
    public long contentLength() {
        return length;
    }

    @Override
    public String lastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public List<Version> versions() {
        return versions;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        throw new NotImplementedException();
    }

    public final static class Builder {

        private String contentId;
        private String contentType;
        private String name;
        private String filename;
        private String description;
        private String location;
        private InputStream inputStream;
        private String eTag;
        private long lastModified;
        private String lastModifiedBy;
        private long length = 0l;
        private Map<String, String> metadata;
        private List<Version> versions;

        public Builder() {
            this.metadata = new HashMap<String, String>();
            this.versions = new ArrayList<Version>();
        }

        public BasicContentResource build() {
            return new BasicContentResource(this);
        }

        public Builder contentId(String contentId) {
            this.contentId = contentId;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified != null ? lastModified.getTime() : 0;
            return this;
        }

        public Builder lastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }

        public Builder lastModified(long lastModified) {
            this.lastModified = lastModified;
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

        public Builder version(Version version) {
            if (version != null)
                this.versions.add(version);
            return this;
        }
    }

}
