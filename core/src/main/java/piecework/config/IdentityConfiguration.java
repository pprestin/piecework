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
package piecework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import piecework.identity.DebugUserDetailsService;
import piecework.identity.GroupServiceFactoryBean;
import piecework.identity.IdentityServiceFactoryBean;
import piecework.identity.UserDetailsServiceFactoryBean;
import piecework.ldap.*;
import piecework.service.GroupService;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author James Renfro
 */
@Configuration
public class IdentityConfiguration {

    @Bean
    public LdapSettings ldapSettings(Environment environment) {
        return new LdapSettings(environment);
    }

    @Bean
    public GroupServiceFactoryBean groupServiceFactoryBean() {
        return new GroupServiceFactoryBean();
    }

    @Bean
    public IdentityServiceFactoryBean identityServiceFactoryBean() {
        return new IdentityServiceFactoryBean();
    }

    @Bean
    public UserDetailsServiceFactoryBean userDetailsServiceFactoryBean() {
        return new UserDetailsServiceFactoryBean();
    }

//    @Bean
//    public CustomLdapUserDetailsMapper userDetailsMapper() throws Exception {
//        return new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper());
//    }

}
