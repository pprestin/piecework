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
package piecework.persistence.concrete;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import piecework.authorization.AuthorizationRole;
import piecework.common.FacetFactory;
import piecework.common.SearchCriteria;
import piecework.common.ViewContext;
import piecework.enumeration.CacheName;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.SearchProvider;
import piecework.repository.ProcessInstanceRepository;
import piecework.repository.ProcessRepository;
import piecework.security.Sanitizer;
import piecework.security.data.DataFilterService;
import piecework.service.CacheService;
import piecework.service.IdentityService;
import piecework.task.TaskFilter;
import piecework.task.TaskPageHandler;
import piecework.util.SearchUtility;

import java.util.*;

/**
 * @author James Renfro
 */
public class SearchRepositoryProvider implements SearchProvider {

    private static final Logger LOG = Logger.getLogger(SearchRepositoryProvider.class);

    private final ProcessRepository processRepository;
    private final ProcessInstanceRepository instanceRepository;
    private final CacheService cacheService;
    private final DataFilterService dataFilterService;
    private final IdentityService identityService;
    private final Sanitizer sanitizer;
    private final Entity principal;

    public SearchRepositoryProvider(ProcessRepository processRepository, ProcessInstanceRepository instanceRepository,
                                    CacheService cacheService, DataFilterService dataFilterService,
                                    IdentityService identityService, Sanitizer sanitizer, Entity principal) {
        this.processRepository = processRepository;
        this.instanceRepository = instanceRepository;
        this.cacheService = cacheService;
        this.dataFilterService = dataFilterService;
        this.identityService = identityService;
        this.sanitizer = sanitizer;
        this.principal = principal;
    }

    @Override
    public SearchResults facets(String label, ViewContext context) throws PieceworkException {
        Set<String> overseerProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER);
        Set<String> userProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.USER);

        Set<String> allProcessDefinitionKeys = Sets.union(overseerProcessDefinitionKeys, userProcessDefinitionKeys);
        Set<piecework.model.Process> allowedProcesses = processes(allProcessDefinitionKeys);

        List<Facet> facets = FacetFactory.facets(allowedProcesses);

        return new SearchResults.Builder()
                .items(facets)
                .total(Long.valueOf(facets.size()))
                .build();
    }

    @Override
    public SearchResults forms(SearchCriteria criteria, ViewContext context) throws PieceworkException {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Set<String> overseerProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER);
        Set<String> userProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.USER);

        Set<String> allProcessDefinitionKeys = Sets.union(overseerProcessDefinitionKeys, userProcessDefinitionKeys);
        Set<piecework.model.Process> allowedProcesses = processes(allProcessDefinitionKeys);

        Pageable pageable = SearchUtility.pageable(criteria, sanitizer);

        TaskFilter taskFilter = new TaskFilter(dataFilterService, principal, overseerProcessDefinitionKeys, true, false);
        TaskPageHandler pageHandler = new TaskPageHandler(criteria, taskFilter, sanitizer, context){

            @Override
            protected Map<String, User> getUserMap(Set<String> userIds) {
                return identityService.findUsers(userIds);
            }

        };

        Page<ProcessInstance> page = instanceRepository.findByCriteria(allProcessDefinitionKeys, criteria, pageable, sanitizer);

        SearchResults results = pageHandler.handle(page, pageable, allowedProcesses);

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved tasks in " + (System.currentTimeMillis() - time) + " ms");

        return results;
    }

    public Set<Process> processes(String ... allowedRoles) {
       Set<String> processDefinitionKeys = principal().getProcessDefinitionKeys(allowedRoles);
        return processes(processDefinitionKeys);
    }

    public Set<Process> processes(Set<String> processDefinitionKeys) {
        long start = 0;
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();

        Set<Process> allProcesses = new HashSet<Process>();
        if (processDefinitionKeys != null) {
            // Check the cache for any processes that have been cached -- note that these process
            // objects only have a subset of their fields populated and so this cache shouldn't be used elsewhere
            Set<String> uncachedProcessDefinitionKeys = new HashSet<String>();
            for (String processDefinitionKey : processDefinitionKeys) {
                Cache.ValueWrapper wrapper = cacheService.get(CacheName.PROCESS_BASIC, processDefinitionKey);
                if (wrapper == null) {
                    uncachedProcessDefinitionKeys.add(processDefinitionKey);
                } else {
                    Process process = Process.class.cast(wrapper.get());
                    if (process != null) {
                        if (LOG.isDebugEnabled())
                            LOG.debug("Retrieving basic process definition from cache for " + processDefinitionKey);
                        allProcesses.add(process);
                    }
                }
            }
            // Look for any that were not cached
            List<Process> processes = uncachedProcessDefinitionKeys.isEmpty() ? Collections.<Process>emptyList() : processRepository.findAllBasic(uncachedProcessDefinitionKeys);
            if (processes != null && !processes.isEmpty()) {
                for (Process process : processes) {
                    cacheService.put(CacheName.PROCESS_BASIC, process.getProcessDefinitionKey(), process);
                    allProcesses.add(process);
                }
            }
        }
        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved basic process definitions in " + (System.currentTimeMillis() - start) + " ms");

        return Collections.unmodifiableSet(allProcesses);
    }

    @Override
    public SearchResults tasks(SearchCriteria criteria, ViewContext context) throws PieceworkException {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Set<String> overseerProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER);
        Set<String> userProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.USER);

        Set<String> allProcessDefinitionKeys = Sets.union(overseerProcessDefinitionKeys, userProcessDefinitionKeys);
        Set<piecework.model.Process> allowedProcesses = processes(allProcessDefinitionKeys);

        Pageable pageable = SearchUtility.pageable(criteria, sanitizer);

        TaskFilter taskFilter = new TaskFilter(dataFilterService, principal, overseerProcessDefinitionKeys, false, false);
        TaskPageHandler pageHandler = new TaskPageHandler(criteria, taskFilter, sanitizer, context){

//            protected Map<String, ProcessDeployment> getDeploymentMap(Set<String> deploymentIds) {
//                return deploymentService.getDeploymentMap(deploymentIds);
//            }

            @Override
            protected Map<String, User> getUserMap(Set<String> userIds) {
                return identityService.findUsers(userIds);
            }

        };

        Page<ProcessInstance> page = instanceRepository.findByCriteria(allProcessDefinitionKeys, criteria, pageable, sanitizer);

        SearchResults results = pageHandler.handle(page, pageable, allowedProcesses);

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved tasks in " + (System.currentTimeMillis() - time) + " ms");

        return results;
    }

    @Override
    public Entity principal() {
        return principal;
    }
}
