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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import piecework.enumeration.Scheme;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ContentProfile.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ContentProfile.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentProfile {

    private final String contentHandlerKey;
    private final String baseClasspath;
    private final String baseDirectory;
    private final String baseRepositoryLocation;
    private final Set<String> remoteResourceLocations;
    private final Scheme defaultScheme;
    private final Map<String, String> contentMetadata;

    private ContentProfile() {
        this(new Builder());
    }

    private ContentProfile(Builder builder) {
        this.contentHandlerKey = builder.contentHandlerKey;
        this.baseClasspath = builder.baseClasspath;
        this.baseDirectory = builder.baseDirectory;
        this.baseRepositoryLocation = builder.baseRepositoryLocation;
        this.remoteResourceLocations = builder.remoteResourceLocations;
        this.defaultScheme = builder.defaultScheme;
        this.contentMetadata = builder.contentMetadata;
    }

    public String getContentHandlerKey() {
        return contentHandlerKey;
    }

    public String getBaseClasspath() {
        return baseClasspath;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public String getBaseRepositoryLocation() {
        return baseRepositoryLocation;
    }

    public Set<String> getRemoteResourceLocations() {
        return remoteResourceLocations;
    }

    public Scheme getDefaultScheme() {
        return defaultScheme;
    }

    public Map<String, String> getContentMetadata() {
        return contentMetadata;
    }

    public final static class Builder {

        private String contentHandlerKey;
        private String baseClasspath;
        private String baseDirectory;
        private String baseRepositoryLocation;
        private Set<String> remoteResourceLocations;
        private Scheme defaultScheme;
        private Map<String, String> contentMetadata;

        public Builder() {
            this.defaultScheme = Scheme.NONE;
            this.contentMetadata = new HashMap<String, String>();
        }

        public Builder(ContentProfile profile, Sanitizer sanitizer) {
            this.contentHandlerKey = sanitizer.sanitize(profile.contentHandlerKey);
            this.baseClasspath = sanitizer.sanitize(profile.baseClasspath);
            this.baseDirectory = sanitizer.sanitize(profile.baseDirectory);
            this.baseRepositoryLocation = sanitizer.sanitize(profile.baseRepositoryLocation);
            this.remoteResourceLocations = new HashSet<String>();
            if (profile.remoteResourceLocations != null) {
                for (String remoteResourceLocation : profile.remoteResourceLocations) {
                    if (remoteResourceLocation == null)
                        continue;

                    this.remoteResourceLocations.add(sanitizer.sanitize(remoteResourceLocation));
                }
            }
            this.defaultScheme = profile.defaultScheme;
            this.contentMetadata = new HashMap<String, String>();
            if (profile.contentMetadata != null && !profile.contentMetadata.isEmpty()) {
                for (Map.Entry<String, String> entry : profile.contentMetadata.entrySet()) {
                    String key = sanitizer.sanitize(entry.getKey());
                    String value = sanitizer.sanitize(entry.getValue());
                    this.contentMetadata.put(key, value);
                }
            }
        }

        public ContentProfile build() {
            return new ContentProfile(this);
        }

        public Builder contentHandlerKey(String contentHandlerKey) {
            this.contentHandlerKey = contentHandlerKey;
            return this;
        }

        public Builder baseClasspath(String baseClasspath) {
            this.baseClasspath = baseClasspath;
            return this;
        }

        public Builder baseDirectory(String baseDirectory) {
            this.baseDirectory = baseDirectory;
            return this;
        }

        public Builder baseRepositoryLocation(String baseRepositoryLocation) {
            this.baseRepositoryLocation = baseRepositoryLocation;
            return this;
        }

        public Builder remoteResourceLocations(Set<String> remoteResourceLocations) {
            this.remoteResourceLocations = remoteResourceLocations;
            return this;
        }

        public Builder defaultScheme(Scheme defaultScheme) {
            this.defaultScheme = defaultScheme;
            return this;
        }

        public Builder contentMetadata(String key, String value) {
            this.contentMetadata.put(key, value);
            return this;
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Content Profile";
        public static final String ROOT_ELEMENT_NAME = "contentProfile";
        public static final String TYPE_NAME = "ContentProfileType";
    }

}
