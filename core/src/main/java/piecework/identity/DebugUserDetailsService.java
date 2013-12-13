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

import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import piecework.ServiceLocator;
import piecework.authorization.SuperUserAccessAuthority;
import piecework.service.ProcessService;

import java.util.Collections;

/**
 * @author James Renfro
 */
public class DebugUserDetailsService implements UserDetailsService {

    private SuperUserAccessAuthority accessAuthority;
    private final ServiceLocator serviceLocator;
    private final String testUser;
    private final String displayName;

    public DebugUserDetailsService(Environment environment, ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        this.testUser = environment.getProperty("authentication.testuser");
        this.displayName = environment.getProperty("authentication.testuser.displayName");
    }

    public void init() {

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String id = username;
        String displayName = username;

        if (testUser != null && testUser.equals(id))
            displayName = this.displayName;

        if (accessAuthority == null) {
            ProcessService processService = serviceLocator.getService(ProcessService.class);
            this.accessAuthority = new SuperUserAccessAuthority(processService);
        }

        UserDetails delegate = new org.springframework.security.core.userdetails.User(id, "none",
                Collections.singletonList(accessAuthority));
        return new IdentityDetails(delegate, id, id, displayName, "");
    }

}
