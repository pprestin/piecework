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
import org.springframework.stereotype.Service;
import piecework.CommandExecutor;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.command.DeploymentCommand;
import piecework.command.PublicationCommand;
import piecework.engine.ProcessDeploymentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.*;
import piecework.identity.IdentityHelper;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.Streamable;
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
    CommandExecutor commandExecutor;

    @Autowired
    DeploymentRepository deploymentRepository;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ContentRepository contentRepository;

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

        return processRepository.save(builder.build());
    }

    public Process createAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment, ProcessDeploymentResource resource, boolean migrateExisting) throws StatusCodeError {
        Process process = create(rawProcess);
        ProcessDeployment deployment = createDeployment(process.getProcessDefinitionKey(), rawDeployment);
        deploy(process.getProcessDefinitionKey(), deployment.getDeploymentId(), resource);
        publishDeployment(process.getProcessDefinitionKey(), deployment.getDeploymentId());
        process = read(process.getProcessDefinitionKey());

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

    public ProcessDeployment createDeployment(String rawProcessDefinitionKey) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        List<ProcessDeploymentVersion> versions = process.getVersions();
        String deploymentVersion = versions != null ? "" + (versions.size() + 1) : "1";

        ProcessDeployment deployment = new ProcessDeployment.Builder().deploymentVersion(deploymentVersion).build();
        deployment = deploymentRepository.save(deployment);

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        Process updated = new Process.Builder(process, passthroughSanitizer)
                .version(new ProcessDeploymentVersion(deployment))
                .build();

        processRepository.save(updated);

        return deployment;
    }

    public ProcessDeployment createDeployment(String rawProcessDefinitionKey, ProcessDeployment rawDeployment) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);

        List<ProcessDeploymentVersion> versions = process.getVersions();
        String deploymentVersion = versions != null ? "" + (versions.size() + 1) : "1";
        ProcessDeployment deployment = cascadeSave(rawDeployment, deploymentVersion, true);

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        Process updated = new Process.Builder(process, passthroughSanitizer)
                .version(new ProcessDeploymentVersion(deployment))
                .build();

        processRepository.save(updated);

        return deployment;
    }

    public ProcessDeployment cloneDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

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
        ProcessDeployment deployment = cascadeSave(original, deploymentVersion, true);

        Process updated = new Process.Builder(process, passthroughSanitizer)
                .version(new ProcessDeploymentVersion(deployment))
                .build();

        processRepository.save(updated);

        return deployment;
    }

    public Process delete(String rawProcessDefinitionKey) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

        Process record = processRepository.findOne(processDefinitionKey);
        if (record == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);

        Process.Builder builder = new Process.Builder(record, sanitizer);
        builder.delete();
        return processRepository.save(builder.build());
    }

    public Activity deleteActivity(ProcessDeployment deployment, String rawActivityKey) throws StatusCodeError {
        String activityKey = sanitizer.sanitize(rawActivityKey);
        Activity deleted = deployment.getActivity(activityKey);
        if (deleted == null)
            return null;

        deploymentRepository.save(new ProcessDeployment.Builder(deployment, null, sanitizer, true).deleteActivity(activityKey).build());
        return deleted;
    }

    public void deleteDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        Process updated = new Process.Builder(process, sanitizer)
                .deleteDeployment(deploymentId)
                .build();

        processRepository.save(updated);
    }

    public void deleteContainer(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey, String rawContainerId) throws StatusCodeError {
        ProcessDeployment deployment = getDeployment(rawProcessDefinitionKey, rawDeploymentId);
        String activityKey = sanitizer.sanitize(rawActivityKey);
        String containerId = sanitizer.sanitize(rawContainerId);

        if (StringUtils.isEmpty(activityKey) || StringUtils.isEmpty(containerId))
            throw new BadRequestError();

        if (!deployment.isEditable())
            throw new ForbiddenError(Constants.ExceptionCodes.not_editable);

        ProcessDeployment updated = new ProcessDeployment.Builder(deployment, null, sanitizer, true)
                .deleteContainer(activityKey, containerId)
                .build();

        deploymentRepository.save(updated);
    }

    public Set<piecework.model.Process> findProcesses(Set<String> processDefinitionKeys) {
        if (processDefinitionKeys != null) {
            List<Process> processes = processRepository.findAllBasic(processDefinitionKeys);
            if (processes != null && !processes.isEmpty())
                return Collections.unmodifiableSet(new HashSet<Process>(processes));
        }

        return Collections.emptySet();
    }

    public Activity getActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityId) throws StatusCodeError {
        ProcessDeployment deployment = getDeployment(rawProcessDefinitionKey, rawDeploymentId);
        String activityKey = sanitizer.sanitize(rawActivityId);

        return deployment.getActivity(activityKey);
    }

    public ProcessDeployment getDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(original, process.getProcessDefinitionKey(), passthroughSanitizer, true);

        return builder.build();
    }

    public Streamable getDiagram(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment deployment = deploymentRepository.findOne(deploymentId);
        if (deployment == null)
            throw new NotFoundError();

        try {
            ProcessDeploymentResource resource = facade.resource(process, deployment, "image/png");
            return resource;
        } catch (ProcessEngineException e) {
            LOG.error("Could not generate diagram", e);
            throw new InternalServerError(e);
        }
    }

    public ProcessDeploymentResource getDeploymentResource(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeployment processDeployment = deploymentRepository.findOne(deploymentId);
        Content content = contentRepository.findByLocation(processDeployment.getEngineProcessDefinitionLocation());
        return new ProcessDeploymentResource.Builder(content).build();
    }

    public ProcessDeployment deploy(String rawProcessDefinitionKey, String rawDeploymentId, ProcessDeploymentResource resource) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        DeploymentCommand deploy = new DeploymentCommand(process, deploymentId, resource);

        return commandExecutor.execute(deploy);
    }

    public ProcessDeployment publishDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        PublicationCommand publish = new PublicationCommand(process, deploymentId);

        return commandExecutor.execute(publish);
    }

    public Process read(String rawProcessDefinitionKey) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

        Process result = processRepository.findOne(processDefinitionKey);

        if (result == null)
            throw new NotFoundError();
        if (result.isDeleted())
            throw new GoneError();

        return result;
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

    public SearchResults searchDeployments(String rawProcessDefinitionKey, MultivaluedMap<String, String> queryParameters) throws StatusCodeError {
        SearchResults.Builder resultsBuilder = new SearchResults.Builder().resourceLabel("Deployments").resourceName(ProcessDeployment.Constants.ROOT_ELEMENT_NAME);

        Process process = read(rawProcessDefinitionKey);
        List<ProcessDeploymentVersion> versions = process.getVersions();

        int count = 0;
        if (versions != null) {
            Set<String> deploymentIds = new HashSet<String>();
            for (ProcessDeploymentVersion version : versions) {
                if (!version.isDeleted()) {
                    deploymentIds.add(version.getDeploymentId());
                }
            }
            Iterable<ProcessDeployment> deployments = deploymentRepository.findAll(deploymentIds);
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

    public Process update(String rawProcessDefinitionKey, Process rawProcess) throws StatusCodeError {
        // Sanitize all user input
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

        Process original = processRepository.findOne(processDefinitionKey);
        Process update = new Process.Builder(rawProcess, sanitizer).build();

        Process.Builder builder = new Process.Builder(original, passthroughSanitizer);

        // Only certain fields can be updated through this method
        builder.processDefinitionKey(update.getProcessDefinitionKey())
               .processDefinitionLabel(update.getProcessDefinitionLabel())
               .participantSummary(update.getParticipantSummary())
               .processSummary(update.getProcessSummary());

        return processRepository.save(builder.build());
    }

    public ProcessDeployment updateActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey, Activity rawActivity) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
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

        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(original, process.getProcessDefinitionKey(), sanitizer, true);
        builder.activity(activityKey, modified.build());

        return cascadeSave(builder.build(), selectedDeploymentVersion.getVersion(), false);
    }

    public ProcessDeployment updateDeployment(String rawProcessDefinitionKey, String rawDeploymentId, ProcessDeployment rawDeployment) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        ProcessDeployment update = new ProcessDeployment.Builder(rawDeployment, process.getProcessDefinitionKey(), sanitizer, true).build();

        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(original, process.getProcessDefinitionKey(), sanitizer, true);
        builder.deploymentLabel(update.getDeploymentLabel());

        return cascadeSave(builder.build(), selectedDeploymentVersion.getVersion(), false);
    }

    public Process updateAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment, ProcessDeploymentResource resource, boolean migrateExisting) throws StatusCodeError {
        Process process = read(rawProcess.getProcessDefinitionKey());
                //update(rawProcess.getProcessDefinitionKey(), rawProcess);
        ProcessDeployment deployment = createDeployment(process.getProcessDefinitionKey(), rawDeployment);
        deploy(process.getProcessDefinitionKey(), deployment.getDeploymentId(), resource);
        publishDeployment(process.getProcessDefinitionKey(), deployment.getDeploymentId());
        process = read(process.getProcessDefinitionKey());

        if (migrateExisting)
            migrate(process);

        return process;
    }

    public String getVersion() {
        return "v1";
    }

    private ProcessDeployment cascadeSave(ProcessDeployment deployment, String deploymentVersion, boolean createNew) {
        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(deployment, null, sanitizer, false)
                .deploymentVersion(deploymentVersion);

        builder.clearActivities();
//        builder.clearInteractions();
//        builder.clearNotifications();
//        builder.clearSections();

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

//        Map<String, String> sectionIdMap = new HashMap<String, String>();
//        if (deployment.getSections() != null && !deployment.getSections().isEmpty()) {
//            for (Section section : deployment.getSections()) {
//                Section.Builder sectionBuilder = new Section.Builder(section, sanitizer);
//                if (createNew)
//                    sectionBuilder.sectionId(null);
//                Section persistedSection = sectionRepository.save(sectionBuilder.build());
//                builder.section(persistedSection);
//                if (StringUtils.isNotEmpty(section.getSectionId()))
//                    sectionIdMap.put(section.getSectionId(), persistedSection.getSectionId());
//            }
//        }
//        if (deployment.getInteractions() != null && !deployment.getInteractions().isEmpty()) {
//            for (Interaction interaction : deployment.getInteractions()) {
//                Interaction.Builder interactionBuilder = new Interaction.Builder(interaction, sanitizer);
//                if (createNew)
//                    interactionBuilder.id(null);
//
//                Map<ActionType, Screen> screens = interaction.getScreens();
//                if (screens != null && !screens.isEmpty()) {
//                    for (Map.Entry<ActionType, Screen> entry : screens.entrySet()) {
//                        Screen screen = entry.getValue();
//                        Screen.Builder screenBuilder = new Screen.Builder(screen, sanitizer, false);
//                        if (createNew)
//                            screenBuilder.screenId(null);
//
//                        List<Grouping> groupings = screen.getGroupings();
//                        if (groupings != null) {
//                            for (Grouping grouping : groupings) {
//                                Grouping.Builder groupingBuilder = new Grouping.Builder(grouping, sanitizer, false);
//                                List<String> sectionIds = grouping.getSectionIds();
//                                if (sectionIds != null) {
//                                    for (String sectionId : sectionIds) {
//                                        String persistedSectionId = sectionIdMap.get(sectionId);
//                                        if (StringUtils.isNotEmpty(persistedSectionId))
//                                            groupingBuilder.sectionId(persistedSectionId);
//                                    }
//                                }
//                                screenBuilder.grouping(groupingBuilder.build());
//                            }
//                        }
//
//                        Screen persistedScreen = screenRepository.save(screenBuilder.build());
//                        interactionBuilder.screen(entry.getKey(), persistedScreen);
//                    }
//                }
//                Interaction persistedInteraction = interactionRepository.save(interactionBuilder.build());
//                builder.interaction(persistedInteraction);
//            }
//        }
//        if (deployment.getNotifications() != null && !deployment.getNotifications().isEmpty()) {
//            for (Notification notification : deployment.getNotifications()) {
//                Notification.Builder notificationBuilder = new Notification.Builder(notification, sanitizer);
//                if (createNew)
//                    notificationBuilder.notificationId(null);
//                Notification persistedNotification = notificationRepository.save(notificationBuilder.build());
//                builder.notification(persistedNotification);
//            }
//        }

        return deploymentRepository.save(builder.build());
    }

}
