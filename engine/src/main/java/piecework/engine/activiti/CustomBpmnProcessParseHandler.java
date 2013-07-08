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

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James Renfro
 */
@Service("customBpmnProcessParseHandler")
public class CustomBpmnProcessParseHandler extends AbstractBpmnParseHandler<Process> {

    @Autowired
    GeneralExecutionListener generalExecutionListener;

    @Override
    protected void executeParse(BpmnParse bpmnParse, Process element) {
//        List<ActivitiListener> executionListeners = element.getExecutionListeners();
//        if (executionListeners == null) {
//            executionListeners = new ArrayList<ActivitiListener>();
//            element.setExecutionListeners(executionListeners);
//        }
//
//        ActivitiListener endEventListener = new ActivitiListener();
//        endEventListener.setEvent("end");
//        endEventListener.setImplementation("generalExecutionListener");
//        executionListeners.add(endEventListener);

        ProcessDefinitionEntity processDefinitionEntity = bpmnParse.getCurrentProcessDefinition();
        processDefinitionEntity.addExecutionListener(ExecutionListener.EVENTNAME_END, generalExecutionListener);
    }

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return Process.class;
    }

}
