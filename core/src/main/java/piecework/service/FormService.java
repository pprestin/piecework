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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.command.CommandFactory;
import piecework.command.SubmissionCommandResponse;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author James Renfro
 */
@Service
public class FormService {

    private static final Logger LOG = Logger.getLogger(FormService.class);
    private static final String VERSION = "v1";

    @Autowired
    CommandFactory commandFactory;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    RequestService requestService;

    @Autowired
    TaskService taskService;

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, Entity principal) throws PieceworkException {
        return taskService.search(rawQueryParameters, principal, true, false);
    }

    public <T> SubmissionCommandResponse save(Process process, RequestDetails requestDetails, String requestId, T data, Class<T> type, Entity principal) throws PieceworkException {
        FormRequest request = requestService.read(requestDetails, requestId);
        ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
        Validation validation = commandFactory.validation(process, deployment, request, data, type, principal, VERSION).execute();
        return commandFactory.submitForm(principal, deployment, validation, ActionType.SAVE, requestDetails, request).execute();
    }

    public <T> SubmissionCommandResponse submit(Process process, RequestDetails requestDetails, String requestId, T data, Class<T> type, Entity principal) throws PieceworkException {
        FormRequest request = requestService.read(requestDetails, requestId);
        ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
        Validation validation = commandFactory.validation(process, deployment, request, data, type, principal, VERSION).execute();
        return commandFactory.submitForm(principal, deployment, validation, request.getAction(), requestDetails, request).execute();
    }

    public <T> void validate(Process process, RequestDetails requestDetails, String requestId, final T data, final Class<T> type, String validationId, Entity principal) throws PieceworkException {
        FormRequest request = requestService.read(requestDetails, requestId);
        ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
        commandFactory.validation(process, deployment, request, data, type, principal, validationId, null, VERSION).execute();
    }

}
