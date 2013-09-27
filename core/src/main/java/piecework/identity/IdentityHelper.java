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

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.authorization.ResourceAuthority;
import piecework.exception.BadRequestError;
import piecework.exception.GoneError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityDetails;
import piecework.model.Process;
import piecework.model.User;
import piecework.persistence.ProcessRepository;

import com.google.common.collect.Sets;
import piecework.service.IdentityService;

/**
 * @author James Renfro
 */
@Service
public class IdentityHelper {

	@Autowired
	ProcessRepository processRepository;

    @Autowired
    IdentityService identityService;

    public String getAuthenticatedSystemOrUserId() {
        String userId = null;
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication authentication = context.getAuthentication();

            if (authentication != null) {
                Object principalAsObject = authentication.getPrincipal();

                if (authentication.getCredentials() != null && authentication.getCredentials() instanceof X509Certificate) {
                    userId = principalAsObject.toString();
                } else if (principalAsObject instanceof IdentityDetails) {
                    IdentityDetails principal = IdentityDetails.class.cast(principalAsObject);
                    userId = principal.getInternalId();
                }
            }
        }
        return userId;
    }

    public User getCurrentUser() {
        IdentityDetails principal = getAuthenticatedPrincipal();
        if (principal == null)
            return null;

        return new User.Builder(principal).build();
    }

    public IdentityDetails getAuthenticatedPrincipal() {
        IdentityDetails principal = null;
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication authentication = context.getAuthentication();

            if (authentication != null) {
                Object principalAsObject = authentication.getPrincipal();
                if (principalAsObject instanceof IdentityDetails)
                    principal = IdentityDetails.class.cast(principalAsObject);
            }
        }
        return principal;
    }

    public boolean isAuthenticatedSystem() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication authentication = context.getAuthentication();

            if (authentication != null && authentication.getCredentials() instanceof X509Certificate) {
                return true;
            }
        }
        return false;
    }


    public Process findProcess(String processDefinitionKey, boolean isBadRequest) throws StatusCodeError {
        Process result = processRepository.findOne(processDefinitionKey);

        if (isBadRequest && (result == null || result.isDeleted()))
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);

        if (result == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist);

        if (result.isDeleted())
            throw new GoneError(Constants.ExceptionCodes.process_does_not_exist);

        return result;
    }

	public Set<piecework.model.Process> findProcesses(String ... allowedRoles) {
		SecurityContext context = SecurityContextHolder.getContext();
		Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
		
		Set<String> allowedRoleSet = allowedRoles != null && allowedRoles.length > 0 ? Sets.newHashSet(allowedRoles) : null;
		Set<String> allowedProcessDefinitionKeys = new HashSet<String>();
		if (authorities != null && !authorities.isEmpty()) {
			for (GrantedAuthority authority : authorities) {		
				if (authority instanceof ResourceAuthority) {
					ResourceAuthority resourceAuthority = ResourceAuthority.class.cast(authority);
					if (allowedRoleSet == null || allowedRoleSet.contains(resourceAuthority.getRole())) {
                        Set<String> processDefinitionKeys = resourceAuthority.getProcessDefinitionKeys();
                        if (processDefinitionKeys != null)
						    allowedProcessDefinitionKeys.addAll(processDefinitionKeys);
                    }
				}
			}
		}

        Set<piecework.model.Process> processes = new HashSet<piecework.model.Process>();
		Iterator<Process> iterator = processRepository.findAllBasic(allowedProcessDefinitionKeys).iterator();
		while (iterator.hasNext()) {
			Process record = iterator.next();
			if (!record.isDeleted())
				processes.add(record);
		}
		
		return processes;
	}

    public boolean hasRole(Process process, String ... allowedRoles) {
        if (process != null && StringUtils.isNotEmpty(process.getProcessDefinitionKey())) {
            SecurityContext context = SecurityContextHolder.getContext();
            Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();

            Set<String> allowedRoleSet = allowedRoles != null && allowedRoles.length > 0 ? Sets.newHashSet(allowedRoles) : null;
            if (authorities != null && !authorities.isEmpty()) {
                for (GrantedAuthority authority : authorities) {
                    if (authority instanceof ResourceAuthority) {
                        ResourceAuthority resourceAuthority = ResourceAuthority.class.cast(authority);
                        if (allowedRoleSet == null || allowedRoleSet.contains(resourceAuthority.getRole())) {
                            Set<String> processDefinitionKeys = resourceAuthority.getProcessDefinitionKeys();
                            if (processDefinitionKeys == null || processDefinitionKeys.contains(process.getProcessDefinitionKey()))
                                return true;
                        }
                    }
                }
            }
        }

        return false;
    }
	
}
