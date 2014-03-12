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
package piecework.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.command.CommandFactory;
import piecework.content.ContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.CacheName;
import piecework.exception.InternalServerError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.ActivityRepository;
import piecework.repository.ContentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.repository.ProcessRepository;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ProcessUtility;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ProcessService {

    private static final Logger LOG = Logger.getLogger(ProcessService.class);

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    CacheService cacheService;

    @Autowired
    CommandFactory commandFactory;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    Versions versions;

    public Process create(Process rawProcess) {
        piecework.model.Process.Builder builder = new Process.Builder(rawProcess, sanitizer);

        Process process = builder.build();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        builder = new Process.Builder(process, passthroughSanitizer);

        return persist(builder.build());
    }

    public Process createAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment, ContentResource resource, boolean migrateExisting, Entity principal) throws PieceworkException {
        Process process = create(rawProcess);

        ProcessProvider processProvider = modelProviderFactory.processProvider(process.getProcessDefinitionKey(), principal);
        ProcessDeployment deployment = createDeployment(processProvider, rawDeployment);
        deploy(process.getProcessDefinitionKey(), deployment.getDeploymentId(), resource);
        publishDeployment(process.getProcessDefinitionKey(), deployment.getDeploymentId(), principal);
        process = read(process.getProcessDefinitionKey(), principal);

        if (migrateExisting)
            migrate(process);

        return process;
    }

    private void migrate(Process process) {
        List<ProcessInstance> all = processInstanceRepository.findByProcessDefinitionKey(process.getProcessDefinitionKey());

        if (all != null && !all.isEmpty()) {
            for (ProcessInstance instance : all) {
                ProcessInstance.Builder builder = new ProcessInstance.Builder(instance).deploymentId(process.getDeploymentId());
                processInstanceRepository.save(builder.build());
            }
        }
    }

//    public ProcessDeployment createDeployment(String rawProcessDefinitionKey, Entity principal) throws PieceworkException {
//        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, principal);
//        ProcessDeployment deployment = deploymentService.create(process);
//
//        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
//        Process updated = new Process.Builder(process, passthroughSanitizer)
//                .version(new ProcessDeploymentVersion(deployment))
//                .build();
//
//        persist(updated);
//
//        return deployment;
//    }

    public ProcessDeployment createDeployment(ProcessProvider processProvider, ProcessDeployment rawDeployment) throws PieceworkException {
        Process process = processProvider.process();
        ProcessDeployment deployment = deploymentService.create(process, rawDeployment);
        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        Process updated = new Process.Builder(process, passthroughSanitizer)
                .version(new ProcessDeploymentVersion(deployment))
                .build();

        persist(updated);

        return deployment;
    }

    public ProcessDeployment cloneDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws PieceworkException {
        Process process = read(rawProcessDefinitionKey, helper.getPrincipal());
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeployment deployment = deploymentService.clone(process, deploymentId);
        Process updated = new Process.Builder(process, new PassthroughSanitizer())
                .version(new ProcessDeploymentVersion(deployment))
                .build();

        persist(updated);

        return deployment;
    }

    public Process delete(String rawProcessDefinitionKey) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

        Process record = processRepository.findOne(processDefinitionKey);
        if (record == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);

        Process.Builder builder = new Process.Builder(record, sanitizer);
        builder.delete();
        return persist(builder.build());
    }

    public void deleteDeployment(String rawProcessDefinitionKey, String rawDeploymentId, Entity principal) throws PieceworkException {
        Process process = read(rawProcessDefinitionKey, principal);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        Process updated = new Process.Builder(process, sanitizer)
                .deleteDeployment(deploymentId)
                .build();

        persist(updated);
    }

    public Set<Process> findAllProcesses() {
        return Collections.unmodifiableSet(new HashSet<Process>(processRepository.findAllBasic()));
    }

    public Set<piecework.model.Process> findProcesses(Set<String> processDefinitionKeys) {
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

    public ContentResource getDiagram(String rawProcessDefinitionKey, String rawDeploymentId, Entity principal) throws PieceworkException {
        Process process = read(rawProcessDefinitionKey, principal);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment deployment = deploymentService.read(process, deploymentId);

        try {
            ContentResource resource = facade.resource(process, deployment, "image/png");
            return resource;
        } catch (ProcessEngineException e) {
            LOG.error("Could not generate diagram", e);
            throw new InternalServerError(e);
        }
    }

    public ContentResource getDeploymentResource(String rawProcessDefinitionKey, String rawDeploymentId, Entity principal) throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, principal);
        Process process = read(rawProcessDefinitionKey, principal);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeployment deployment = deploymentService.read(process, deploymentId);

        return contentRepository.findByLocation(deploymentProvider, deployment.getEngineProcessDefinitionLocation());
    }

    public ProcessDeployment deploy(String rawProcessDefinitionKey, String rawDeploymentId, ContentResource resource) throws PieceworkException {
        ProcessDeploymentProvider modelProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, helper.getPrincipal());
        String deploymentId = sanitizer.sanitize(rawDeploymentId);
        return commandFactory.deployment(modelProvider, deploymentId, resource).execute();
    }

    public ProcessDeployment publishDeployment(String rawProcessDefinitionKey, String rawDeploymentId, Entity principal) throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, principal);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        return commandFactory.publication(deploymentProvider, deploymentId).execute();
    }

    private Process read(String rawProcessDefinitionKey, Entity principal) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, principal);
        return processProvider.process();
