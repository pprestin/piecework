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
package piecework.engine.activiti.identity;

import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.ContextMapperCallbackHandler;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.stereotype.Service;
import piecework.ldap.CustomLdapUserDetailsMapper;
import piecework.ldap.LdapSettings;
import piecework.service.IdentityService;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class LdapIdentityManager extends UserEntityManager {

    @Autowired
    IdentityService identityService;

    @Override
    public UserEntity findUserById(String userId) {
        piecework.model.User internalUser = identityService.getUser(userId);

        if (internalUser == null)
            return null;

        UserEntity user = new UserEntity(userId);
        user.setEmail(internalUser.getEmailAddress());

        return user;
    }

    @Override
    public Boolean checkPassword(String userId, String password) {
        return Boolean.FALSE;
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
        List<User> userList = new ArrayList<User>();
        UserQueryImpl userQuery = (UserQueryImpl) query;
        if (StringUtils.isNotEmpty(userQuery.getId())) {
            UserEntity user = findUserById(userQuery.getId());
            if (user != null)
                userList.add(user);
            return userList;
        } else if (StringUtils.isNotEmpty(userQuery.getLastName())) {
            userList.add(findUserById(userQuery.getLastName()));
            return userList;
        } else {
            return null;
        }
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query) {
        return findUserByQueryCriteria(query, null).size();
    }

}
