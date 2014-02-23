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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.enumeration.CacheName;
import piecework.enumeration.DataInjectionStrategy;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessProvider;
import piecework.repository.ActivityRepository;
import piecework.repository.DeploymentRepository;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ProcessUtility;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

/**
 * ProcessDeployment documents are expensive to retrieve because they currently contain
 * a map of Activity documents using a DBRef annotation. This service should abstract
 * the work of getting them to facilitate efficient caching and minimize on retrievals.
 *
 * At the same time, since ProcessDeployment documents can change (rarely) with significant
 * impact on the overall system, it's important to work out a strategy for evicting modified
 * deployment objects from the cache.
 *
 * @author James Renfro
 */
@Service
public class DeploymentService {

    private static final Logger LOG = Logger.getLogger(DeploymentService.class);

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    CacheService cacheService;

    @Autowired
    DeploymentRepository deploymentRepository;

    @Autowired
    Sanitizer sanitizer;

    public ProcessDeployment create(ProcessProvider processProvider) throws PieceworkException {
        Process process = processProvider.process();
        List<ProcessDeploymentVersion> versions = process.getVersions();
        String deploymentVersion = versions != null ? "" + (versions.size() + 1) : "1";

        ProcessDeployment deployment = new ProcessDeployment.Builder().deploymentVersion(deploymentVersion).build();
        return deploymentRepository.save(deployment);
    }

    public ProcessDeployment create(Process process, ProcessDeployment rawDeployment) {
        List<ProcessDeploymentVersion> versions = process.getVersions();
        String deploymentVersion = versions != null ? "" + (versions.size() + 1) : "1";
        return cascadeSave(rawDeployment, deploymentVersion, true);
    }

    public ProcessDeployment clone(Process process, String deploymentId) throws NotFoundError {
        List<ProcessDeploymentVersion> versions = process.getVersions();
        String deploymentVersion = versions != null ? "" + (versions.size() + 1) : "1";

        ProcessDeployment original = null;

        for (ProcessDeploymentVersion version : versions) {
            if (version.getDeploymentId().equals(deploymentId)) {
                original = deploymentRepository.findOne(deploymentId);
                break;
            }
        }

        if (original == null)
            throw new NotFoundError();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        return cascadeSave(original, deploymentVersion, true);
    }

    public ProcessDeployment read(Process process, ProcessInstance instance) throws MisconfiguredProcessException {
        // Make sure we have a process
        if (process == null)
            throw new MisconfiguredProcessException("No process available, cannot look for deployment");

        // Always use the current deployment when starting a new execution, but for executions
        // that are already running (that have an instance) it may be necessary to retrieve them
        // on an ad-hoc basis
        ProcessDeployment deployment = process.getDeployment();

        // Various other sanity checks
        if (deployment == null || StringUtils.isEmpty(deployment.getDeploymentId()))
            throw new MisconfiguredProcessException("No deployment or deployment id is empty", process.getProcessDefinitionKey());

        // It's okay not to have an instance here, but if we have one, it needs to have a deployment id
        if (instance != null && StringUtils.isEmpty(instance.getDeploymentId()))
            throw new MisconfiguredProcessException("Instance deployment id is empty", process.getProcessDefinitionKey());

        if (!useCurrentDeployment(instance, deployment)) {
            String deploymentId = instance.getDeploymentId();
            return deploymentRepository.findOne(deploymentId);
        }
        return deployment;
    }

    public ProcessDeployment read(Process process, String rawDeploymentId) throws StatusCodeError {
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(original, passthroughSanitizer, true);

        return builder.build();
    }

