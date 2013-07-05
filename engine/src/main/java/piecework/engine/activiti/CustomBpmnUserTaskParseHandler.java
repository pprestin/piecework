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

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.task.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class CustomBpmnUserTaskParseHandler extends AbstractBpmnParseHandler<UserTask> {

    @Autowired
    GeneralUserTaskListener generalUserTaskListener;

    @Override
    protected void executeParse(BpmnParse bpmnParse, UserTask element) {
        TaskDefinition taskDefinition = (TaskDefinition) bpmnParse.getCurrentActivity().getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_ALL_EVENTS, generalUserTaskListener);

//        List<ActivitiListener> taskListeners = element.getTaskListeners();
//        if (taskListeners == null) {
//            taskListeners = new ArrayList<ActivitiListener>();
//            element.setTaskListeners(taskListeners);
//        }
//
//        ActivitiListener createTaskListener = new ActivitiListener();
//        createTaskListener.setEvent(TaskListener.EVENTNAME_ALL_EVENTS);
//        createTaskListener.setImplementation("${generalUserTaskListener}");
//        createTaskListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
////        taskListeners.add(createTaskListener);
//
//        bpmnParse.getListenerFactory().createDelegateExpressionTaskListener(createTaskListener);
//
//
//        bpmnParse.
    }

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return UserTask.class;
    }

}
