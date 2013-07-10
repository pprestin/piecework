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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.model.ProcessInstance;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessRepository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import java.util.Date;

/**
 * @author James Renfro
 */
@Service("generalExecutionListener")
public class GeneralExecutionListener implements ExecutionListener {

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    MongoOperations mongoOperations;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String eventName = execution.getEventName();

        if (eventName != null && eventName.equals(EVENTNAME_END)) {
            // Business key will always be the actual Piecework process instance id
            String businessKey = execution.getProcessBusinessKey();
            String completionStatus = null;

            ProcessInstance processInstance = processInstanceRepository.findOne(businessKey);
            if (processInstance != null) {
                piecework.model.Process process = processRepository.findOne(processInstance.getProcessDefinitionKey());
                if (process != null) {
                    completionStatus = process.getCompletionStatus();
                }
            }

            mongoOperations.updateFirst(new Query(where("_id").is(businessKey)),
                new Update().set("endTime", new Date())
                    .set("applicationStatus", completionStatus)
                    .set("processStatus", Constants.ProcessStatuses.COMPLETE),
                ProcessInstance.class);
        }
    }

}
