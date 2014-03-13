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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.*;
import piecework.enumeration.CacheName;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.SearchProvider;
import piecework.repository.ProcessInstanceRepository;
import piecework.repository.ProcessRepository;
import piecework.repository.BucketListRepository;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.security.data.DataFilterService;
import piecework.service.CacheService;
import piecework.service.IdentityService;
import piecework.task.TaskDeployment;
import piecework.task.TaskFactory;
import piecework.task.TaskFilter;
import piecework.task.TaskPageHandler;
import piecework.util.ProcessInstanceUtility;
import piecework.util.SearchUtility;

import java.util.*;

/**
 * @author James Renfro
 */
public class SearchRepositoryProvider implements SearchProvider {

    private static final Logger LOG = Logger.getLogger(SearchRepositoryProvider.class);

    private final ProcessRepository processRepository;
    private final ProcessInstanceRepository instanceRepository;
    private final BucketListRepository bucketListRepository;
    private final CacheService cacheService;
    private final DataFilterService dataFilterService;
    private final IdentityService identityService;
    private final Sanitizer sanitizer;
    private final Entity principal;

    public SearchRepositoryProvider(ProcessRepository processRepository, ProcessInstanceRepository instanceRepository,
                                    BucketListRepository bucketListRepository,
                                    CacheService cacheService, DataFilterService dataFilterService,
                                    IdentityService identityService, Sanitizer sanitizer, Entity principal) {
        this.processRepository = processRepository;
        this.instanceRepository = instanceRepository;
        this.bucketListRepository = bucketListRepository;
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
    public SearchResponse forms(SearchCriteria criteria, ViewContext context) throws PieceworkException {
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

        SearchResponse response = new SearchResponse();

        if (allowedProcesses == null || allowedProcesses.isEmpty())
            return response;

        List<Process> alphabetical = new ArrayList<Process>(allowedProcesses);
        Collections.sort(alphabetical, new Comparator<Process>() {
            @Override
            public int compare(Process o1, Process o2) {
                if (org.apache.commons.lang.StringUtils.isEmpty(o1.getProcessDefinitionLabel()))
                    return 0;
                if (org.apache.commons.lang.StringUtils.isEmpty(o2.getProcessDefinitionLabel()))
                    return 1;
                return o1.getProcessDefinitionLabel().compareTo(o2.getProcessDefinitionLabel());
            }
        });

        List<Map<String, String>> metadata = new ArrayList<Map<String, String>>();
        Set<String> pgs = new HashSet<String>();
        for (Process allowedProcess : alphabetical) {
            if (allowedProcess.getProcessDefinitionKey() != null) {
                Process definition = allowedProcess;
                Form form = new Form.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).task(new Task.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).processDefinitionLabel(definition.getProcessDefinitionLabel()).build(context)).build(context);
                Map<String, String> map = new HashMap<String, String>();
                map.put("processDefinitionKey", definition.getProcessDefinitionKey());
                map.put("processDefinitionLabel", definition.getProcessDefinitionLabel());
                map.put("link", form.getLink());
                metadata.add(map);
                if ( StringUtils.isNotEmpty(allowedProcess.getProcessGroup()) ) {
                     pgs.add(allowedProcess.getProcessGroup());
                }
            }
        }
        response.setMetadata(metadata);

        // bucket list stuff
        String pg = null;

        // get process group from allowed processes
        if ( pgs.size() == 1 ) {
            pg = pgs.toArray()[0].toString();
        } else { 
            // then try to get process group from query
            Map<String, List<String>> contentParameter = criteria.getContentParameters();
            if ( contentParameter != null ) {
                List<String> vlist = contentParameter.get("pg");
                if ( vlist != null && vlist.size() > 0 ) {
                    pg = vlist.get(0);
                }
            }
        }

        if ( pg != null && bucketListRepository != null ) {
            BucketList bucketList = bucketListRepository.findOne(pg);
            if ( bucketList != null ) {
                response.setBucketList(bucketList);
            }
        }



        String processStatus = criteria.getProcessStatus() != null ? sanitizer.sanitize(criteria.getProcessStatus()) : Constants.ProcessStatuses.OPEN;
        String taskStatus = criteria.getTaskStatus() != null ? sanitizer.sanitize(criteria.getTaskStatus()) : Constants.TaskStatuses.ALL;

        List<TaskDeployment> taskDeployments = new ArrayList<TaskDeployment>();
        Set<String> userIds = new HashSet<String>();

        List<Facet> facets = FacetFactory.facets(allowedProcesses);
        response.setFacets(facets);

        Map<DataFilterFacet, String> filterFacetParameters = criteria.getFilterFacetParameters();

