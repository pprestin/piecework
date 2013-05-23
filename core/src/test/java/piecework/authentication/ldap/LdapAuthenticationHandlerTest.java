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


/**
 * @author James Renfro
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(
//	classes={LdapAuthenticationConfiguration.class, ApplicationConfigurationForTest.class, StandaloneLdapConfiguration.class}, 
//	loader=AnnotationConfigContextLoader.class
//)
//@ActiveProfiles({"ldap", "test", "standalone"})
public class LdapAuthenticationHandlerTest {

//	@Autowired
//	AuthenticationHandler authenticationHandler;
//	
//	@Autowired
//	LdapContextSource personLdapContextSource;
//	
//	@Autowired
//	LdapContextSource groupLdapContextSource;
//	
//	@MyTest
//	public void testSimpleAuthentication() {
//		SpringSecurityLdapTemplate ldapTemplate = new SpringSecurityLdapTemplate(personLdapContextSource);
//		
//		boolean isAuthenticated = ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, "uid=rod", "koala");
//		Assert.assertTrue(isAuthenticated);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@MyTest
//	public void testAuthenticationHandler() throws SecurityException, NoSuchMethodException {
//
//		String id = "rod";
//		String password = "koala";
//		
//		final Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
//		
//		Message message = Mockito.mock(Message.class);
//		Exchange exchange = Mockito.mock(Exchange.class);
//		
//		ClassResourceInfo resourceClass = Mockito.mock(ClassResourceInfo.class);
//		Mockito.when(message.get(SecurityToken.class)).thenReturn(new UsernameToken(id, password, null, false, null, null));
//		Mockito.when(message.get("org.apache.cxf.resource.method")).thenReturn(FormResourceVersion1.class.getMethod("read", String.class));
//		Mockito.doAnswer(new SecurityContextStorageAnswer(map)).when(message).put(Mockito.any(Class.class), Mockito.any());
//		Mockito.when(message.get(SecurityContext.class)).then(new SecurityContextRetrievalAnswer(map));
//		Mockito.when(exchange.get(Mockito.any())).thenReturn(null);
//		Mockito.when(message.getExchange()).thenReturn(exchange);
//		
//		Response response = authenticationHandler.handleRequest(message, resourceClass);
//	
//		Assert.assertNull(response);
//	}
//
//	public class SecurityContextStorageAnswer implements Answer<Void> {
//		
//		private final Map<Class<?>, Object> map;
//		
//		public SecurityContextStorageAnswer(Map<Class<?>, Object> map) {
//			this.map = map;
//		}
//		
//	    @Override
//	    public Void answer(InvocationOnMock invocation) throws Throwable {
//	        Object[] arguments = invocation.getArguments();
//	        if (arguments != null && arguments.length > 1) {
//	        	Class<?> key = (Class<?>)arguments[0];
//	        	SecurityContext value = (SecurityContext) arguments[1];
//	        	map.put(key, value);
//	        }
//	        return null;
//	    }
//	}
//	
//	public class SecurityContextRetrievalAnswer implements Answer<SecurityContext> {
//		
//		private final Map<Class<?>, Object> map;
//		
//		public SecurityContextRetrievalAnswer(Map<Class<?>, Object> map) {
//			this.map = map;
//		}
//		
//		@Override
//	    public SecurityContext answer(InvocationOnMock invocation) throws Throwable {
//	        Object[] arguments = invocation.getArguments();
//	        if (arguments != null && arguments.length > 0 && arguments[0] != null) {
//	        	Class<?> key = (Class<?>)arguments[0];
//	        	return (SecurityContext) map.get(key);
//	        }
//	        return null;
//	    }
//	}
}
