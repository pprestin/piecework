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
package piecework.authorization;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import piecework.common.ManyMap;

import java.util.*;

/**
 * @author James Renfro
 */
public class AccessAuthority implements GrantedAuthority {

    private final Map<String, List<ResourceAuthority>> resourceAuthorityMap;
    private final Set<String> groupIds;

    protected AccessAuthority() {
        this(new Builder());
    }

    private AccessAuthority(Builder builder) {
        this.resourceAuthorityMap = Collections.unmodifiableMap(builder.resourceAuthorityMap);
        this.groupIds = Collections.unmodifiableSet(builder.groupIds);
    }

    public boolean isAuthorized(String roleAllowed, String processDefinitionKeyAllowed) {
        if (roleAllowed == null)
            return false;

        Set<ResourceAuthority> resourceAuthorities = matchedResourceAuthorities(Collections.singleton(roleAllowed));
        for (ResourceAuthority resourceAuthority : resourceAuthorities) {
            if (resourceAuthority != null && resourceAuthority.isAuthorized(roleAllowed, processDefinitionKeyAllowed))
                return true;
        }
        return false;
    }

    public boolean hasGroup(Set<String> allowedGroupIds) {
        return Sets.intersection(groupIds, allowedGroupIds).size() >= 1;
    }

    public boolean hasRole(piecework.model.Process process, Set<String> allowedRoleSet) {
        if (!resourceAuthorityMap.isEmpty()) {
            Set<ResourceAuthority> resourceAuthorities = matchedResourceAuthorities(allowedRoleSet);
            for (ResourceAuthority resourceAuthority : resourceAuthorities) {
                if (allowedRoleSet == null || allowedRoleSet.contains(resourceAuthority.getRole())) {
                    if (resourceAuthority.hasRole(process, allowedRoleSet))
                        return true;
                }
            }
        }
        return false;
    }

    public Set<String> getProcessDefinitionKeys(Set<String> allowedRoleSet) {
        Set<String> processDefinitionKeys = new HashSet<String>();
        if (!resourceAuthorityMap.isEmpty()) {
            Set<ResourceAuthority> resourceAuthorities = matchedResourceAuthorities(allowedRoleSet);
            // Iterate through all of the matching resource authorities and add their process definition keys
            for (ResourceAuthority resourceAuthority : resourceAuthorities) {
                Set<String> resourceAuthorityProcessDefinitionKeys = resourceAuthority.getProcessDefinitionKeys();
                if (resourceAuthorityProcessDefinitionKeys != null && !resourceAuthorityProcessDefinitionKeys.isEmpty())
                    processDefinitionKeys.addAll(resourceAuthorityProcessDefinitionKeys);
            }

            return processDefinitionKeys;
        }

        return Collections.emptySet();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        if (resourceAuthorityMap != null && !resourceAuthorityMap.isEmpty()) {
            int count = 1;
            int size = resourceAuthorityMap.size();
            builder.append("resourceAuthorities: [");
            for (ResourceAuthority resourceAuthority : matchedResourceAuthorities(null)) {
                builder.append(resourceAuthority.toString());
                if (count < size)
                    builder.append(", ");
                count++;
            }
            builder.append("],");
        }

        if (groupIds != null && !groupIds.isEmpty()) {
            builder.append("groupIds: [");
            int count = 1;
            int size = groupIds.size();
            for (String groupId : groupIds) {
                builder.append(groupId);
                if (count < size)
                    builder.append(", ");
                count++;
            }
            builder.append("]");
        }

        builder.append("}");
        return builder.toString();
    }

    @Override
    public String getAuthority() {
        return toString();
    }

    private Set<ResourceAuthority> matchedResourceAuthorities(Set<String> allowedRoleSet) {
        Set<ResourceAuthority> resourceAuthorities = new HashSet<ResourceAuthority>();
        if (allowedRoleSet == null) {
            // If there is no allowed role set then just grab all resource authorities
            for (List<ResourceAuthority> resourceAuthorityList : resourceAuthorityMap.values()) {
                resourceAuthorities.addAll(resourceAuthorityList);
            }
        } else {
            // Otherwise, only match resource authorities for the roles that are allowed
            for (String allowRole : allowedRoleSet) {
                List<ResourceAuthority> resourceAuthorityList = resourceAuthorityMap.get(allowRole);
                if (resourceAuthorityList != null)
                    resourceAuthorities.addAll(resourceAuthorityList);
            }
        }
        return resourceAuthorities;
    }

    public final static class Builder {

        private ManyMap<String, ResourceAuthority> resourceAuthorityMap;
        private Set<String> groupIds;

        public Builder() {
            this.resourceAuthorityMap = new ManyMap<String, ResourceAuthority>();
            this.groupIds = new HashSet<String>();
        }

        public AccessAuthority build() {
            return new AccessAuthority(this);
        }

        public Builder resourceAuthority(ResourceAuthority resourceAuthority) {
            if (resourceAuthority != null) {
                resourceAuthorityMap.putOne(resourceAuthority.getRole(), resourceAuthority);
            }
            return this;
        }

        public Builder groupId(String groupId) {
            if (StringUtils.isNotEmpty(groupId))
                groupIds.add(groupId);
            return this;
        }

    }

}
