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
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.enumeration.FlowElementType;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Activity.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Activity.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Activity.Constants.ROOT_ELEMENT_NAME)
public class Activity implements Serializable {

    @Id
    private final String activityId;

    private final ActivityUsageType usageType;

    private final FlowElementType elementType;

    private final Set<Field> fields;

    private final Map<ActionType, Action> actionMap;

    private final int activeScreen;

    private final boolean allowAttachments;

    private final long maxAttachmentSize;

    private final boolean allowAny;

    private Activity() {
        this(new Builder());
    }

    private Activity(Builder builder) {
        this.activityId = builder.activityId;
        this.fields = Collections.unmodifiableSet(builder.fields);
        this.actionMap = Collections.unmodifiableMap(builder.actionMap);
        this.elementType = builder.elementType;
        this.usageType = builder.usageType;
        this.activeScreen = builder.activeScreen;
        this.allowAttachments = builder.allowAttachments;
        this.allowAny = builder.allowAny;
        this.maxAttachmentSize = builder.maxAttachmentSize;
    }

    public String getActivityId() {
        return activityId;
    }

    public FlowElementType getElementType() {
        return elementType;
    }

    public ActivityUsageType getUsageType() {
        return usageType;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public Action action(ActionType type) {
        return actionMap != null ? actionMap.get(type) : null;
    }

    public Map<ActionType, Action> getActionMap() {
        return actionMap;
    }

    public boolean isAllowAny() {
        return allowAny;
    }

    public boolean isAllowAttachments() {
        return allowAttachments;
    }

    public long getMaxAttachmentSize() {
        return maxAttachmentSize;
    }

    @JsonIgnore
    public Map<String, Field> getFieldMap() {
        Map<String, Field> map = new HashMap<String, Field>();
        if (fields != null && !fields.isEmpty()) {
            for (Field field : fields) {
                if (field.getFieldId() == null)
                    continue;
                map.put(field.getFieldId(), field);
            }
        }
        return map;
    }

    @JsonIgnore
    public Map<String, Field> getFieldKeyMap() {
        Map<String, Field> map = new HashMap<String, Field>();
        if (fields != null && !fields.isEmpty()) {
            for (Field field : fields) {
                if (field.getName() == null)
                    continue;
                map.put(field.getName(), field);
            }
        }
        return map;
    }

    public int getActiveScreen() {
        return activeScreen;
    }

    public Activity withoutContainer(String containerId) {
        return new Activity.Builder(this, new PassthroughSanitizer(), containerId).build();
    }

    public final static class Builder {
        private String activityId;
        private ActivityUsageType usageType;
        private FlowElementType elementType;
        private Set<Field> fields;
        private Map<ActionType, Action> actionMap;
        private int activeScreen;
        private boolean allowAttachments;
        private boolean allowAny;
        private long maxAttachmentSize;

        public Builder() {
            this.fields = new TreeSet<Field>();
            this.actionMap = new HashMap<ActionType, Action>();
            this.activeScreen = -1;
            this.maxAttachmentSize = 10485760l;
        }

        public Builder(Activity activity, Sanitizer sanitizer) {
            this(activity, sanitizer, null);
        }

        public Builder(Activity activity, Sanitizer sanitizer, String containerIdToRemove) {
            this();
            this.activityId = activity.activityId;
            Map<String, Field> fieldMap = activity.getFieldMap();
            if (activity.fields != null && !activity.fields.isEmpty()) {
                for (Field field : activity.fields) {
                    this.fields.add(new Field.Builder(field, sanitizer).build());
                }
            }
            if (activity.actionMap != null && !activity.actionMap.isEmpty()) {
                for (Map.Entry<ActionType, Action> entry : activity.actionMap.entrySet()) {
                    Action value = entry.getValue();
                    if (value.getContainer() == null)
                        continue;

                    if (value.getContainer().getContainerId() == null || containerIdToRemove == null || !containerIdToRemove.equals(value.getContainer().getContainerId())) {
                        Container container = new Container.Builder(value.getContainer(), sanitizer, fieldMap).build();
                        String location = sanitizer.sanitize(value.getLocation());
                        DataInjectionStrategy strategy = value.getStrategy();
                        this.actionMap.put(entry.getKey(), new Action(container, location, strategy));
                    }
                }
            }
            this.usageType = activity.usageType;
            this.activeScreen = activity.activeScreen;
            this.allowAttachments = activity.allowAttachments;
            this.allowAny = activity.allowAny;
            this.maxAttachmentSize = activity.maxAttachmentSize;
        }

        public Activity build() {
            return new Activity(this);
        }

        public Builder fields(Set<Field> fields) {
            this.fields = fields;
            return this;
        }

        public Builder appendFields(Collection<Field> fields) {
            if (fields != null)
                this.fields.addAll(fields);
            return this;
        }

        public Builder fields(Field ... fields) {
            this.fields = new TreeSet<Field>(Arrays.asList(fields));
            return this;
        }

        public Builder elementType(FlowElementType elementType) {
            this.elementType = elementType;
            return this;
        }

        public Builder usageType(ActivityUsageType usageType) {
            this.usageType = usageType;
            return this;
        }

        public Builder action(ActionType type, Action action) {
            this.actionMap.put(type, action);
            appendFieldsFromContainer(action.getContainer());
            return this;
        }

        private void appendFieldsFromContainer(Container container) {
            if (container != null) {
                this.appendFields(container.getFields());
                for (Container child : container.getChildren())
                    appendFieldsFromContainer(child);
            }
        }

        public Builder actionMap(Map<ActionType, Action> actionMap) {
            this.actionMap = actionMap;
            return this;
        }

        public Builder activeScreen(int activeScreen) {
            this.activeScreen = activeScreen;
            return this;
        }

        public Builder allowAttachments() {
            this.allowAttachments = true;
            return this;
        }

        public Builder allowAny() {
            this.allowAny = true;
            return this;
        }

        public Builder maxAttachmentSize(long maxAttachmentSize) {
            this.maxAttachmentSize = maxAttachmentSize;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Activity";
        public static final String ROOT_ELEMENT_NAME = "activity";
        public static final String TYPE_NAME = "ActivityType";
    }

}
