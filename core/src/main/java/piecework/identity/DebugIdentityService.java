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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import piecework.authorization.DebugResourceAuthority;
import piecework.authorization.ResourceAuthority;
import piecework.model.User;
import piecework.persistence.ProcessRepository;
import piecework.service.IdentityService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
public class DebugIdentityService implements IdentityService {

    @Autowired
    Environment environment;

    @Autowired
    ProcessRepository processRepository;

    @Override
    public User getUser(String internalId) {

        return new User.Builder(loadUserByUsername(internalId)).build();
    }

    @Override
    public User getUserByAnyId(String id) {
        return getUser(id);
    }

    @Override
    public List<User> findUsersByDisplayName(String displayNameLike, Long maxResults) {
        return Collections.singletonList(getUser("testuser"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String testUser = environment.getProperty("authentication.testuser");
        String id = username;
        String displayName = username;

        if (testUser != null && testUser.equals(id))
            displayName = environment.getProperty("authentication.testuser.displayName");

        UserDetails delegate = new org.springframework.security.core.userdetails.User(id, "none",
                Collections.singletonList(new DebugResourceAuthority(processRepository)));
        return new IdentityDetails(delegate, id, id, displayName, "");
    }
}
