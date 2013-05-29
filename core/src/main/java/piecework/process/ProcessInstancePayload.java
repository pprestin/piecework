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
package piecework.process;

import piecework.common.Payload;
import piecework.common.RequestDetails;
import piecework.model.ProcessInstance;

/**
 * @author James Renfro
 */
public class ProcessInstancePayload extends Payload<ProcessInstance> {

    private RequestDetails requestDetails;
    private String processInstanceId;
    private String alias;
    private String requestId;
    private String validationId;

    public ProcessInstancePayload processInstance(ProcessInstance instance) {
        super.processInstance(instance);
        this.alias = instance.getAlias();
        return this;
    }

    public ProcessInstancePayload requestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
        return this;
    }

    public ProcessInstancePayload processInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public ProcessInstancePayload alias(String alias) {
        this.alias = alias;
        return this;
    }

    public ProcessInstancePayload requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ProcessInstancePayload validationId(String validationId) {
        this.validationId = validationId;
        return this;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getAlias() {
        return alias;
    }

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getValidationId() {
        return validationId;
    }
}