    public SearchResults search(Process process, MultivaluedMap<String, String> queryParameters) throws StatusCodeError {
        SearchResults.Builder resultsBuilder = new SearchResults.Builder().resourceLabel("Deployments").resourceName(ProcessDeployment.Constants.ROOT_ELEMENT_NAME);
        List<ProcessDeploymentVersion> versions = process.getVersions();

        int count = 0;
        if (versions != null) {
            Set<String> deploymentIds = new HashSet<String>();
            for (ProcessDeploymentVersion version : versions) {
                if (!version.isDeleted()) {
                    deploymentIds.add(version.getDeploymentId());
                }
            }
            Iterable<ProcessDeployment> deployments = retrieveDeployments(deploymentIds);
            if (deployments != null) {
                for (ProcessDeployment deployment : deployments) {
                    resultsBuilder.item(deployment);
                    count++;
                }
            }
        }

        resultsBuilder.maxResults(count);
        resultsBuilder.firstResult(1);
        resultsBuilder.total(Long.valueOf(count));

        return resultsBuilder.build();
    }

    public ProcessDeployment update(Process process, String rawDeploymentId, ProcessDeployment rawDeployment) throws StatusCodeError {
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        ProcessDeployment update = new ProcessDeployment.Builder(rawDeployment, sanitizer, true).build();

        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(original, sanitizer, true);
        builder.deploymentLabel(update.getDeploymentLabel());

        return cascadeSave(builder.build(), selectedDeploymentVersion.getVersion(), false);
    }

    public Activity deleteActivity(ProcessDeployment deployment, String rawActivityKey) {
        String activityKey = sanitizer.sanitize(rawActivityKey);
        Activity deleted = deployment.getActivity(activityKey);
        if (deleted == null)
            return null;

        ProcessDeployment stored = deploymentRepository.save(new ProcessDeployment.Builder(deployment, sanitizer, true).deleteActivity(activityKey).build());
        cache(stored);
        return deleted;
    }

    public void deleteContainer(Process process, String rawDeploymentId, String rawActivityKey, String rawContainerId) throws StatusCodeError {
        ProcessDeployment deployment = read(process, rawDeploymentId);
        String activityKey = sanitizer.sanitize(rawActivityKey);
        String containerId = sanitizer.sanitize(rawContainerId);

        if (StringUtils.isEmpty(activityKey) || StringUtils.isEmpty(containerId))
            throw new BadRequestError();

        if (!deployment.isEditable())
            throw new ForbiddenError(Constants.ExceptionCodes.not_editable);

        ProcessDeployment updated = new ProcessDeployment.Builder(deployment, sanitizer, true)
                .deleteContainer(activityKey, containerId)
                .build();

        ProcessDeployment stored = deploymentRepository.save(updated);
        cache(stored);
    }

    public Activity getActivity(Process process, String rawDeploymentId, String rawActivityId) throws StatusCodeError {
        ProcessDeployment deployment = read(process, rawDeploymentId);
        String activityKey = sanitizer.sanitize(rawActivityId);
        return deployment.getActivity(activityKey);
    }

    public Map<String, ProcessDeployment> getDeploymentMap(Set<String> deploymentIds) {
        Map<String, ProcessDeployment> deploymentMap = new HashMap<String, ProcessDeployment>();
        if (!deploymentIds.isEmpty()) {
            Iterable<ProcessDeployment> deployments = retrieveDeployments(deploymentIds);
            if (deployments != null) {
                for (ProcessDeployment deployment : deployments) {
                    deploymentMap.put(deployment.getDeploymentId(), deployment);
                }
            }
        }

        return deploymentMap;
    }

