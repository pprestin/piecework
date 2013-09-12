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
import java.util.Map;

/**
 * @author James Renfro
 */
@Service("customBpmnUserTaskParseHandler")
public class CustomBpmnUserTaskParseHandler extends AbstractBpmnParseHandler<UserTask> {

    @Autowired(required = false)
    GeneralUserTaskListener generalUserTaskListener;

    @Override
    protected void executeParse(BpmnParse bpmnParse, UserTask element) {
        if (generalUserTaskListener != null) {
            TaskDefinition taskDefinition = (TaskDefinition) bpmnParse.getCurrentActivity().getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);

            Map<String, List<TaskListener>> listeners = taskDefinition.getTaskListeners();
            for(Map.Entry<String, List<TaskListener>> entry : taskDefinition.getTaskListeners().entrySet())
            {
                entry.getValue().add(0, generalUserTaskListener);
            }
            taskDefinition.setTaskListeners(listeners);
        }
    }

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return UserTask.class;
    }

}
