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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Transient;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.content.Version;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
@XmlRootElement(name = File.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = File.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class File extends Value {

    @XmlTransient
    private final String id;

    @XmlElement
    private final String name;

    @XmlElement
    private final String description;

    @XmlElement
    private final String contentType;

    @XmlTransient
    private final String location;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String uri;



    @XmlElementWrapper(name = "versions")
    @XmlElement(name = "version")
    private final List<Version> versions;

    @XmlTransient
    @JsonIgnore
    @Transient
    private transient final ContentResource contentResource;


    private File() {
        this(new Builder(), null);
    }

    private File(Builder builder, ViewContext context) {
        this.id = builder.id;
        this.contentType = builder.contentType;
        this.location = builder.location;
        this.name = builder.name;
        this.description = builder.description;
        this.link = builder.link == null && context != null && StringUtils.isNotEmpty(builder.processInstanceId) ? context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.processInstanceId, "value", builder.fieldName, builder.id) : builder.link;
        this.uri = context != null && StringUtils.isNotEmpty(builder.processInstanceId) ? context.getServiceUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.processInstanceId, "value", builder.fieldName, builder.id) : builder.link;
        this.contentResource = builder.contentResource;
        this.versions = builder.versions != null && !builder.versions.isEmpty() ? new ArrayList<Version>(builder.versions) : new ArrayList<Version>();
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getContentType() {
        return contentType;
    }

    @JsonIgnore
    public String getLocation() {
        return location;
    }

    @JsonIgnore
    public ContentResource getContentResource() {
        return contentResource;
    }

    public String getLink() {
        return link;
    }

    public String getUri() {
        return uri;
    }

    public List<Version> getVersions() {
        return versions;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isEmpty(name);
    }

    @JsonValue(value=false)
    @JsonIgnore
    public String getValue() {
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public final static class Builder {

        private String id;
        private String name;
        private String description;
        private String contentType;
        private String location;
        private String processDefinitionKey;
        private String processInstanceId;
        private String fieldName;
        private String link;
        private ContentResource contentResource;
        private List<Version> versions = new ArrayList<Version>();

        public Builder() {
            super();
        }

        public Builder(File file) {
            this.id = file.id;
            this.description = file.description;
            this.location = file.location;
            this.name = file.name;
            this.contentType = file.contentType;
            this.contentResource = file.contentResource;
            this.versions = file.versions != null && !file.versions.isEmpty() ? new ArrayList<Version>(file.versions) : new ArrayList<Version>();
        }

        public Builder(File file, Sanitizer sanitizer) {
            this.id = sanitizer.sanitize(file.id);
            this.description = sanitizer.sanitize(file.description);
            this.location = sanitizer.sanitize(file.location);
            this.name = sanitizer.sanitize(file.name);
            this.contentType = sanitizer.sanitize(file.contentType);
            this.contentResource = file.contentResource;
            this.versions = file.versions != null && !file.versions.isEmpty() ? new ArrayList<Version>(file.versions) : new ArrayList<Version>();
        }

        public File build() {
            return new File(this, null);
        }

        public File build(ViewContext context) {
            return new File(this, context);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder contentResource(ContentResource contentResource) {
            this.contentResource = contentResource;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder link(String link) {
            this.link = link;
            return this;
        }

        public Builder versions(List<Version> versions) {
            if (versions != null && !versions.isEmpty())
                this.versions.addAll(versions);
            return this;
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "File";
        public static final String ROOT_ELEMENT_NAME = "file";
        public static final String TYPE_NAME = "FileType";
    }
}
