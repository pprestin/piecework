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
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;

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
    private final String contentType;

    @XmlTransient
    private final String location;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String uri;

    private File() {
        this(new Builder(), null);
    }

    private File(Builder builder, ViewContext context) {
        this.id = builder.id;
        this.contentType = builder.contentType;
        this.location = builder.location;
        this.name = builder.name;
        this.link = builder.link == null && context != null && StringUtils.isNotEmpty(builder.processInstanceId) ? context.getApplicationUri(builder.processDefinitionKey, builder.processInstanceId, "value", builder.fieldName, builder.id) : builder.link;
        this.uri = context != null && StringUtils.isNotEmpty(builder.processInstanceId) ? context.getServiceUri(builder.processDefinitionKey, builder.processInstanceId, "value", builder.fieldName, builder.id) : builder.link;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    @JsonIgnore
    public String getLocation() {
        return location;
    }

    public String getLink() {
        return link;
    }

    public String getUri() {
        return uri;
    }

    @JsonValue(value=false)
    @JsonIgnore
    public String getValue() {
        return null;
    }

    public final static class Builder {

        private String id;
        private String name;
        private String contentType;
        private String location;
        private String processDefinitionKey;
        private String processInstanceId;
        private String fieldName;
        private String link;

        public Builder() {
            super();
        }

        public Builder(File file) {
            this.id = file.id;
            this.location = file.location;
            this.contentType = file.contentType;
            this.name = file.name;
        }

        public Builder(File file, Sanitizer sanitizer) {
            this.id = sanitizer.sanitize(file.id);
            this.location = sanitizer.sanitize(file.location);
            this.contentType = sanitizer.sanitize(file.contentType);
            this.name = sanitizer.sanitize(file.name);
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

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
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

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "File";
        public static final String ROOT_ELEMENT_NAME = "file";
        public static final String TYPE_NAME = "FileType";
    }
}
