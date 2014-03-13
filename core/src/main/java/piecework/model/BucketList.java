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
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jiefeng Shen
 */
@XmlRootElement(name = BucketList.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = BucketList.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = BucketList.Constants.ROOT_ELEMENT_NAME)
public class BucketList {

    private static final long serialVersionUID = -8642937056889667693L;

    @XmlAttribute
    @XmlID
    @Id
    private final String bucketListId;	// unique key (could be the same as display name)

    @XmlElement
    private final String name;

    @XmlAttribute
    private final List<String> buckets;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private BucketList() {
        this(new BucketList.Builder(), new ViewContext());
    }

    private BucketList(BucketList.Builder builder, ViewContext context) {
        this.bucketListId = builder.bucketListId;
        this.name = builder.name;
        this.isDeleted = builder.isDeleted;
        this.buckets = builder.buckets != null ? Collections.unmodifiableList(builder.buckets) : null;
    }

    public String getBucketListId() {
        return bucketListId;
    }

    public String getName() {
        return name;
    }

    public List<String> getBuckets() {
        return buckets;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public final static class Builder {

        private String bucketListId;
        private String name;
        private List<String> buckets;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(BucketList field, Sanitizer sanitizer) {
            this.bucketListId = sanitizer.sanitize(field.bucketListId);
            this.name = sanitizer.sanitize(field.name);
            this.isDeleted = field.isDeleted;

            if (field.buckets != null && !field.buckets.isEmpty()) {
                this.buckets = new ArrayList<String>(field.buckets.size());
                for (String bucket : field.buckets) {
                    this.buckets.add(bucket);
                }
            }
        }

        public BucketList build() {
            return new BucketList(this, null);
        }

        public BucketList build(ViewContext context) {
            return new BucketList(this, context);
        }

        public Builder bucketListId(String bucketListId) {
            this.bucketListId = bucketListId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder bucket(String bucket) {
            if (this.buckets == null)
                this.buckets = new ArrayList<String>();
            this.buckets.add(bucket);
            return this;
        }

        public Builder delete() {
            this.isDeleted = true;
            return this;
        }

        public Builder undelete() {
            this.isDeleted = false;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "BucketList";
        public static final String ROOT_ELEMENT_NAME = "bucket.list";
        public static final String TYPE_NAME = "BucketListType";
    }

}