    public ProcessDeployment updateActivity(Process process, String rawDeploymentId, String rawActivityKey, Activity rawActivity) throws StatusCodeError {
        String deploymentId = sanitizer.sanitize(rawDeploymentId);
        String activityKey = sanitizer.sanitize(rawActivityKey);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        Activity originalActivity = original.getActivity(activityKey);
        Activity.Builder modified = new Activity.Builder(originalActivity, new PassthroughSanitizer());
        Activity update = new Activity.Builder(rawActivity, sanitizer).build();
        modified.usageType(update.getUsageType());

        Map<ActionType, Action> originalActionMap =  originalActivity.getActionMap();
        Map<ActionType, Action> updateActionMap = update.getActionMap();

        if (originalActionMap != null && updateActionMap != null) {
            for (Map.Entry<ActionType, Action> entry : originalActionMap.entrySet()) {
                ActionType type = entry.getKey();
                Action action = entry.getValue();

                Action updateAction = updateActionMap.get(type);

                // Only replace the update location and not the container -- container changes must
                // use the updateContainer method
                Container originalContainer = action.getContainer();
                String updateLocation = updateAction != null ? updateAction.getLocation() : null;
                DataInjectionStrategy updateStrategy = updateAction != null ? updateAction.getStrategy() : DataInjectionStrategy.NONE;
                modified.action(type, new Action(originalContainer, updateLocation, updateStrategy));
            }
        }

        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(original, sanitizer, true);
        builder.activity(activityKey, modified.build());

        return cascadeSave(builder.build(), selectedDeploymentVersion.getVersion(), false);
    }

    private Collection<ProcessDeployment> retrieveDeployments(Collection<String> deploymentIds) {
        long start = 0;
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();

        Set<ProcessDeployment> allDeployments = new HashSet<ProcessDeployment>();
        if (deploymentIds != null) {
            // Check the cache for any deployments that have been cached
            Set<String> uncachedDeploymentIds = new HashSet<String>();
            for (String deploymentId : deploymentIds) {
                Cache.ValueWrapper wrapper = cacheService.get(CacheName.PROCESS_DEPLOYMENT, deploymentId);
                if (wrapper == null) {
                    uncachedDeploymentIds.add(deploymentId);
                } else {
                    ProcessDeployment deployment = ProcessDeployment.class.cast(wrapper.get());
                    if (deployment != null) {
                        if (LOG.isDebugEnabled())
                            LOG.debug("Retrieving deployment from cache for " + deploymentId);
                        allDeployments.add(deployment);
                    }
                }
            }
            // Look for any that were not cached
            Iterable<ProcessDeployment> deployments = uncachedDeploymentIds.isEmpty() ? Collections.<ProcessDeployment>emptyList() : deploymentRepository.findAll(deploymentIds);
            if (deployments != null) {
                for (ProcessDeployment deployment : deployments) {
                    cacheService.put(CacheName.PROCESS_DEPLOYMENT, deployment.getDeploymentId(), deployment);
                    allDeployments.add(deployment);
                }
            }
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved deployments for " + deploymentIds.size() + " deployment ids in " + (System.currentTimeMillis() - start) + " ms");

        return Collections.unmodifiableSet(allDeployments);
    }

    private ProcessDeployment cascadeSave(ProcessDeployment deployment, String deploymentVersion, boolean createNew) {
        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(deployment, sanitizer, false)
                .deploymentVersion(deploymentVersion);

        builder.clearActivities();

        if (createNew)
            builder.deploymentId(null);
        builder.published(false);

        Map<String, Activity> activityMap = deployment.getActivityMap();
        if (activityMap != null) {
            for (Map.Entry<String, Activity> entry : deployment.getActivityMap().entrySet()) {
                String key = sanitizer.sanitize(entry.getKey());
                if (key == null)
                    continue;
                if (entry.getValue() == null)
                    continue;

                builder.activity(key, activityRepository.save(new Activity.Builder(entry.getValue(), sanitizer).build()));
            }
        }

        ProcessDeployment stored = deploymentRepository.save(builder.build());
        cache(stored);
        return stored;
    }

    private void cache(ProcessDeployment deployment) {
        cacheService.put(CacheName.PROCESS_DEPLOYMENT, deployment.getDeploymentId(), deployment);
    }

    private boolean useCurrentDeployment(ProcessInstance instance, ProcessDeployment deployment) {
        // If instance is null then we have no choice
        if (instance == null)
            return true;

        // If the instance has a deployment id and it's the same as the one passed then it means this instance is on the current deployment
        String deploymentId = instance.getDeploymentId();
        return deploymentId.equals(deployment.getDeploymentId());
    }

}
