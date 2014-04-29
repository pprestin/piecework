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
package piecework.engine.activiti;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.engine.*;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.query.Query;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.*;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.engine.exception.TaskAlreadyClaimedException;
import piecework.enumeration.ActionType;
import piecework.enumeration.FlowElementType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.engine.*;
import piecework.engine.exception.ProcessEngineException;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.persistence.TaskProvider;
import piecework.common.SearchCriteria;
import piecework.repository.ProcessInstanceRepository;
import piecework.identity.IdentityHelper;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;
import piecework.common.ManyMap;
import piecework.validation.Validation;

/**
 * @author James Renfro
 */
@Service
public class ActivitiEngineProxy implements ProcessEngineProxy {

    private static final Logger LOG = Logger.getLogger(ActivitiEngineProxy.class);

    @Autowired
    IdentityHelper helper;

    @Autowired
    ActivitiEngineProxyHelper proxyHelper;

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    piecework.service.IdentityService userDetailsService;

	@Override
	public String start(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        Entity principal = helper.getPrincipal();
        String userId = principal != null ? principal.getEntityId() : null;
        processEngine.getIdentityService().setAuthenticatedUserId(userId);

        ProcessDeployment detail = process.getDeployment();

        if (detail == null)
            throw new ProcessEngineException("No process has been published for " + process.getProcessDefinitionKey());

        // These variables are to tie this instance to the instance that piecework stored in Mongo
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("PIECEWORK_PROCESS_DEFINITION_KEY", process.getProcessDefinitionKey());
        variables.put("PIECEWORK_PROCESS_INSTANCE_ID", instance.getProcessInstanceId());
        variables.put("PIECEWORK_PROCESS_INSTANCE_LABEL", instance.getProcessInstanceLabel());
        variables(variables, instance.getData());

		String engineProcessDefinitionKey = detail.getEngineProcessDefinitionKey();
        String engineProcessDefinitionId = detail.getEngineProcessDefinitionId();
		org.activiti.engine.runtime.ProcessInstance activitiInstance =
                processEngine.getRuntimeService().startProcessInstanceById(engineProcessDefinitionId, instance.getProcessInstanceId(), variables);
		return activitiInstance.getId();
	}

    @Override
    public boolean activate(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        Entity principal = helper.getPrincipal();
        String userId = principal != null ? principal.getEntityId() : null;
        processEngine.getIdentityService().setAuthenticatedUserId(userId);

        if (deployment == null)
            throw new ProcessEngineException("No process has been published for " + process.getProcessDefinitionKey());

        String engineProcessDefinitionKey = deployment.getEngineProcessDefinitionKey();
        org.activiti.engine.runtime.ProcessInstance activitiInstance = findActivitiInstance(instance.getEngineProcessInstanceId(), null);

        if (activitiInstance != null) {
            processEngine.getRuntimeService().activateProcessInstanceById(activitiInstance.getProcessInstanceId());
            return true;
        }

        return false;
    }

    @Override
    public boolean assign(Process process, ProcessDeployment deployment, String taskId, User user) throws ProcessEngineException {
        if (user != null && user.getUserId() != null) {
            processEngine.getTaskService().setAssignee(taskId, user.getUserId());
        } else {
            processEngine.getTaskService().setAssignee(taskId, null);
        }
        return true;
    }

    @Override
	public boolean cancel(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        Entity principal = helper.getPrincipal();
        String userId = principal != null ? principal.getEntityId() : null;
        processEngine.getIdentityService().setAuthenticatedUserId(userId);
        org.activiti.engine.runtime.ProcessInstance activitiInstance = findActivitiInstance(instance.getEngineProcessInstanceId(), null);
		
		if (activitiInstance != null) {
            processEngine.getRuntimeService().deleteProcessInstance(activitiInstance.getProcessInstanceId(), Constants.DeleteReasons.CANCELLED);
			return true;
		}
		
		return false;
	}

    @Override
    public boolean suspend(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        Entity principal = helper.getPrincipal();
        String userId = principal != null ? principal.getEntityId() : null;
        processEngine.getIdentityService().setAuthenticatedUserId(userId);

        if (deployment == null)
            throw new ProcessEngineException("Deployment missing for " + process.getProcessDefinitionKey());

        String engineProcessDefinitionKey = deployment.getEngineProcessDefinitionKey();
        org.activiti.engine.runtime.ProcessInstance activitiInstance = findActivitiInstance(instance.getEngineProcessInstanceId(), null);

        if (activitiInstance != null) {
            processEngine.getRuntimeService().suspendProcessInstanceById(activitiInstance.getProcessInstanceId());
            return true;
        }

        return false;
    }

