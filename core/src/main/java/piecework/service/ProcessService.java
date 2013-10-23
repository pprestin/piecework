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
    InteractionRepository interactionRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    ScreenRepository screenRepository;

    @Autowired
    SectionRepository sectionRepository;

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

    public Process createAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment, ProcessDeploymentResource resource) throws StatusCodeError {
        Process process = create(rawProcess);
        ProcessDeployment deployment = createDeployment(process.getProcessDefinitionKey(), rawDeployment);
        deploy(process.getProcessDefinitionKey(), deployment.getDeploymentId(), resource);
        publishDeployment(process.getProcessDefinitionKey(), deployment.getDeploymentId());
        process = read(process.getProcessDefinitionKey());
        return process;
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

    public void deleteDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        Process updated = new Process.Builder(process, sanitizer)
                .deleteDeployment(deploymentId)
                .build();

        processRepository.save(updated);
    }

    public void deleteSection(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, String rawActionTypeId, String rawGroupingId, String rawSectionId) throws StatusCodeError {
        ProcessDeployment deployment = getDeployment(rawProcessDefinitionKey, rawDeploymentId);
        String interactionId = sanitizer.sanitize(rawInteractionId);
        String actionTypeId = sanitizer.sanitize(rawActionTypeId);
        String groupingId = sanitizer.sanitize(rawGroupingId);
        String sectionId = sanitizer.sanitize(rawSectionId);

        if (StringUtils.isEmpty(interactionId) || StringUtils.isEmpty(sectionId) || StringUtils.isEmpty(actionTypeId))
            throw new BadRequestError();

        if (!deployment.isEditable())
            throw new ForbiddenError(Constants.ExceptionCodes.not_editable);

        ActionType actionType = ActionType.valueOf(actionTypeId);

        ProcessDeployment updated = new ProcessDeployment.Builder(deployment, null, sanitizer, true)
                .deleteScreenGroupingSection(interactionId, actionType, groupingId, sectionId)
                .build();

        deploymentRepository.save(updated);
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
        builder.clearInteractions();

        List<Interaction> interactions = original.getInteractions();
        if (interactions != null) {
            Map<String, Section> sectionMap = original.getSectionMap();
            for (Interaction interaction : interactions) {
                Interaction decorated = ProcessUtility.interaction(interaction, sectionMap, versions.getVersion1());
                if (decorated != null)
                    builder.interaction(decorated);
            }
        }

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

    public Interaction getInteraction(ProcessDeployment deployment, String rawInteractionId) throws StatusCodeError {
        String interactionId = sanitizer.sanitize(rawInteractionId);

        List<Interaction> interactions = deployment.getInteractions();
        Map<String, Section> sectionMap = deployment.getSectionMap();
        if (interactions != null) {
            for (Interaction interaction : interactions) {
                if (interaction != null && interaction.getId() != null && interaction.getId().equals(interactionId)) {
                    return ProcessUtility.interaction(interaction, sectionMap, versions.getVersion1());
                }
            }
        }

        return null;
    }

    public Interaction deleteInteraction(ProcessDeployment deployment, String rawInteractionId) throws StatusCodeError {
        String interactionId = sanitizer.sanitize(rawInteractionId);

        ProcessDeployment.Builder updated = new ProcessDeployment.Builder(deployment, null, sanitizer, true);
        updated.clearInteractions();

        Interaction deleted = null;
        List<Interaction> interactions = deployment.getInteractions();
        Map<String, Section> sectionMap = deployment.getSectionMap();
        if (interactions != null) {
            for (Interaction interaction : interactions) {
                if (interaction != null && interaction.getId() != null && !interaction.getId().equals(interactionId)) {
                    updated.interaction(interaction);
                } else {
                    deleted = interaction;
                }
            }
        }

        deploymentRepository.save(updated.build());

        return deleted;
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

    public SearchResults search(MultivaluedMap<String, String> queryParameters) {
        List<Process> results;
        Set<Process> processes = helper.findProcesses(AuthorizationRole.OWNER, AuthorizationRole.CREATOR);

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

        // FIXME -- currently doesn't make any changes


        return cascadeSave(builder.build(), selectedDeploymentVersion.getVersion(), false);
    }

    public Process updateAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment, ProcessDeploymentResource resource) throws StatusCodeError {
        Process process = read(rawProcess.getProcessDefinitionKey());
                //update(rawProcess.getProcessDefinitionKey(), rawProcess);
        ProcessDeployment deployment = createDeployment(process.getProcessDefinitionKey(), rawDeployment);
        deploy(process.getProcessDefinitionKey(), deployment.getDeploymentId(), resource);
        publishDeployment(process.getProcessDefinitionKey(), deployment.getDeploymentId());
        process = read(process.getProcessDefinitionKey());
        return process;
    }

    public String getVersion() {
        return "v1";
    }

    private ProcessDeployment cascadeSave(ProcessDeployment deployment, String deploymentVersion, boolean createNew) {
        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(deployment, null, sanitizer, false)
                .deploymentVersion(deploymentVersion);

        builder.clearInteractions();
        builder.clearNotifications();
        builder.clearSections();

        if (createNew)
            builder.deploymentId(null);
        builder.published(false);

        Map<String, String> sectionIdMap = new HashMap<String, String>();
        if (deployment.getSections() != null && !deployment.getSections().isEmpty()) {
            for (Section section : deployment.getSections()) {
                Section.Builder sectionBuilder = new Section.Builder(section, sanitizer);
                if (createNew)
                    sectionBuilder.sectionId(null);
                Section persistedSection = sectionRepository.save(sectionBuilder.build());
                builder.section(persistedSection);
                if (StringUtils.isNotEmpty(section.getSectionId()))
                    sectionIdMap.put(section.getSectionId(), persistedSection.getSectionId());
            }
        }
        if (deployment.getInteractions() != null && !deployment.getInteractions().isEmpty()) {
            for (Interaction interaction : deployment.getInteractions()) {
                Interaction.Builder interactionBuilder = new Interaction.Builder(interaction, sanitizer);
                if (createNew)
                    interactionBuilder.id(null);

                Map<ActionType, Screen> screens = interaction.getScreens();
                if (screens != null && !screens.isEmpty()) {
                    for (Map.Entry<ActionType, Screen> entry : screens.entrySet()) {
                        Screen screen = entry.getValue();
                        Screen.Builder screenBuilder = new Screen.Builder(screen, sanitizer, false);
                        if (createNew)
                            screenBuilder.screenId(null);

                        List<Grouping> groupings = screen.getGroupings();
                        if (groupings != null) {
                            for (Grouping grouping : groupings) {
                                Grouping.Builder groupingBuilder = new Grouping.Builder(grouping, sanitizer, false);
                                List<String> sectionIds = grouping.getSectionIds();
                                if (sectionIds != null) {
                                    for (String sectionId : sectionIds) {
                                        String persistedSectionId = sectionIdMap.get(sectionId);
                                        if (StringUtils.isNotEmpty(persistedSectionId))
                                            groupingBuilder.sectionId(persistedSectionId);
                                    }
                                }
                                screenBuilder.grouping(groupingBuilder.build());
                            }
                        }

                        Screen persistedScreen = screenRepository.save(screenBuilder.build());
                        interactionBuilder.screen(entry.getKey(), persistedScreen);
                    }
                }
                Interaction persistedInteraction = interactionRepository.save(interactionBuilder.build());
                builder.interaction(persistedInteraction);
            }
        }
        if (deployment.getNotifications() != null && !deployment.getNotifications().isEmpty()) {
            for (Notification notification : deployment.getNotifications()) {
                Notification.Builder notificationBuilder = new Notification.Builder(notification, sanitizer);
                if (createNew)
                    notificationBuilder.notificationId(null);
                Notification persistedNotification = notificationRepository.save(notificationBuilder.build());
                builder.notification(persistedNotification);
            }
        }


        return deploymentRepository.save(builder.build());
    }

}
