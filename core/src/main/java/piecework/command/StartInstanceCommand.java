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
package piecework.command;

import com.mongodb.WriteResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import piecework.Constants;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityDetails;
import piecework.identity.IdentityService;
import piecework.model.*;
import piecework.persistence.ProcessInstanceRepository;
import piecework.Toolkit;
import piecework.identity.IdentityHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class StartInstanceCommand extends InstanceCommand {

    private static final Logger LOG = Logger.getLogger(StartInstanceCommand.class);

    public StartInstanceCommand(piecework.model.Process process) {
        super(process);
    }

    @Override
    public ProcessInstance execute(Toolkit toolkit) throws StatusCodeError {

        if (LOG.isDebugEnabled())
            LOG.debug("Executing start instance command " + this.toString());

        ProcessEngineFacade facade = toolkit.getFacade();
        IdentityHelper helper = toolkit.getHelper();
        IdentityService identityService = toolkit.getIdentityService();
        MongoTemplate operations = toolkit.getMongoOperations();
        ProcessInstanceRepository repository = toolkit.getProcessInstanceRepository();

        ProcessInstance.Builder builder;
        try {
            IdentityDetails user = helper.getAuthenticatedPrincipal();
            String initiatorId = user != null ? user.getInternalId() : null;
            String initiationStatus = process.getInitiationStatus();

            if (helper.isAuthenticatedSystem() && StringUtils.isNotEmpty(submission.getSubmitterId())) {
                User submitter = identityService.getUserByAnyId(submission.getSubmitterId());
                if (submitter != null)
                    initiatorId = submitter.getUserId();
            }

            builder = new ProcessInstance.Builder()
                    .processDefinitionKey(process.getProcessDefinitionKey())
                    .processDefinitionLabel(process.getProcessDefinitionLabel())
                    .processInstanceLabel(label)
                    .data(data)
                    .submission(submission)
                    .startTime(new Date())
                    .initiatorId(initiatorId)
                    .processStatus(Constants.ProcessStatuses.OPEN)
                    .attachments(attachments)
                    .applicationStatus(initiationStatus);

            // Save it before routing, then save again with the engine instance id
            ProcessInstance stored = repository.save(builder.build());

            Map<String, String> variables = new HashMap<String, String>();
            variables.put("PIECEWORK_PROCESS_DEFINITION_KEY", process.getProcessDefinitionKey());
            variables.put("PIECEWORK_PROCESS_INSTANCE_ID", stored.getProcessInstanceId());
            variables.put("PIECEWORK_PROCESS_INSTANCE_LABEL", label);

            String engineInstanceId = facade.start(process, stored.getProcessInstanceId(), variables);

            builder.processInstanceId(stored.getProcessInstanceId());
            builder.engineProcessInstanceId(engineInstanceId);

            WriteResult result = operations.updateFirst(new Query(where("_id").is(stored.getProcessInstanceId())),
                    new Update().set("engineProcessInstanceId", engineInstanceId),
                    ProcessInstance.class);

            String error = result.getError();
            if (StringUtils.isNotEmpty(error))
                LOG.error("Unable to correctly save engine instance id " + engineInstanceId + " for " + stored.getProcessInstanceId() + ": " + error);

            if (LOG.isDebugEnabled())
                LOG.debug("Executed start instance command " + this.toString());

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to start instance ", e);
            throw new InternalServerError();
        }

        return builder.build();
    }

    public String toString() {
        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : "";

        return "{ processDefinitionKey: \"" + processDefinitionKey + "\"}";
    }

}
