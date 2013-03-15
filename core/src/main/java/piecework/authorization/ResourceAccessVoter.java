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
package piecework.authorization;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.ws.rs.PathParam;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author James Renfro
 */
public class ResourceAccessVoter extends RoleVoter {

	private String processPrefix = "PROCESS_";
	
	@Override
	public boolean supports(Class<?> clazz) {
		return MethodInvocation.class.isAssignableFrom(clazz);
	}

	@Override
	public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
		int result = ACCESS_ABSTAIN;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (object instanceof MethodInvocation) {
        	MethodInvocation methodInvocation = MethodInvocation.class.cast(object);
        	
	        for (ConfigAttribute attribute : attributes) {
	            if (this.supports(attribute)) {
	                result = ACCESS_DENIED;
	
	                // Attempt to find a matching granted authority
	                for (GrantedAuthority authority : authorities) {
	                	String processDefinitionKey = getProcessDefinitionKey(methodInvocation);
	                
	                	if (authority instanceof ResourceAuthority) {
	                		ResourceAuthority resourceAuthority = ResourceAuthority.class.cast(authority);
	                		String roleAllowed = attribute.getAttribute();
	                		if (resourceAuthority.isAuthorized(roleAllowed, processDefinitionKey))
	                			return ACCESS_GRANTED;
	                	}	
	                }
	            }
	        }
        }

        return result;
	}
	
	private String getProcessDefinitionKey(MethodInvocation methodInvocation) {
		Method method = methodInvocation.getMethod();
    	Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    	Object[] arguments = methodInvocation.getArguments();
    	Class<?>[] parameterTypes = method.getParameterTypes();
    	
    	for (int i=0;i<parameterAnnotations.length;i++) {
    		if (parameterAnnotations[i] == null)
    			continue;
    		for (int j=0;j<parameterAnnotations[i].length;j++) {
    			Annotation annotation = parameterAnnotations[i][j];
    			if (annotation.equals(PathParam.class)) {
    				PathParam pathParam = PathParam.class.cast(annotation);
    				String name = pathParam.value();
    				if (name != null && name.equalsIgnoreCase("processDefinitionKey") && arguments.length > i && parameterTypes.length > i)
    					if (parameterTypes[i].equals(String.class))
    						return (String) arguments[i];
    			}
    		}
    	}
    	return null;
	}

	public String getProcessPrefix() {
		return processPrefix;
	}

	public void setProcessPrefix(String processPrefix) {
		this.processPrefix = processPrefix;
	}

}