    @Override
    public boolean completeTask(Process process, ProcessDeployment deployment, String taskId, ActionType action, Validation validation, Entity principal) throws ProcessEngineException {
        //String userId = principal != null ? principal.getEntityId() : null;
        String userId = validation.getSubmission() != null ? validation.getSubmission().getSubmitterId() : null; //use the act as
        processEngine.getIdentityService().setAuthenticatedUserId(userId);

        if (deployment == null)
            throw new ProcessEngineException("Deployment missing for " + process.getProcessDefinitionKey());

        String engineProcessDefinitionKey = deployment.getEngineProcessDefinitionKey();

        try {
            org.activiti.engine.task.Task activitiTask = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();

            if (activitiTask != null)  {
                Map<String, Object> variables = new HashMap<String, Object>();

                String taskDefinition = activitiTask.getTaskDefinitionKey();
                String executionId = activitiTask.getExecutionId();

                //if subtask use parent variables
                if(activitiTask.getParentTaskId() != null){
                    org.activiti.engine.task.Task parentTask = processEngine.getTaskService().createTaskQuery().taskId(activitiTask.getParentTaskId()).singleResult();
                    taskDefinition = parentTask.getTaskDefinitionKey();
                    executionId = parentTask.getExecutionId();
                }

                if (action != null) {
                    String variableName = taskDefinition + "_action";
                    variables.put(variableName, action.name());
                    processEngine.getTaskService().setVariableLocal(taskId, variableName, action.name());
                }

                // Since we sometimes need to use user-submitted data to route the process instance, include
                // just the first value here -- later, it may be necessary to include more complicated logic to
                // determine which values should be passed to Activiti
                Map<String, List<Value>> data = validation.getData();
                variables(variables, data);
                processEngine.getRuntimeService().setVariables(executionId, variables);

                // Always assign the task to the user before completing it
                String currentAssignee = activitiTask.getAssignee();
                if ( StringUtils.isNotEmpty(userId) && ( currentAssignee == null || ! userId.equals(currentAssignee) ) )
                    processEngine.getTaskService().setAssignee(taskId, userId);

                processEngine.getTaskService().complete(taskId);
                return true;
            }
        } catch (ActivitiTaskAlreadyClaimedException e) {
            throw new TaskAlreadyClaimedException();
        } catch (ActivitiException exception) {
            throw new ProcessEngineException("Activiti unable to complete task ", exception);
        }

        return false;
    }

    @Override
    public Task createSubTask(TaskProvider taskProvider, Validation validation) throws PieceworkException {
        //Entity principal = helper.getPrincipal();
        String userId = validation.getSubmission().getSubmitterId() != null ? validation.getSubmission().getSubmitterId() : null;
        processEngine.getIdentityService().setAuthenticatedUserId(userId);

        Process process = taskProvider.process();
        ProcessDeployment deployment = taskProvider.deployment();
        if (deployment == null)
            throw new ProcessEngineException("Deployment missing for " + process.getProcessDefinitionKey());

        Task task = taskProvider.task();
        String engineProcessDefinitionKey = deployment.getEngineProcessDefinitionKey();

        try {
            org.activiti.engine.task.Task activitiTask = processEngine.getTaskService().createTaskQuery().taskId(task.getTaskInstanceId()).singleResult();

            org.activiti.engine.runtime.ProcessInstance p = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(activitiTask.getProcessInstanceId()).singleResult();

            //Need to check what this process instance is....
            String processInstanceId = (String)processEngine.getRuntimeService().getVariable(activitiTask.getProcessInstanceId(), "PIECEWORK_PROCESS_INSTANCE_ID");

            if (activitiTask != null)  {
                Map<String, Object> variables = new HashMap<String, Object>();
                Map<String, List<Value>> data = validation.getData();
                variables(variables, data);

                //String assignedUserId = data.get("adHocPerson").get(0).toString();
                String assignedUserId = validation.getSubmission().getAssignee();
                User user =  userDetailsService.getUser(assignedUserId);

                org.activiti.engine.task.Task subTask =  processEngine.getTaskService().newTask();
                subTask.setParentTaskId(task.getTaskInstanceId());

                processEngine.getRuntimeService().setVariables(activitiTask.getExecutionId(), variables);
                processEngine.getTaskService().saveTask(subTask);

                //set the assignee
                processEngine.getTaskService().setAssignee(subTask.getId(), assignedUserId);

                Task subtask = convert(subTask, process, true);
                Task.Builder b = new Task.Builder(subtask, new PassthroughSanitizer());

                //backfill the process instance into the piecework version
                b.engineProcessInstanceId(activitiTask.getProcessInstanceId());
                b.processInstanceId(processInstanceId);


                b.candidateAssignee(user);
                b.assignee(user);

                //set the creator
                b.initiator(validation.getSubmission().getSubmitter());

                //use daddy's task definition and append _SubTask
                b.taskDefinitionKey(activitiTask.getTaskDefinitionKey() + "_SubTask");
                return b.build();
            }
        } catch (ActivitiException exception) {
            throw new ProcessEngineException("Activiti unable to create sub task ", exception);
        }

        return null;
    }



