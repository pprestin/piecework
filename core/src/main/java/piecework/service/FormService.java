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
import org.springframework.stereotype.Service;

/**
 * @author James Renfro
 */
@Service
public class FormService {

    private static final Logger LOG = Logger.getLogger(FormService.class);
    private static final String VERSION = "v1";

//    @Autowired
//    CommandFactory commandFactory;
//
//    @Autowired
//    DeploymentService deploymentService;
//
//    @Autowired
//    RequestService requestService;
//
//    @Autowired
//    TaskService taskService;
//
//    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, Entity principal) throws PieceworkException {
//        return taskService.search(rawQueryParameters, principal, true, false);
//    }
//
//    public <T, P extends ProcessDeploymentProvider> SubmissionCommandResponse save(P provider, RequestDetails requestDetails, FormRequest request, T data, Class<T> type, Entity principal) throws PieceworkException {
//        Validation validation = commandFactory.validation(provider, request, data, type, VERSION).execute();
//        return commandFactory.submitForm(principal, deployment, validation, ActionType.SAVE, requestDetails, request).execute();
//    }
//
//    public <T, P extends ProcessDeploymentProvider> SubmissionCommandResponse submit(P provider, RequestDetails requestDetails, FormRequest request, T data, Class<T> type, Entity principal) throws PieceworkException {
//        Validation validation = commandFactory.validation(process, deployment, request, data, type, principal, VERSION).execute();
//        return commandFactory.submitForm(principal, deployment, validation, request.getAction(), requestDetails, request).execute();
//    }
//
//    public <T, P extends ProcessDeploymentProvider> void validate(P provider, RequestDetails requestDetails, FormRequest request, final T data, final Class<T> type, String validationId, Entity principal) throws PieceworkException {
//
//        commandFactory.validation(process, deployment, request, data, type, principal, validationId, null, VERSION).execute();
//    }

}
