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

import com.mongodb.WriteResult;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.persistence.ProcessInstanceRepository;
import piecework.persistence.ProcessRepository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import java.util.Date;

/**
 * @author James Renfro
 */
@Service("generalExecutionListener")
public class GeneralExecutionListener implements ExecutionListener {

    private static final Logger LOG = Logger.getLogger(GeneralExecutionListener.class);

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
                Process process = processRepository.findOne(processInstance.getProcessDefinitionKey());
                if (process != null) {
                    ProcessDeployment deployment = process.getDeployment();
                    if (deployment != null)
                        completionStatus = deployment.getCompletionStatus();
                }

//                ProcessInstance.Builder builder = new ProcessInstance.Builder(processInstance);
//                builder.applicationStatus(completionStatus);
//                builder.processStatus(Constants.ProcessStatuses.COMPLETE);
//                builder.endTime(new Date());
//                processInstance = builder.build();
//                processInstance = processInstanceRepository.save(processInstance);

                WriteResult result = mongoOperations.updateFirst(new Query(where("_id").is(processInstance.getProcessInstanceId())),
                        new Update().set("endTime", new Date())
                                .set("applicationStatus", completionStatus)
                                .set("processStatus", Constants.ProcessStatuses.COMPLETE),
                        ProcessInstance.class);

                String error = result.getError();
                if (StringUtils.isNotEmpty(error)) {
                    LOG.error("Unable to correctly save final state of process instance " + processInstance.getProcessInstanceId() + ": " + error);
                }
            } else {
                LOG.error("Unable to save final state of process instance with execution business key " + businessKey);
            }


        }
    }

}