    @Override
    public ProcessDeployment deploy(Process process, ProcessDeployment deployment, ContentResource contentResource) throws ProcessEngineException {
        Entity principal = helper.getPrincipal();
        String userId = principal != null ? principal.getEntityId() : null;
        processEngine.getIdentityService().setAuthenticatedUserId(userId);

        if (!deployment.getEngine().equals(getKey()))
            return null;

        InputStream inputStream;

        try {
            inputStream = contentResource.getInputStream();
        } catch (IOException ioe) {
            throw new ProcessEngineException("Unable to read input stream to deploy", ioe);
        }
        Deployment activitiDeployment = processEngine.getRepositoryService().createDeployment()
                .addInputStream(contentResource.getFilename(), inputStream)
                .deploy();

        LOG.debug("Deployed new process definition to activiti: " + activitiDeployment.getId() + " : " + activitiDeployment.getName());

        List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(activitiDeployment.getId()).list();
        ProcessDefinition deployedProcessDefinition = processDefinitions != null && !processDefinitions.isEmpty() ? processDefinitions.iterator().next() : null;

        ProcessDeployment.Builder updated = new ProcessDeployment.Builder(deployment, new PassthroughSanitizer(), true);

        if (deployedProcessDefinition != null && StringUtils.isNotEmpty(activitiDeployment.getId())) {

            Map<String, Activity> activityMap = deployment.getActivityMap();

            BpmnModel bpmnModel = processEngine.getRepositoryService().getBpmnModel(deployedProcessDefinition.getId());

            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();

            if (flowElements != null) {
                for (FlowElement flowElement : flowElements) {
                    String flowElementId = flowElement.getId();
                    FlowElementType elementType = null;
                    if (flowElement instanceof org.activiti.bpmn.model.StartEvent) {
                        elementType = FlowElementType.START_EVENT;
                        updated.startActivityKey(flowElementId);
                    } else if (flowElement instanceof org.activiti.bpmn.model.ServiceTask)
                        elementType = FlowElementType.SERVICE_TASK;
                    else if (flowElement instanceof org.activiti.bpmn.model.UserTask)
                        elementType = FlowElementType.USER_TASK;
                    else if (flowElement instanceof  ManualTask)
                        elementType = FlowElementType.MANUAL_TASK;

                    if (elementType != null) {
                        updated.flowElement(flowElementId, flowElement.getName(), elementType);

                        Activity.Builder activityBuilder = new Activity.Builder().elementType(elementType);
                        if (!activityMap.containsKey(flowElementId)) {
                            updated.activity(flowElementId, activityBuilder.build());
                        }
                    }
                }
            }

            updated.engineProcessDefinitionId(deployedProcessDefinition != null ? deployedProcessDefinition.getId() : null)
                .engineDeploymentId(activitiDeployment.getId())
                .engineProcessDefinitionLocation(contentResource.getLocation())
                .engineProcessDefinitionResource(contentResource.getFilename())
                .deploy();
        }

        return updated.build();
    }

    @Override
	public ProcessExecution findExecution(SearchCriteria criteria) throws ProcessEngineException {

        if (! criteria.getEngines().contains(getKey()))
            return null;

        boolean includeVariables = false;

        HistoricProcessInstance instance = instanceQuery(criteria).singleResult();

        if (instance != null) {
            ProcessExecution.Builder executionBuilder = new ProcessExecution.Builder()
                .executionId(instance.getId())
                .businessKey(instance.getBusinessKey())
                .initiatorId(instance.getStartUserId())
                .deleteReason(instance.getDeleteReason());

            if (criteria.isIncludeVariables()) {
                Map<String, Object> variables = processEngine.getRuntimeService().getVariables(instance.getId());
                executionBuilder.data(variables);
            }

            return executionBuilder.build();
        }

        return null;
	}

