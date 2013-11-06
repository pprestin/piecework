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
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Transient;
import piecework.security.Sanitizer;

import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Container implements Serializable {

    private final String containerId;

    private final String tagName;

    private final String cssClass;

    private final String title;

    private final String breadcrumb;

    private final String description;

    @Transient
    private final List<Field> fields;

    private final List<String> fieldIds;

    private final List<Container> children;

    private final List<Button> buttons;

    private final int ordinal;

    private final int activeChildIndex;

    private final boolean readonly;

    private final boolean deleted;

    private Container() {
        this(new Builder());
    }

    private Container(Builder builder) {
        this.containerId = builder.containerId;
        this.tagName = builder.tagName;
        this.cssClass = builder.cssClass;
        this.title = builder.title;
        this.breadcrumb = builder.breadcrumb;
        this.description = builder.description;
        this.fields = Collections.unmodifiableList(builder.fields);
        this.fieldIds = Collections.unmodifiableList(builder.fieldIds);
        this.children = Collections.unmodifiableList(builder.children);
        this.buttons = Collections.unmodifiableList(builder.buttons);
        this.ordinal = builder.ordinal;
        this.activeChildIndex = builder.activeChildIndex;
        this.readonly = builder.readonly;
        this.deleted = builder.deleted;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getTagName() {
        return tagName;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getTitle() {
        return title;
    }

    public String getBreadcrumb() {
        return breadcrumb;
    }

    public String getDescription() {
        return description;
    }

    public List<Field> getFields() {
        return fields;
    }

    @JsonIgnore
    public List<String> getFieldIds() {
        return fieldIds;
    }

    public List<Container> getChildren() {
        return children;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public int getActiveChildIndex() {
        return activeChildIndex;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public static final class Builder {

        private String containerId;
        private String tagName;
        private String cssClass;
        private String title;
        private String breadcrumb;
        private String description;
        private List<Field> fields;
        private List<String> fieldIds;
        private List<Container> children;
        private List<Button> buttons;
        private int ordinal;
        private int activeChildIndex;
        private boolean readonly;
        private boolean deleted;

        public Builder() {
            this.containerId = UUID.randomUUID().toString();
            this.fieldIds = new ArrayList<String>();
            this.fields = new ArrayList<Field>();
            this.children = new ArrayList<Container>();
            this.buttons = new ArrayList<Button>();
            this.activeChildIndex = -1;
        }

        public Builder(Container container, Sanitizer sanitizer) {
            this(container, sanitizer, null);
        }

        public Builder(Container container, Sanitizer sanitizer, Map<String, Field> fieldMap) {
            this(container, sanitizer, fieldMap, null);
        }

        public Builder(Container container, Sanitizer sanitizer, Map<String, Field> fieldMap, String containerIdToRemove) {
            this();
            this.containerId = container.containerId;
            this.title = sanitizer.sanitize(container.title);
            this.breadcrumb = sanitizer.sanitize(container.breadcrumb);
            this.description = sanitizer.sanitize(container.description);
            // Prefer objects to ids
            if (container.fields != null && !container.fields.isEmpty()) {
                for (Field field : container.fields) {
                    field(new Field.Builder(field, sanitizer).build());
                }
            } else if (container.fieldIds != null && !container.fieldIds.isEmpty()) {
                TreeSet<Field> orderedSet = new TreeSet<Field>();
                for (String fieldId : container.fieldIds) {
                    if (fieldMap != null) {
                        Field field = fieldMap.get(fieldId);
                        if (field != null)
                            orderedSet.add(field);
                    }
                    this.fieldIds.add(sanitizer.sanitize(fieldId));
                }
                if (!orderedSet.isEmpty())
                    fields = new ArrayList<Field>(orderedSet);
            }
            if (container.children != null && !container.children.isEmpty()) {
                for (Container child : container.children) {
                    if (child.getContainerId() == null || containerIdToRemove == null || !containerIdToRemove.equals(child.getContainerId())) {
                        Builder childBuilder = new Builder(child, sanitizer, fieldMap, containerIdToRemove);
                        if (container.activeChildIndex != -1 && container.activeChildIndex != child.ordinal)
                            childBuilder.readonly();
                        this.children.add(childBuilder.build());
                    }
                }
            }
            if (container.buttons != null && !container.buttons.isEmpty()) {
                for (Button button : container.buttons) {
                    this.buttons.add(new Button.Builder(button, sanitizer).build());
                }
            }
            this.activeChildIndex = container.activeChildIndex;
            this.ordinal = container.ordinal;
            this.readonly = container.readonly;
            this.deleted = container.deleted;
        }

        public Container build() {
            return new Container(this);
        }

        public Builder field(Field field) {
            if (field != null) {
                this.fields.add(field);
                if (field.getFieldId() != null)
                    this.fieldIds.add(field.getFieldId());
            }
            return this;
        }

        public Builder fields(Field ... fields) {
            return fields(Arrays.asList(fields));
        }

        public Builder fields(Collection<Field> fields) {
            if (fields != null && !fields.isEmpty()) {
                this.fields = new ArrayList<Field>(fields);
                for (Field field : fields) {
                    if (StringUtils.isNotEmpty(field.getFieldId()))
                        this.fieldIds.add(field.getFieldId());
                }
            }
            return this;
        }

        public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder breadcrumb(String breadcrumb) {
            this.breadcrumb = breadcrumb;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder child(Container child) {
            if (child != null)
                this.children.add(child);
            return this;
        }

        public Builder button(Button button) {
            if (this.buttons == null)
                this.buttons = new ArrayList<Button>();
            this.buttons.add(button);
            return this;
        }

        public Builder activeChildIndex(int activeChildIndex) {
            this.activeChildIndex = activeChildIndex;
            return this;
        }

        public Builder readonly() {
            this.readonly = true;
            return this;
        }

        public Builder readonly(boolean readonly) {
            this.readonly = readonly;
            return this;
        }

        public Builder delete() {
            this.deleted = true;
            return this;
        }

        public Builder undelete() {
            this.deleted = false;
            return this;
        }

    }

}