        if (page.hasContent()) {
            // Loop again through the list to get all user ids and build the intermediate object including
            // task, instance, and deployment
            for (ProcessInstance instance : page.getContent()) {
                String processDefinitionKey = instance.getProcessDefinitionKey();
                String processInstanceId = instance.getProcessInstanceId();

                ProcessDeployment processDeployment = null;

                Map<String, Object> instanceData = new HashMap<String, Object>();

                instanceData.put("processInstanceId", processInstanceId);
                instanceData.put("processInstanceLabel", instance.getProcessInstanceLabel());
                instanceData.put("processDefinitionLabel", instance.getProcessDefinitionLabel());
                instanceData.put("processStatus", instance.getProcessStatus());
                instanceData.put("startTime", instance.getStartTime());
                instanceData.put("lastModifiedTime", instance.getLastModifiedTime());
                instanceData.put("endTime", instance.getEndTime());

                String activation = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "activation");
                String attachment = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, Attachment.Constants.ROOT_ELEMENT_NAME);
                String cancellation = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "cancellation");
                String history = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, History.Constants.ROOT_ELEMENT_NAME);
                String restart = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "restart");
                String suspension = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "suspension");
                String bucketUrl = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "value/Bucket");

                instanceData.put("activation", activation);
                instanceData.put("attachment", attachment);
                instanceData.put("cancellation", cancellation);
                instanceData.put("history", history);
                instanceData.put("restart", restart);
                instanceData.put("suspension", suspension);
                instanceData.put("bucketUrl", bucketUrl);

                Map<String, List<Value>> valueData = instance.getData();
                if (valueData != null && !valueData.isEmpty()) {
                    for (Facet facet : facets) {
                        if (facet instanceof DataSearchFacet) {
                            DataSearchFacet dataSearchFacet = DataSearchFacet.class.cast(facet);
                            String name = dataSearchFacet.getName();
                            String value = ProcessInstanceUtility.firstString(name, valueData);
                            if (StringUtils.isNotEmpty(value))
                                instanceData.put(name, value);
                        }
                    }
                }

                Set<Task> tasks = instance.getTasks();
                if (tasks != null && !tasks.isEmpty()) {
                    for (Task task : tasks) {
                        if (include(task, processStatus, taskStatus, overseerProcessDefinitionKeys, principal)) {
                            taskDeployments.add(new TaskDeployment(processDeployment, instance, task, instanceData));
                            userIds.addAll(task.getAssigneeAndCandidateAssigneeIds());
                        }
                    }
                }
            }

            Map<String, User> userMap = identityService.findUsers(userIds);

            List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

            for (TaskDeployment taskDeployment : taskDeployments) {
                Map<String, Object> map = new HashMap<String, Object>();
                Map<String, Object> instanceData = taskDeployment.getInstanceData();

                if (instanceData != null && !instanceData.isEmpty())
                    map.putAll(instanceData);

                Task task = TaskFactory.task(taskDeployment.getTask(), new PassthroughSanitizer(), userMap, context);
                String processDefinitionKey = task.getProcessDefinitionKey();

                if (!include(task, filterFacetParameters))
                    continue;

                map.put("assignee", task.getAssignee());
                map.put("candidateAssignees", task.getCandidateAssignees());

                map.put("formInstanceId", task.getTaskInstanceId());
                map.put("taskClaimTime", task.getClaimTime());
                map.put("taskDueDate", task.getDueDate());
                map.put("taskStartTime", task.getStartTime());
                map.put("taskEndTime", task.getEndTime());
                map.put("taskLabel", task.getTaskLabel());
                map.put("taskDescription", task.getTaskDescription());
                map.put("taskStatus", task.getTaskStatus());
                map.put("active", task.isActive());

                String assignment = context != null && task != null && task.getTaskInstanceId() != null ? context.getApplicationUri(Task.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, task.getTaskInstanceId(), "assign") : null;

                map.put("assignment", assignment);
                map.put("link", context != null ? context.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME, processDefinitionKey) + "?taskId=" + task.getTaskInstanceId() : null);

//                if (includeData) {
//                    Set<Field> fields = SecurityUtility.fields(activity, createAction);
//                    data = dataFilterService.unrestrictedInstanceData(instance, fields);
//                }

                data.add(map);
            }
            List<FacetSort> postQuerySortBy = criteria.getPostQuerySortBy();
            if (postQuerySortBy != null && !postQuerySortBy.isEmpty()) {
                Collections.reverse(postQuerySortBy);
                for (FacetSort facetSort : postQuerySortBy) {
                    Collections.sort(data, new DataFilterFacetComparator(facetSort.getFacet()));
                    if (facetSort.getDirection().equals(Sort.Direction.ASC))
                        Collections.reverse(data);
                }
            }

            response.setData(data);
            response.setTotal((int)page.getTotalElements());
        }

        List<FacetSort> facetSortList = criteria.getSortBy();
        List<String> sortBy = new ArrayList<String>();
        if (facetSortList != null) {
            for (FacetSort facetSort : facetSortList) {
                sortBy.add(facetSort.toString());
            }
        }
        response.setSortBy(sortBy);

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved tasks in " + (System.currentTimeMillis() - time) + " ms");

        if (principal instanceof User)
            response.setCurrentUser(User.class.cast(principal));

        return response;
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

    private static boolean include(Task task, Map<DataFilterFacet, String> filterFacetParameters) {
        if (filterFacetParameters != null && !filterFacetParameters.isEmpty()) {
            boolean doInclude = false;
            for (Map.Entry<DataFilterFacet, String> entry : filterFacetParameters.entrySet()) {
                DataFilterFacet facet = entry.getKey();
                String value = entry.getValue();

                if (facet.include(task, value)) {
                    doInclude = true;
                    break;
                }
            }
            if (!doInclude)
                return false;
        }
        return true;
    }

    private static boolean include(Task task, String processStatus, String taskStatus, Set<String> overseerProcessDefinitionKeys, Entity principal) {
        if (!processStatus.equals(Constants.ProcessStatuses.QUEUED)) {
            if (!processStatus.equals(Constants.ProcessStatuses.ALL) &&
                    !processStatus.equalsIgnoreCase(task.getTaskStatus()))
                return false;
        }

        if (!taskStatus.equals(Constants.TaskStatuses.ALL) &&
                !taskStatus.equalsIgnoreCase(task.getTaskStatus()))
            return false;

        return overseerProcessDefinitionKeys.contains(task.getProcessDefinitionKey())
                || task.isCandidateOrAssignee(principal);
    }
}
