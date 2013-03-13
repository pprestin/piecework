/*
 * Copyright 2012 University of Washington
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
package piecework.authentication.ldap;

import javax.naming.directory.SearchControls;
import javax.ws.rs.core.Response;

import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import piecework.ApplicationConfigurationForTest;
import piecework.authentication.AuthenticationHandler;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
	classes={AuthenticationConfiguration.class, ApplicationConfigurationForTest.class, StandaloneLdapConfiguration.class}, 
	loader=AnnotationConfigContextLoader.class
)
@ActiveProfiles({"ldap", "test", "standalone"})
public class LdapAuthenticationHandlerTest {

	@Autowired
	AuthenticationHandler authenticationHandler;
	
	@Autowired
	LdapContextSource personLdapContextSource;
	
	@Autowired
	LdapContextSource groupLdapContextSource;
	
	@Test
	public void test() {

		String id = "jkeats";
		String password = "pass";
		
		Message message = Mockito.mock(Message.class);
		ClassResourceInfo resourceClass = Mockito.mock(ClassResourceInfo.class);
		Mockito.when(message.get(SecurityToken.class)).thenReturn(new UsernameToken(id, password, null, false, null, null));
		
		Response response = authenticationHandler.handleRequest(message, resourceClass);
	
		
		
//		SpringSecurityLdapTemplate ldapTemplate = new SpringSecurityLdapTemplate(groupLdapContextSource);
//	
////		AndFilter filter = new AndFilter();
////		filter.and(new LikeFilter("uwRegId", id));
//		String encoded = "member=uwNetID=" + id;
//		
//		AbstractContextMapper userMapper = new AbstractContextMapper() {
//			protected Object doMapFromContext(DirContextOperations context) {
//				return context.getStringAttribute("cn");
//			}
//		};
//		
//		ContextMapperCallbackHandler callbackHandler = new ContextMapperCallbackHandler(userMapper);
//		
//		ldapTemplate.search(DistinguishedName.EMPTY_PATH, 
//        		encoded, getSearchControls(), callbackHandler);
//		
//		List<String> list = callbackHandler.getList();
//		
//		for (String name : list) {
//			System.out.println(name);
//		}
	}

	private SearchControls getSearchControls() {
        SearchControls retval = new SearchControls();
        retval.setCountLimit(100);
        retval.setSearchScope(SearchControls.SUBTREE_SCOPE);
        retval.setReturningAttributes(null);
        retval.setTimeLimit(10000);
        return retval;
    }
}
