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
import piecework.common.Decorateable;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
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
public class Activity implements Serializable, Decorateable<Activity> {

    @Id
    private final String activityId;

    private final ActivityUsageType usageType;

    private final FlowElementType elementType;

    private final Set<Field> fields;

    private final Container container;

    private final Map<ActionType, ActivityResponse> responseMap;

    private final String location;

    private final int activeScreen;

    private Activity() {
        this(new Builder());
    }

    private Activity(Builder builder) {
        this.activityId = builder.activityId;
        this.fields = Collections.unmodifiableSet(builder.fields);
        this.location = builder.location;
        this.container = builder.container;
        this.responseMap = Collections.unmodifiableMap(builder.responseMap);
        this.elementType = builder.elementType;
        this.usageType = builder.usageType;
        this.activeScreen = builder.activeScreen;
    }

    public Activity decorate() {
        return new Builder(this, new PassthroughSanitizer()).build();
    }

    public String getActivityId() {
        return activityId;
    }

    public ActivityUsageType getUsageType() {
        return usageType;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public Map<ActionType, ActivityResponse> getResponseMap() {
        return responseMap;
    }

    public ActivityResponse getActivityResponse(ActionType action) {
        return responseMap.get(action);
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

    public String getLocation() {
        return location;
    }

    public Container getContainer() {
        return container;
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
        private String location;
        private Container container;
        private Map<ActionType, ActivityResponse> responseMap;
        private int activeScreen;

        public Builder() {
            this.fields = new TreeSet<Field>();
            this.responseMap = new HashMap<ActionType, ActivityResponse>();
            this.activeScreen = -1;
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
            if (activity.responseMap != null && !activity.responseMap.isEmpty()) {
                for (Map.Entry<ActionType, ActivityResponse> entry : activity.responseMap.entrySet()) {
                    ActivityResponse value = entry.getValue();
                    if (value.getContainer() == null)
                        continue;

                    if (value.getContainer().getContainerId() == null || containerIdToRemove == null || !containerIdToRemove.equals(value.getContainer().getContainerId())) {
                        Container container = new Container.Builder(value.getContainer(), sanitizer, fieldMap).build();
                        String location = sanitizer.sanitize(value.getLocation());
                        this.responseMap.put(entry.getKey(), new ActivityResponse(container, location));
                    }
                }
            }
            this.location = sanitizer.sanitize(activity.location);
            this.container = activity.container != null && (containerIdToRemove == null || containerIdToRemove.equals(activity.container.getContainerId())) ? new Container.Builder(activity.container, sanitizer, fieldMap, containerIdToRemove).build() : new Container.Builder().build();
            this.usageType = activity.usageType;
            this.activeScreen = activity.activeScreen;
        }

        public Activity build() {
            return new Activity(this);
        }

        public Builder container(Container container) {
            this.container = container;
            return this;
        }

        public Builder fields(Set<Field> fields) {
            this.fields = fields;
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

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder response(ActionType action, ActivityResponse response) {
            this.responseMap.put(action, response);
            return this;
        }

        public Builder activeScreen(int activeScreen) {
            this.activeScreen = activeScreen;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Activity";
        public static final String ROOT_ELEMENT_NAME = "activity";
        public static final String TYPE_NAME = "ActivityType";
    }

}