	@Override
	public ProcessExecutionResults findExecutions(SearchCriteria criteria) throws ProcessEngineException {
        if (! criteria.getEngines().contains(getKey()))
            return null;

        HistoricProcessInstanceQuery query = instanceQuery(criteria);

        ProcessExecutionResults.Builder resultsBuilder = new ProcessExecutionResults.Builder();

        List<HistoricProcessInstance> instances;

        // Can't use paging since we're going to filter after the fact
        instances = query.list();
        int size = instances.size();

        resultsBuilder.firstResult(0);
        resultsBuilder.maxResults(size);
        resultsBuilder.total(size);

        // Only need to worry about filtering if there are more than 1 key, since with 1 key
        // it's part of the search that Activiti performs --- see the instanceQuery() method
        SortedSet<String> engineProcessDefinitionKeys = new TreeSet<String>(criteria.getEngineProcessDefinitionKeys());
        Set<String> processDefinitionIds = proxyHelper.getProcessDefinitionIds(engineProcessDefinitionKeys.toArray(new String[engineProcessDefinitionKeys.size()]));

        List<ProcessExecution> executions;
        if (instances != null && !instances.isEmpty()) {
            executions = new ArrayList<ProcessExecution>(instances.size());

            for (HistoricProcessInstance instance : instances) {
                if (!processDefinitionIds.contains(instance.getProcessDefinitionId()))
                    continue;

                ProcessExecution.Builder executionBuilder = new ProcessExecution.Builder()
                        .executionId(instance.getId())
                        .businessKey(instance.getBusinessKey())
                        .initiatorId(instance.getStartUserId())
                        .deleteReason(instance.getDeleteReason());

                if (criteria.isIncludeVariables()) {
                    Map<String, Object> variables = processEngine.getRuntimeService().getVariables(instance.getId());
                    executionBuilder.data(variables);
                }

                executions.add(executionBuilder.build());
            }
        } else {
            executions = Collections.emptyList();
        }

        resultsBuilder.executions(executions);

        return resultsBuilder.build();
	}

    @Override
    public Task findTask(Process process, ProcessDeployment deployment, String taskId, boolean limitToActive) throws ProcessEngineException {
        org.activiti.engine.task.Task activitiTask = processEngine.getTaskService()
                .createTaskQuery()
//                .processDefinitionKey(deployment.getEngineProcessDefinitionKey())
                .taskId(taskId)
                .singleResult();
        if (activitiTask != null)
            return convert(activitiTask, process, true);

        if (!limitToActive) {
            HistoricTaskInstance historicTask = processEngine.getHistoryService()
                    .createHistoricTaskInstanceQuery()
//                    .processDefinitionKey(deployment.getEngineProcessDefinitionKey())
                    .taskId(taskId)
                    .singleResult();
            if (historicTask != null)
                return convert(historicTask, process, true);
        }

        return null;
    }

//    @Override
//    public Task findTask(TaskCriteria ... criterias) throws ProcessEngineException {
//        TaskCriteria criteria = criterias[0];
//
//        if (criteria.getProcesses() == null)
//            return null;
//
//        for (Process process : criteria.getProcesses()) {
//            org.activiti.engine.task.Task activitiTask = taskQuery(criteria).singleResult();
//            HistoricTaskInstance historicTask = null;
//
//            Task task;
//
//            String engineProcessInstanceId;
//            if (activitiTask != null) {
//                engineProcessInstanceId = activitiTask.getProcessInstanceId();
//                task = convert(activitiTask, process, true);
//            } else {
//                historicTask = historicTaskQuery(criteria).singleResult();
//                if (historicTask == null)
//                    continue;
//
//                engineProcessInstanceId = historicTask.getProcessInstanceId();
//                task = convert(historicTask, process, true);
//            }
//
//            return task;
//        }
//
//        return null;
//    }

