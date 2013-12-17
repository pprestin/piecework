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
package piecework.identity;

import org.apache.cxf.common.util.StringUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import piecework.model.User;
import piecework.service.IdentityService;

import java.util.*;

/**
 * @author James Renfro
 */
public class DebugIdentityService implements IdentityService {

    private final UserDetailsService userDetailsService;

    public DebugIdentityService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public User getUser(String internalId) {
        if (StringUtils.isEmpty(internalId))
            return null;
        return new User.Builder(userDetailsService.loadUserByUsername(internalId)).build();
    }

    @Override
    public List<User> findUsersByDisplayName(String displayNameLike, Long maxResults) {
        return Collections.singletonList(getUser("testuser"));
    }

    @Override
    public Map<String, User> findUsers(Set<String> ids) {
        Map<String, User> map = new HashMap<String, User>();
        if (ids != null) {
            for (String id : ids) {
                if (id == null)
                    continue;

                User user = getUser(id);
                if (user != null)
                    map.put(id, user);
            }
        }
        return map;
    }

}
