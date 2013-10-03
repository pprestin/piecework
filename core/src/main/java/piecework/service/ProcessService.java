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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.*;
import piecework.identity.IdentityHelper;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class ProcessService {

    @Autowired
    DeploymentRepository deploymentRepository;

    @Autowired
    IdentityHelper helper;

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

//        builder.clearInteractions();
//
//        if (process.getInteractions() != null && !process.getInteractions().isEmpty()) {
//            for (Interaction interaction : process.getInteractions()) {
//                Interaction.Builder interactionBuilder = new Interaction.Builder(interaction, passthroughSanitizer);
//                interactionBuilder.screens(null);
//                if (interaction.getScreens() != null && !interaction.getScreens().isEmpty()) {
//                    for (Screen screen : interaction.getScreens()) {
//                        Screen persistedScreen = screenRepository.save(screen);
//                        interactionBuilder.screen(persistedScreen);
//                    }
//                }
//                Interaction persistedInteraction = interactionRepository.save(interactionBuilder.build());
//                builder.interaction(persistedInteraction);
//            }
//        }
//        if (rawProcess.getNotifications() != null && !rawProcess.getNotifications().isEmpty()) {
//            for (Notification notification : rawProcess.getNotifications()) {
//                Notification.Builder notificationBuilder = new Notification.Builder(notification, sanitizer);
//                Notification persistedNotification = notificationRepository.save(notificationBuilder.build());
//                builder.notification(persistedNotification);
//            }
//        }
//        if (rawProcess.getSections() != null && !rawProcess.getSections().isEmpty()) {
//            for (Section section : rawProcess.getSections()) {
//                Section.Builder sectionBuilder = new Section.Builder(section, sanitizer);
//                Section persistedSection = sectionRepository.save(sectionBuilder.build());
//                builder.section(persistedSection);
//            }
//        }

        return processRepository.save(builder.build());
    }

    public Process createAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment) throws StatusCodeError {
        Process process = create(rawProcess);
        ProcessDeployment deployment = createDeployment(process.getProcessDefinitionKey(), rawDeployment);
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
        ProcessDeployment deployment = cascadeSave(rawDeployment, deploymentVersion);

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
        ProcessDeployment deployment = cascadeSave(original, deploymentVersion);

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

    public ProcessDeployment getDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        List<ProcessDeploymentVersion> versions = process.getVersions();

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

        return new ProcessDeployment.Builder(original, process.getProcessDefinitionKey(), passthroughSanitizer, true)
                .build();
    }

    public ProcessDeployment publishDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        List<ProcessDeploymentVersion> deploymentVersions = process.getVersions();

        ProcessDeploymentVersion selectedDeploymentVersion = null;
        ProcessDeployment original = null;

        for (ProcessDeploymentVersion deploymentVersion : deploymentVersions) {
            if (deploymentVersion.getDeploymentId().equals(deploymentId)) {
                selectedDeploymentVersion = deploymentVersion;
                original = deploymentRepository.findOne(deploymentId);
                break;
            }
        }

        if (original == null)
            throw new NotFoundError();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        Process updated = new Process.Builder(process, passthroughSanitizer)
                .deploy(selectedDeploymentVersion, original)
                .build();

        processRepository.save(updated);

        return original;
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
            for (ProcessDeploymentVersion version : versions) {
                if (!version.isDeleted()) {
                    resultsBuilder.item(version);
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

        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        List<ProcessDeploymentVersion> versions = process.getVersions();
        ProcessDeploymentVersion current = null;
        if (versions != null) {
            for (ProcessDeploymentVersion version : versions) {

            }
        }

        ProcessDeployment deployment = new ProcessDeployment.Builder(rawDeployment, process.getProcessDefinitionKey(), sanitizer, true).build();
        if (!deployment.getDeploymentId().equals(deploymentId))
            throw new BadRequestError();

        deployment = deploymentRepository.save(deployment);

        return deployment;
    }

    public Process updateAndPublishDeployment(Process rawProcess, ProcessDeployment rawDeployment) throws StatusCodeError {
        Process process = read(rawProcess.getProcessDefinitionKey());
                //update(rawProcess.getProcessDefinitionKey(), rawProcess);
        ProcessDeployment deployment = createDeployment(process.getProcessDefinitionKey(), rawDeployment);
        publishDeployment(process.getProcessDefinitionKey(), deployment.getDeploymentId());
        process = read(process.getProcessDefinitionKey());
        return process;
    }

    public String getVersion() {
        return "v1";
    }

    private ProcessDeployment cascadeSave(ProcessDeployment deployment, String deploymentVersion) {
        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(deployment, null, sanitizer, false)
                .deploymentVersion(deploymentVersion);

        if (deployment.getInteractions() != null && !deployment.getInteractions().isEmpty()) {
            for (Interaction interaction : deployment.getInteractions()) {
                Interaction.Builder interactionBuilder = new Interaction.Builder(interaction, sanitizer);
                interactionBuilder.screens(null);
                if (interaction.getScreens() != null && !interaction.getScreens().isEmpty()) {
                    for (Screen screen : interaction.getScreens()) {
                        Screen persistedScreen = screenRepository.save(screen);
                        interactionBuilder.screen(persistedScreen);
                    }
                }
                Interaction persistedInteraction = interactionRepository.save(interactionBuilder.build());
                builder.interaction(persistedInteraction);
            }
        }
        if (deployment.getNotifications() != null && !deployment.getNotifications().isEmpty()) {
            for (Notification notification : deployment.getNotifications()) {
                Notification.Builder notificationBuilder = new Notification.Builder(notification, sanitizer);
                Notification persistedNotification = notificationRepository.save(notificationBuilder.build());
                builder.notification(persistedNotification);
            }
        }
        if (deployment.getSections() != null && !deployment.getSections().isEmpty()) {
            for (Section section : deployment.getSections()) {
                Section.Builder sectionBuilder = new Section.Builder(section, sanitizer);
                Section persistedSection = sectionRepository.save(sectionBuilder.build());
                builder.section(persistedSection);
            }
        }
        return deploymentRepository.save(builder.build());
    }

}
