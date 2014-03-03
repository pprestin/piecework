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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.AttachmentRepository;
import piecework.repository.ContentRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.util.ProcessUtility;

import java.util.Set;

/**
 * @author James Renfro
 */
public class ProcessInstanceRepositoryProvider extends ProcessDeploymentRepositoryProvider implements ProcessInstanceProvider {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceRepositoryProvider.class);

    protected final ProcessInstanceRepository processInstanceRepository;
    protected final ProcessEngineFacade facade;
    protected final AttachmentRepository attachmentRepository;
    protected final ContentRepository contentRepository;
    protected final DeploymentRepository deploymentRepository;
    private final String processInstanceId;

    private ProcessInstance _instance;
    private ProcessDeployment _deployment;


    ProcessInstanceRepositoryProvider(ProcessProvider processProvider, ProcessInstanceRepository processInstanceRepository,
                                      ProcessEngineFacade facade, AttachmentRepository attachmentRepository, ContentRepository contentRepository, DeploymentRepository deploymentRepository,
                                      String processInstanceId) {
        super(processProvider);
        this.processInstanceRepository = processInstanceRepository;
        this.facade = facade;
        this.attachmentRepository = attachmentRepository;
        this.contentRepository = contentRepository;
        this.deploymentRepository = deploymentRepository;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public synchronized ProcessDeployment deployment() throws PieceworkException {
        if (_deployment != null)
            return _deployment;

        Process process = process();
        ProcessInstance instance = instance();

        // Make sure we have a process
        if (process == null)
            throw new MisconfiguredProcessException("No process available, cannot look for deployment");

        // Always use the current deployment when starting a new execution, but for executions
        // that are already running (that have an instance) it may be necessary to retrieve them
        // on an ad-hoc basis
        _deployment = process.getDeployment();

        // Various other sanity checks
        if (_deployment == null || StringUtils.isEmpty(_deployment.getDeploymentId()))
            throw new MisconfiguredProcessException("No deployment or deployment id is empty", process.getProcessDefinitionKey());

        // It's okay not to have an instance here, but if we have one, it needs to have a deployment id
        if (instance != null && StringUtils.isEmpty(instance.getDeploymentId()))
            throw new MisconfiguredProcessException("Instance deployment id is empty", process.getProcessDefinitionKey());

        if (!useCurrentDeployment(instance, _deployment)) {
            String deploymentId = instance.getDeploymentId();
            return deploymentRepository.findOne(deploymentId);
        }
        return _deployment;
    }

    @Override
    public ContentResource diagram() throws PieceworkException {
        Process process = process();
        ProcessInstance instance = instance();

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, instance.getDeploymentId());
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment deployment = deployment();

        try {
            return facade.resource(process, deployment, "image/png");
        } catch (ProcessEngineException e) {
            LOG.error("Could not generate diagram", e);
            throw new InternalServerError(e);
        }
    }

    @Override
    public ProcessInstance instance() throws PieceworkException {
        return instance(null);
    }

    @Override
    public synchronized ProcessInstance instance(ViewContext context) throws PieceworkException {
        long start = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
            LOG.debug("Retrieving instance for " + processInstanceId);
        }

        if (_instance == null) {
            Process process = process();
            _instance = processInstanceRepository.findOne(processInstanceId);

            if (_instance == null || StringUtils.isEmpty(_instance.getProcessDefinitionKey()))
                throw new NotFoundError(Constants.ExceptionCodes.instance_does_not_exist);

            if (!_instance.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
                throw new NotFoundError(Constants.ExceptionCodes.instance_does_not_exist);

            if (_instance.isDeleted())
                throw new GoneError(Constants.ExceptionCodes.instance_does_not_exist);
        }

        if (context != null) {
            ProcessInstance.Builder builder = new ProcessInstance.Builder(_instance);

            Process process = process();
            builder.processDefinitionKey(process.getProcessDefinitionKey())
                   .processDefinitionLabel(process.getProcessDefinitionLabel());

            Set<String> attachmentIds = _instance.getAttachmentIds();
            if (attachmentIds != null && !attachmentIds.isEmpty()) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Retrieving all attachments for instance " + processInstanceId);
                Iterable<Attachment> attachments = attachmentRepository.findAll(attachmentIds);

                if (attachments != null) {
                    for (Attachment attachment : attachments) {
                        builder.attachment(new Attachment.Builder(attachment).build(context));
                    }
                }
            }

            if (LOG.isDebugEnabled())
                LOG.debug("Retrieved instance and attachments for " + processInstanceId + " in " + (System.currentTimeMillis() - start) + " ms");

            return builder.build(context);
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved instance for " + processInstanceId + " in " + (System.currentTimeMillis() - start) + " ms");

        return _instance;
    }

    protected synchronized ProcessInstance getInstance() {
        return this._instance;
    }

    protected synchronized void setInstance(ProcessInstance instance) {
        this._instance = instance;
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