    public TaskResults findTasks(TaskCriteria ... criterias) throws ProcessEngineException {
        TaskCriteria criteria = criterias[0];

        TaskResults.Builder resultsBuilder = new TaskResults.Builder();

        if (criteria.getProcesses() == null || criteria.getProcesses().isEmpty())
            return resultsBuilder.build();

        Query query;

        if (StringUtils.isEmpty(criteria.getProcessStatus()) || criteria.getProcessStatus().equals(Constants.ProcessStatuses.OPEN) ||
                criteria.getProcessStatus().equals(Constants.ProcessStatuses.SUSPENDED)) {
            query = taskQuery(criteria);
        } else {
            query = historicTaskQuery(criteria);
        }

        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        List<?> activitiTasks = query.list();
        int size = activitiTasks.size();

        if (activitiTasks != null) {
            ManyMap<String, Process> processMap = proxyHelper.getProcessDefinitionIdMap(criteria.getProcesses());
            for (Object instance : activitiTasks) {
                String engineProcessInstanceId;
                Task task;
                if (instance instanceof org.activiti.engine.task.Task) {
                    org.activiti.engine.task.Task activitiTask = org.activiti.engine.task.Task.class.cast(instance);
                    engineProcessInstanceId = activitiTask.getProcessInstanceId();
                    List<Process> processes = processMap.get(activitiTask.getProcessDefinitionId());
                    if (processes != null) {
                        for (Process process : processes) {
                            resultsBuilder.task(convert(activitiTask, process, true));
                        }
                    }
                } else {
                    HistoricTaskInstance historicTask = HistoricTaskInstance.class.cast(instance);
                    if (historicTask == null)
                        continue;

                    engineProcessInstanceId = historicTask.getProcessInstanceId();
                    List<Process> processes = processMap.get(historicTask.getProcessDefinitionId());
                    if (processes != null) {
                        for (Process process : processes) {
                            resultsBuilder.task(convert(historicTask, process, true));
                        }
                    }
                }
                resultsBuilder.engineProcessInstanceId(engineProcessInstanceId);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching for tasks took " + (System.currentTimeMillis() - time) + " ms");
        }

        resultsBuilder.firstResult(0);
        resultsBuilder.maxResults(size);
        resultsBuilder.total(size);

        return resultsBuilder.build();
    }

    @Override
    public ContentResource resource(Process process, ProcessDeployment deployment, String contentType) throws ProcessEngineException {
        InputStream inputStream = processEngine.getRepositoryService().getProcessDiagram(deployment.getEngineProcessDefinitionId());
        return new BasicContentResource.Builder().inputStream(inputStream).contentType("image/png").build();
    }

    @Override
    public ContentResource resource(Process process, ProcessDeployment deployment, ProcessInstance instance, String contentType) throws ProcessEngineException {
        BpmnModel bpmnModel = processEngine.getRepositoryService().getBpmnModel(deployment.getEngineProcessDefinitionId());
        InputStream inputStream = ProcessDiagramGenerator.generateDiagram(bpmnModel, "png", processEngine.getRuntimeService().getActiveActivityIds(instance.getEngineProcessInstanceId()));
        return new BasicContentResource.Builder().inputStream(inputStream).contentType("image/png").build();
    }

    private Task convert(org.activiti.engine.task.Task instance, Process process, boolean includeDetails) {
        Task.Builder taskBuilder = new Task.Builder()
                .taskInstanceId(instance.getId())
                .taskDefinitionKey(instance.getTaskDefinitionKey())
                .taskLabel(instance.getName())
                .taskDescription(instance.getDescription())
                .taskStatus(instance.isSuspended() ? Constants.TaskStatuses.SUSPENDED : Constants.TaskStatuses.OPEN) //default case
                .engineProcessInstanceId(instance.getProcessInstanceId())
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .priority(instance.getPriority())
                .startTime(instance.getCreateTime())
                .dueDate(instance.getDueDate());

        if(instance instanceof DelegateTask)
        {
            String eventName = ((DelegateTask)instance).getEventName();

            if(eventName != null && eventName.equals(Constants.ActionTypes.COMPLETE)) {
                    taskBuilder.taskStatus(Constants.TaskStatuses.COMPLETE);
            }
        }

        //!suspended and !completed
        if (!instance.isSuspended())
            taskBuilder.active();

        if (includeDetails) {
            List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(instance.getId());

            if (identityLinks != null && !identityLinks.isEmpty()) {
                for (IdentityLink identityLink : identityLinks) {
                    String type = identityLink.getType();

                    if (type == null)
                        continue;

                    if (type.equals(IdentityLinkType.ASSIGNEE) && identityLink.getUserId() != null)
                        taskBuilder.assignee(userDetailsService.getUser(identityLink.getUserId()));
                    else if (type.equals(IdentityLinkType.CANDIDATE) && identityLink.getUserId() != null)
                        taskBuilder.candidateAssignee(userDetailsService.getUser(identityLink.getUserId()));
                }
            }
        } else if (StringUtils.isNotEmpty(instance.getAssignee())) {
            taskBuilder.assignee(userDetailsService.getUser(instance.getAssignee()));
        }

        return taskBuilder.build();
    }

    private Task convert(HistoricTaskInstance instance, Process process, boolean includeDetails) {
        Task.Builder taskBuilder = new Task.Builder()
                .taskInstanceId(instance.getId())
                .taskDefinitionKey(instance.getTaskDefinitionKey())
                .taskLabel(instance.getName())
                .taskDescription(instance.getDescription())
                .engineProcessInstanceId(instance.getProcessInstanceId())
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .priority(instance.getPriority())
                .startTime(instance.getStartTime())
                .endTime(instance.getEndTime())
                .claimTime(instance.getClaimTime())
                .dueDate(instance.getDueDate())
                .finished();

        if (instance.getDeleteReason() != null && instance.getDeleteReason().equals(Constants.DeleteReasons.CANCELLED))
            taskBuilder.taskStatus(Constants.TaskStatuses.CANCELLED);
        else if (instance.getEndTime() != null)
            taskBuilder.taskStatus(Constants.TaskStatuses.COMPLETE);
        else
            taskBuilder.taskStatus(Constants.TaskStatuses.OPEN);

        if (includeDetails) {
            List<HistoricIdentityLink> identityLinks = processEngine.getHistoryService().getHistoricIdentityLinksForTask(instance.getId());

            if (identityLinks != null && !identityLinks.isEmpty()) {
                for (HistoricIdentityLink identityLink : identityLinks) {
                    String type = identityLink.getType();

                    if (type == null)
                        continue;

                    if (type.equals(IdentityLinkType.ASSIGNEE) && identityLink.getUserId() != null)
                        taskBuilder.assignee(userDetailsService.getUser(identityLink.getUserId()));
                    else if (type.equals(IdentityLinkType.CANDIDATE) && identityLink.getUserId() != null)
                        taskBuilder.candidateAssignee(userDetailsService.getUser(identityLink.getUserId()));
                }
            }
        } else if (StringUtils.isNotEmpty(instance.getAssignee())) {
            taskBuilder.assignee(userDetailsService.getUser(instance.getAssignee()));
        }

        return taskBuilder.build();
    }


    private org.activiti.engine.runtime.ProcessInstance findActivitiInstance(String engineProcessInstanceId, String processBusinessKey) {
		org.activiti.engine.runtime.ProcessInstance activitiInstance = null;
		if (engineProcessInstanceId == null)
			activitiInstance = processEngine.getRuntimeService()
				.createProcessInstanceQuery()
				.processInstanceBusinessKey(processBusinessKey)
				.singleResult();
		else
            activitiInstance = processEngine.getRuntimeService()
				.createProcessInstanceQuery()
				.processInstanceId(engineProcessInstanceId)
				.singleResult();

		return activitiInstance;
	}

    private org.activiti.engine.history.HistoricProcessInstance findActivitiHistoricInstance(String engineProcessDefinitionKey, String engineProcessInstanceId, String processBusinessKey) {
        org.activiti.engine.history.HistoricProcessInstance activitiInstance = null;
        if (engineProcessInstanceId == null)
            activitiInstance = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processDefinitionKey(engineProcessDefinitionKey)
                    .processInstanceBusinessKey(processBusinessKey)
                    .singleResult();
        else
            activitiInstance = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processDefinitionKey(engineProcessDefinitionKey)
                    .processInstanceId(engineProcessInstanceId)
                    .singleResult();

        return activitiInstance;
    }

    private HistoricProcessInstanceQuery instanceQuery(SearchCriteria criteria) {
        HistoricProcessInstanceQuery query = processEngine.getHistoryService().createHistoricProcessInstanceQuery();

        // Activiti only allows us to filter by a single process definition key at a time -- so if there are more than 1
        // we have to filter the result set
        if (criteria.getEngineProcessDefinitionKeys() != null && criteria.getEngineProcessDefinitionKeys().size() == 1)
            query.processDefinitionKey(criteria.getEngineProcessDefinitionKeys().iterator().next());

        List<String> executionIds = criteria.getExecutionIds();
        if (executionIds != null && !executionIds.isEmpty())
            query.processInstanceIds(new HashSet<String>(criteria.getExecutionIds()));
//
//        if (criteria.getBusinessKey() != null)
//            query.processInstanceBusinessKey(criteria.getBusinessKey());
//
//        if (criteria.getStartedAfter() != null)
//            query.startedAfter(criteria.getStartedAfter());
//
//        if (criteria.getStartedBefore() != null)
//            query.startedBefore(criteria.getStartedBefore());
//
//        if (criteria.getCompletedBefore() != null)
//            query.finishedBefore(criteria.getCompletedBefore());
//
//        if (criteria.getCompletedAfter() != null)
//            query.finishedAfter(criteria.getCompletedAfter());

        if (criteria.getInitiatedBy() != null)
            query.startedBy(criteria.getInitiatedBy());

        Sort sort = criteria.getSort(new PassthroughSanitizer());

        if (sort != null) {
//            switch (orderBy) {
//                case START_TIME_ASC:
//                    query.orderByProcessInstanceStartTime().asc();
//                    break;
//                case START_TIME_DESC:
//                    query.orderByProcessInstanceStartTime().desc();
//                    break;
//                case END_TIME_ASC:
//                    query.orderByProcessInstanceEndTime().asc();
//                    break;
//                case END_TIME_DESC:
//                    query.orderByProcessInstanceEndTime().desc();
//                    break;
//            }
        } else {
            query.orderByProcessInstanceEndTime().desc();
        }

//        if (criteria.getComplete() != null) {
//            if (criteria.getComplete().booleanValue())
//                query.finished();
//            else
//                query.unfinished();
//        }

        return query;
    }

    private TaskQuery taskQuery(TaskCriteria criteria) throws ProcessEngineException {
        TaskQuery query = processEngine.getTaskService().createTaskQuery();

        if (criteria.getProcesses() != null && criteria.getProcesses().size() == 1) {
            Process process = criteria.getProcesses().iterator().next();
            ProcessDeployment detail = process.getDeployment();

            if (detail == null)
                throw new ProcessEngineException("No process has been published for " + process.getProcessDefinitionKey());

            query.processDefinitionKey(detail.getEngineProcessDefinitionKey());
        }

        List<String> taskIds = criteria.getTaskIds();
        if (taskIds != null && taskIds.size() == 1)
            query.taskId(taskIds.iterator().next());

        if (StringUtils.isNotEmpty(criteria.getProcessStatus()) && !criteria.getProcessStatus().equals(Constants.ProcessStatuses.OPEN)) {
            if (criteria.getProcessStatus().equals(Constants.ProcessStatuses.SUSPENDED))
                query.suspended();
        } else {
            query.active();
        }

        if (StringUtils.isNotEmpty(criteria.getExecutionId()))
            query.processInstanceId(criteria.getExecutionId());

        if (StringUtils.isNotEmpty(criteria.getBusinessKey()))
            query.processInstanceBusinessKey(criteria.getBusinessKey());

        if (StringUtils.isNotEmpty(criteria.getAssigneeId()))
            query.taskAssignee(criteria.getAssigneeId());

        if (StringUtils.isNotEmpty(criteria.getCandidateAssigneeId()))
            query.taskCandidateUser(criteria.getCandidateAssigneeId());

        if (StringUtils.isNotEmpty(criteria.getParticipantId()))
            query.taskInvolvedUser(criteria.getParticipantId());

        if (criteria.getCreatedAfter() != null)
            query.taskCreatedAfter(criteria.getCreatedAfter());

        if (criteria.getCreatedBefore() != null)
            query.taskCreatedBefore(criteria.getCreatedBefore());

        if (criteria.getDueBefore() != null)
            query.dueBefore(criteria.getDueBefore());

        if (criteria.getDueAfter() != null)
            query.dueAfter(criteria.getDueAfter());

        if (criteria.getMaxPriority() != null)
            query.taskMaxPriority(criteria.getMaxPriority());

        if (criteria.getMinPriority() != null)
            query.taskMinPriority(criteria.getMinPriority());

        if (StringUtils.isNotEmpty(criteria.getProcessInstanceId())) {
            ProcessInstance instance = processInstanceRepository.findOne(criteria.getProcessInstanceId());
            query.processInstanceId(instance.getEngineProcessInstanceId());
        }

        TaskCriteria.OrderBy orderBy = criteria.getOrderBy();
        if (orderBy != null) {
           switch (orderBy) {
               case CREATED_TIME_ASC:
                   query.orderByTaskCreateTime().asc();
                   break;
               case CREATED_TIME_DESC:
                   query.orderByTaskCreateTime().desc();
                   break;
               case DUE_TIME_ASC:
                   query.orderByDueDate().asc();
                   break;
               case DUE_TIME_DESC:
                   query.orderByDueDate().desc();
                   break;
               case PRIORITY_ASC:
                   query.orderByTaskPriority().asc();
                   break;
               case PRIORITY_DESC:
                   query.orderByTaskPriority().desc();
                   break;
           }
        } else {
            query.orderByDueDate().orderByTaskCreateTime().desc();
        }

        if (criteria.getActive() != null) {
            if (criteria.getActive().booleanValue())
                query.active();
            else
                query.suspended();
        }

        return query;
    }

    private HistoricTaskInstanceQuery historicTaskQuery(TaskCriteria criteria) throws ProcessEngineException {

        HistoricTaskInstanceQuery query = processEngine.getHistoryService().createHistoricTaskInstanceQuery();

        if (criteria.getProcesses() != null && criteria.getProcesses().size() == 1) {
            Process process = criteria.getProcesses().iterator().next();
            ProcessDeployment detail = process.getDeployment();

            if (detail == null)
                throw new ProcessEngineException("No process has been published for " + process.getProcessDefinitionKey());

            query.processDefinitionKey(detail.getEngineProcessDefinitionKey());
        }

        if (StringUtils.isNotEmpty(criteria.getProcessStatus()) && !criteria.getProcessStatus().equals(Constants.ProcessStatuses.OPEN)) {
            if (criteria.getProcessStatus().equals(Constants.ProcessStatuses.COMPLETE)) {
                query.taskDeleteReason("completed");
                query.processFinished();
            }
            if (criteria.getProcessStatus().equals(Constants.ProcessStatuses.CANCELLED)) {
                query.taskDeleteReason(Constants.DeleteReasons.CANCELLED);
                query.processFinished();
            }
        } else {
            query.processUnfinished();
        }

        List<String> taskIds = criteria.getTaskIds();
        if (taskIds != null && taskIds.size() == 1)
            query.taskId(taskIds.iterator().next());

        if (criteria.getExecutionId() != null)
            query.executionId(criteria.getExecutionId());

        if (criteria.getAssigneeId() != null)
            query.taskAssignee(criteria.getAssigneeId());

        if (criteria.getDueBefore() != null)
            query.taskDueBefore(criteria.getDueBefore());

        if (criteria.getDueAfter() != null)
            query.taskDueAfter(criteria.getDueAfter());

        if (StringUtils.isNotEmpty(criteria.getAssigneeId()))
            query.taskAssignee(criteria.getAssigneeId());

        if (StringUtils.isNotEmpty(criteria.getParticipantId()))
            query.taskInvolvedUser(criteria.getParticipantId());

        if (StringUtils.isNotEmpty(criteria.getProcessInstanceId())) {
            ProcessInstance instance = processInstanceRepository.findOne(criteria.getProcessInstanceId());
            if (instance != null)
                query.processInstanceId(instance.getEngineProcessInstanceId());
        }

        TaskCriteria.OrderBy orderBy = criteria.getOrderBy();
        if (orderBy != null) {
            switch (orderBy) {
                case CREATED_TIME_ASC:
                    query.orderByHistoricTaskInstanceStartTime().asc();
                    break;
                case CREATED_TIME_DESC:
                    query.orderByHistoricTaskInstanceStartTime().desc();
                    break;
                case DUE_TIME_ASC:
                    query.orderByTaskDueDate().asc();
                    break;
                case DUE_TIME_DESC:
                    query.orderByTaskDueDate().desc();
                    break;
                case PRIORITY_ASC:
                    query.orderByTaskPriority().asc();
                    break;
                case PRIORITY_DESC:
                    query.orderByTaskPriority().desc();
                    break;
            }
        }  else {
            query.orderByHistoricTaskInstanceStartTime().desc();
        }

        if (criteria.getComplete() != null) {
            if (criteria.getComplete().booleanValue())
                query.processFinished();
            else
                query.processUnfinished();
        }

        return query;
    }

    private static void variables(Map<String, Object> variables, Map<String, List<Value>> data) {
        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                List<Value> values = entry.getValue();
                if (values != null) {
                    for (Value value : values) {
                        if (value == null || value.getValue() == null)
                            continue;

                        // Only put the first value in, then break out of the loop
                        variables.put(entry.getKey(), value.getValue());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Class<ProcessEngineProxy> getType() {
        return ProcessEngineProxy.class;
    }

    @Override
    public String getKey() {
        return "activiti";
    }
}
