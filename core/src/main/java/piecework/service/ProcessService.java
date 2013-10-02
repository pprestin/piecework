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
import org.springframework.core.env.Environment;
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
        piecework.model.Process.Builder builder = new Process.Builder(rawProcess, sanitizer, true);

        Process process = builder.build();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        builder = new Process.Builder(process, passthroughSanitizer, false);

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

    public ProcessDeployment createDeployment(String rawProcessDefinitionKey) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);

        ProcessDeployment deployment = new ProcessDeployment.Builder().build();
        deployment = deploymentRepository.save(deployment);

        List<ProcessDeploymentVersion> versions = process.getVersions();

        String version = versions != null ? "" + (versions.size() + 1) : "1";

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        Process updated = new Process.Builder(process, passthroughSanitizer, true)
                .version(new ProcessDeploymentVersion("", deployment.getDeploymentId(), version))
                .build();

        processRepository.save(updated);

        return deployment;
    }

    public ProcessDeployment cloneDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        List<ProcessDeploymentVersion> deploymentVersions = process.getVersions();

        String version = deploymentVersions != null ? "" + (deploymentVersions.size() + 1) : "1";

        ProcessDeployment original = null;

        for (ProcessDeploymentVersion deploymentVersion : deploymentVersions) {
            if (deploymentVersion.getDeploymentId().equals(deploymentId)) {
                original = deploymentRepository.findOne(deploymentId);
                break;
            }
        }

        if (original == null)
            throw new NotFoundError();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        ProcessDeployment deployment = new ProcessDeployment.Builder(original, null, passthroughSanitizer, true).deploymentId(null).build();
        deployment = deploymentRepository.save(deployment);

        Process updated = new Process.Builder(process, passthroughSanitizer, true)
                .version(new ProcessDeploymentVersion("", deployment.getDeploymentId(), version))
                .build();

        processRepository.save(updated);

        return deployment;
    }

    public Process delete(String rawProcessDefinitionKey) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

        Process record = processRepository.findOne(processDefinitionKey);
        if (record == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);

        Process.Builder builder = new Process.Builder(record, sanitizer, true);
        builder.delete();
        return processRepository.save(builder.build());
    }

    public void deleteDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        Process updated = new Process.Builder(process, sanitizer, true)
                .deleteDeployment(deploymentId)
                .build();

        processRepository.save(updated);
    }

    public ProcessDeployment publishDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        List<ProcessDeploymentVersion> deploymentVersions = process.getVersions();

        String version = deploymentVersions != null ? "" + (deploymentVersions.size() + 1) : "1";

        ProcessDeployment original = null;

        for (ProcessDeploymentVersion deploymentVersion : deploymentVersions) {
            if (deploymentVersion.getDeploymentId().equals(deploymentId)) {
                original = deploymentRepository.findOne(deploymentId);
                break;
            }
        }

        if (original == null)
            throw new NotFoundError();

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        Process updated = new Process.Builder(process, passthroughSanitizer, true)
                .deploy(original)
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
                results.add(new Process.Builder(process, sanitizer, false).build(versions.getVersion1()));
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
        String includedKey = sanitizer.sanitize(rawProcess.getProcessDefinitionKey());
        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

        // If the path param key is not the same as the one that's included in the process, then this put is a rename
        // of the key -- this means we delete the old one and createDeployment a new one, assuming that the new one doesn't conflict
        // with an existing key
        if (!processDefinitionKey.equals(includedKey)) {

            // Check for a process with the new key
            Process record = processRepository.findOne(includedKey);

            // This means that a process with that key already exists
            if (record != null && !record.isDeleted())
                throw new ForbiddenError(Constants.ExceptionCodes.process_change_key_duplicate, processDefinitionKey, includedKey);

            record = processRepository.findOne(processDefinitionKey);
            if (record != null) {
                Process.Builder builder = new Process.Builder(record, passthroughSanitizer, true);
                processRepository.delete(builder.build());
            }
        }

        Process.Builder builder = new Process.Builder(rawProcess, sanitizer, false);
//        builder.clearInteractions();
//        if (rawProcess.getInteractions() != null && !rawProcess.getInteractions().isEmpty()) {
//            for (Interaction interaction : rawProcess.getInteractions()) {
//                Interaction.Builder interactionBuilder = new Interaction.Builder(interaction, sanitizer);
//                interactionBuilder.clearScreens();
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


    public ProcessDeployment updateDeployment(String rawProcessDefinitionKey, String rawDeploymentId, ProcessDeployment rawDeployment) throws StatusCodeError {
        Process process = read(rawProcessDefinitionKey);
        String deploymentId = sanitizer.sanitize(rawDeploymentId);

        ProcessDeployment deployment = new ProcessDeployment.Builder(rawDeployment, process.getProcessDefinitionKey(), sanitizer, true).build();
        if (!deployment.getDeploymentId().equals(deploymentId))
            throw new BadRequestError();

        deployment = deploymentRepository.save(deployment);

        return deployment;
    }

    public String getVersion() {
        return "v1";
    }

}
