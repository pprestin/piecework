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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import piecework.common.view.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Grouping.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Grouping.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Grouping {

    @XmlAttribute
    private final String groupingId;

    @XmlElement
    private final String title;

    @XmlElement
    private final String description;

    @XmlElement
    private final String breadcrumb;

    @XmlElementWrapper(name="sectionIds")
    private final List<String> sectionIds;

    @XmlElementWrapper(name="buttons")
    @XmlElementRef
    private final List<Button> buttons;

    @XmlAttribute
    private final boolean readonly;

    @XmlAttribute
    private final int ordinal;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private Grouping() {
        this(new Builder());
    }

    private Grouping(Builder builder) {
        this.groupingId = builder.groupingId;
        this.sectionIds = Collections.unmodifiableList(builder.sectionIds);
        this.title = builder.title;
        this.description = builder.description;
        this.breadcrumb = builder.breadcrumb;
        this.readonly = builder.readonly;
        this.buttons = builder.buttons != null ? Collections.unmodifiableList(builder.buttons) : null;
        this.ordinal = builder.ordinal;
        this.isDeleted = builder.isDeleted;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBreadcrumb() {
        return breadcrumb;
    }

    public String getGroupingId() {
        return groupingId;
    }

    public List<String> getSectionIds() {
        return sectionIds;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public final static class Builder {

        private String groupingId;
        private String title;
        private String description;
        private String breadcrumb;
        private List<String> sectionIds;
        private List<Button> buttons;
        private boolean readonly;
        private int ordinal;
        private boolean isDeleted;

        public Builder() {
            super();
            this.groupingId = UUID.randomUUID().toString();
            this.sectionIds = new ArrayList<String>();
            this.buttons = new ArrayList<Button>();
        }

        public Builder(Grouping grouping, Sanitizer sanitizer) {
            this.groupingId = grouping.groupingId != null ? sanitizer.sanitize(grouping.groupingId) : UUID.randomUUID().toString();
            this.title = sanitizer.sanitize(grouping.title);
            this.description = sanitizer.sanitize(grouping.description);
            this.breadcrumb = sanitizer.sanitize(grouping.breadcrumb);
            if (grouping.sectionIds != null && !grouping.sectionIds.isEmpty()) {
                this.sectionIds = new ArrayList<String>(grouping.sectionIds.size());
                for (String sectionId : grouping.sectionIds) {
                    this.sectionIds.add(sanitizer.sanitize(sectionId));
                }
            } else {
                this.sectionIds = new ArrayList<String>();
            }

            if (grouping.buttons != null && !grouping.buttons.isEmpty()) {
                this.buttons = new ArrayList<Button>(grouping.buttons.size());
                for (Button button : grouping.buttons) {
                    this.buttons.add(new Button.Builder(button, sanitizer).build());
                }
            } else {
                this.buttons = new ArrayList<Button>();
            }
            this.ordinal = grouping.ordinal;
        }

        public Builder groupingId(String groupingId) {
            this.groupingId = groupingId;
            return this;
        }

        public Grouping build() {
            return new Grouping(this);
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder breadcrumb(String breadcrumb) {
            this.breadcrumb = breadcrumb;
            return this;
        }

        public Builder button(Button button) {
            if (this.buttons == null)
                this.buttons = new ArrayList<Button>();
            this.buttons.add(button);
            return this;
        }

        public Builder sectionId(String sectionId) {
            if (this.sectionIds == null)
                this.sectionIds = new ArrayList<String>();
            this.sectionIds.add(sectionId);
            return this;
        }

        public Builder sectionIds(List<String> sectionIds) {
            if (this.sectionIds == null)
                this.sectionIds = new ArrayList<String>();
            this.sectionIds.addAll(sectionIds);
            return this;
        }

        public int numberOfButtons() {
            return buttons != null ? buttons.size() : 0;
        }

        public Builder readonly() {
            this.readonly = true;
            return this;
        }

        public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
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
        public static final String RESOURCE_LABEL = "Grouping";
        public static final String ROOT_ELEMENT_NAME = "grouping";
        public static final String TYPE_NAME = "GroupingType";
    }

}
