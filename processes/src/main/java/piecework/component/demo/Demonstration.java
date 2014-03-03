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
package piecework.component.demo;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.SystemUser;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessInstanceProvider;
import piecework.service.ProcessService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service("Demonstration")
public class Demonstration implements TaskListener {

    private static final Logger LOG = Logger.getLogger(Demonstration.class);

    private static String NAMESPACE = "activiti";
    private static String PROCESS_DEFINITION_KEY = "Demonstration";

    @Autowired
    Environment environment;

    @Autowired
    ProcessService processService;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    ProcessEngineFacade facade;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processBusinessKey = delegateTask.getExecution().getProcessBusinessKey();
        String processInstanceId = delegateTask.getProcessInstanceId();
        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();

        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(PROCESS_DEFINITION_KEY, processBusinessKey, new SystemUser());

        ProcessInstance processInstance;
        try {
            processInstance = instanceProvider.instance();
        } catch (PieceworkException e) {
            LOG.error("Could not retrieve process instance ", e);
            throw new RuntimeException(e);
        }

        Map<String, List<Value>> values = processInstance != null ? processInstance.getData() : null;

        if (values == null) {
            LOG.error("Process instance could not be found, or form messages is null " + NAMESPACE + ", " + PROCESS_DEFINITION_KEY + ", " + processInstanceId);
            return;
        }

        if (taskDefinitionKey == null) {
            LOG.error("Task definition key is null " + NAMESPACE + ", " + PROCESS_DEFINITION_KEY + ", " + processInstanceId);
            return;
        }

        List<Value> submitters = values.get("Submitter");
        User submitter = submitters != null ? (User) submitters.iterator().next() : null;

        delegateTask.setAssignee(submitter.getUserId());
    }

    @PostConstruct
    public void configure() throws IOException, ProcessEngineException, PieceworkException {
        boolean isDemoMode = environment.getProperty("demo.mode", Boolean.class, Boolean.FALSE);

        if (!isDemoMode)
            return;

        Process process = demoProcess();
        ProcessDeployment deployment = demoProcessDeployment();

        ClassPathResource classPathResource = new ClassPathResource("META-INF/demo/Demonstration.bpmn20.xml");
        ContentResource resource = new BasicContentResource.Builder()
                .contentType("application/xml")
                .filename("Demonstration.bpmn20.xml")
                .inputStream(classPathResource.getInputStream())
                .build();

        try {
//            ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(process.getProcessDefinitionKey(), new SystemUser());
            processService.update(process.getProcessDefinitionKey(), process, new SystemUser());
            processService.updateAndPublishDeployment(process, deployment, resource, false, new SystemUser());
        } catch (MappingException mappingException) {
            LOG.fatal("Could not create Demonstration process because of a spring mapping exception", mappingException);
        } catch (Exception e) {
            processService.create(process);
            processService.createAndPublishDeployment(process, deployment, resource, false, new SystemUser());
        }
    }

    public void synchronize() throws IOException, ProcessEngineException, PieceworkException {
        Process process = demoProcess();
        ProcessDeployment deployment = demoProcessDeployment();

        ClassPathResource classPathResource = new ClassPathResource("META-INF/demo/Demonstration.bpmn20.xml");
        ContentResource resource = new BasicContentResource.Builder()
                .contentType("application/xml")
                .filename("Demonstration.bpmn20.xml")
                .inputStream(classPathResource.getInputStream())
                .build();

        processService.updateAndPublishDeployment(process, deployment, resource, false, new SystemUser());
    }

    public static Field approvedField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.HTML)
                .defaultValue("<h4>Form has been approved. </h4>")
                .name("ApprovedText")
                .build();
    }

    public static Field rejectedField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.HTML)
                .defaultValue("<h4>Form has been returned to reviewer. </h4>")
                .name("RejectedText")
                .build();
    }


    public ProcessDeployment demoProcessDeployment() {
        ProcessDeployment current = new ProcessDeployment.Builder()
                .processInstanceLabelTemplate("{{Submitter.DisplayName}} likes {{FavoriteColor}}")
                .engine("activiti")
                .engineProcessDefinitionKey("DemonstrationProcess")
                .engineProcessDefinitionLocation("classpath:META-INF/demo/Demonstration.bpmn20.xml")
                .base("classpath:META-INF/demo")
                .build();
        return current;
    }

    public static Process demoProcess() {
        return new Process.Builder()
                .processDefinitionKey("Demonstration")
                .processDefinitionLabel("Demonstration Process")
                .allowAnonymousSubmission(true)
                .build();
    }

}