//        long start = 0;
//        if (LOG.isDebugEnabled())
//            start = System.currentTimeMillis();
//
//        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
//
//        Process result;
//        Cache.ValueWrapper value = cacheService.get(CacheName.PROCESS, processDefinitionKey);
//        if (value != null) {
//            result = Process.class.cast(value.get());
//        } else {
//            result = processRepository.findOne(processDefinitionKey);
//            cacheService.put(CacheName.PROCESS, processDefinitionKey, result);
//        }
//
//        if (LOG.isDebugEnabled())
//            LOG.debug("Retrieved full process definition for " + processDefinitionKey + " in " + (System.currentTimeMillis() - start) + " ms");
//
//        if (result == null)
//            throw new NotFoundError();
//        if (result.isDeleted())
//            throw new GoneError();
//
//        return result;
    }

    public SearchResults search(MultivaluedMap<String, String> queryParameters, Entity principal) {
        List<Process> results;
        Set<String> processDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OWNER, AuthorizationRole.CREATOR);
        Set<Process> processes = findProcesses(processDefinitionKeys);

        if (processes != null && !processes.isEmpty()) {
            results = new ArrayList<Process>(processes.size());
            for (Process process : processes) {
                results.add(new Process.Builder(process, sanitizer).build(versions.getVersion1()));
            }
        } else {
            results = Collections.emptyList();
        }

        SearchResults.Builder resultsBuilder = new SearchResults.Builder().resourceLabel("Processes").resourceName(Process.Constants.ROOT_ELEMENT_NAME);

        int firstResult = processes.size() > 0 ? 1 : 0;
        resultsBuilder.items(results);
        resultsBuilder.firstResult(0);
        resultsBuilder.maxResults(processes.size());
        resultsBuilder.firstResult(firstResult);
        resultsBuilder.total(Long.valueOf(processes.size()));

        return resultsBuilder.build();
    }

    public Process update(String rawProcessDefinitionKey, Process rawProcess, Entity principal) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, principal);

        Process original = processProvider.process();
        if (original == null)
            throw new NotFoundError();

        Process update = new Process.Builder(rawProcess, sanitizer).build();

        Process.Builder builder = new Process.Builder(original, new PassthroughSanitizer());

        // Only certain fields can be updated through this method
        builder.processDefinitionKey(update.getProcessDefinitionKey())
               .processDefinitionLabel(update.getProcessDefinitionLabel())
               .participantSummary(update.getParticipantSummary())
               .processSummary(update.getProcessSummary())
               .facets(update.getFacets())
               .allowAnonymousSubmission(update.isAnonymousSubmissionAllowed())
               .assignmentRestrictedToCandidates(update.isAssignmentRestrictedToCandidates());

        return persist(builder.build());
    }

    public Process updateAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment, ContentResource resource, boolean migrateExisting, Entity principal) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcess.getProcessDefinitionKey(), principal);

        String processDefinitionKey = processProvider.processDefinitionKey();
        ProcessDeployment deployment = createDeployment(processProvider, rawDeployment);
        deploy(processDefinitionKey, deployment.getDeploymentId(), resource);
        publishDeployment(processDefinitionKey, deployment.getDeploymentId(), principal);
        Process process = modelProviderFactory.processProvider(processDefinitionKey, principal).process();

        if (migrateExisting)
            migrate(process);

        return process;
    }

    private Process persist(Process process) {
        Process stored = processRepository.save(process);
        cacheService.put(CacheName.PROCESS, process.getProcessDefinitionKey(), stored);
        return stored;
    }

}
