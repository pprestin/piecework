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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;

/**
 * @author Jiefeng Shen
 */
@XmlRootElement(name = User.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = User.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

    @XmlAttribute
    @XmlID
    @Id 
    private final String groupId;

    @XmlAttribute
    private final String name;

    @XmlElement
    private final String displayName;

    @XmlElementWrapper(name="fields")
    @XmlElementRef
    private final List<User> users;

    // constructors
    private Group() {
        this(new Group.Builder());
    }

    private Group(Group.Builder builder) {
        groupId = builder.groupId;
        name = builder.name;
        displayName = builder.displayName;
        users = builder.users == null ? null : Collections.unmodifiableList(builder.users);
    }

    // getters
    public String getGroupId() { return groupId; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public List<User> getUsers() { return users; }

    // nested Builder class
    public static class Builder {
        private String groupId;
        private String name;
        private String displayName;
        private List<User> users;

        // constructors
        public Builder() { super(); }

        public Builder(Group group) {
            groupId = group.groupId;
            name = group.name;
            displayName = group.displayName;
            users = group.users == null ? null : Collections.unmodifiableList(group.users);

            if ( group.users == null || group.users.isEmpty() ) {
                users = new ArrayList<User>();
            } else {
                users = new ArrayList<User>( group.users.size() );
                for ( User u : group.users ) {
                    users.add(new User.Builder(u).build());
                }
            }
        }

        public Group build() {
            return new Group(this);
        }

        // setters
        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }
    
        public Builder name(String name) {
            this.name = name;
            return this;
        }
    
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder user(User u) {
            if ( users == null ) {
                users = new ArrayList<User>();
            }
            if ( u != null ) {
                users.add(u);  // no copy is created
            }
            return this;
        }
    }
    
    public static class Constants {
        public static final String RESOURCE_LABEL = "Group";
        public static final String ROOT_ELEMENT_NAME = "group";
        public static final String TYPE_NAME = "GroupType";
    }

}
