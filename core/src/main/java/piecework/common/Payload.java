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
package piecework.common;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import piecework.model.Attachment;
import piecework.model.FormValue;
import piecework.model.ProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class Payload {

    public enum PayloadType { INSTANCE, MULTIPART, FORMDATA, FORMVALUEMAP };

    private final PayloadType type;
    private final ProcessInstance instance;
    private final MultipartBody multipartBody;
    private final Map<String, List<String>> formData;
    private final Map<String, FormValue> formValueMap;
    private final List<Attachment> attachments;
    private final RequestDetails requestDetails;
    private final String processDefinitionKey;
    private final String processInstanceId;
    private final String alias;
    private final String requestId;
    private final String taskId;
    private final String validationId;

    private Payload() {
        this(new Builder());
    }

    private Payload(Builder builder) {
        this.type = builder.type;
        this.instance = builder.instance;
        this.multipartBody = builder.multipartBody;
        this.formData = builder.formData;
        this.formValueMap = builder.formValueMap;
        this.attachments = builder.attachments;
        this.requestDetails = builder.requestDetails;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processInstanceId = builder.processInstanceId;
        this.alias = builder.alias;
        this.requestId = builder.requestId;
        this.taskId = builder.taskId;
        this.validationId = builder.validationId;
    }

    public PayloadType getType() {
        return type;
    }

    public ProcessInstance getInstance() {
        return instance;
    }

    public MultipartBody getMultipartBody() {
        return multipartBody;
    }

    public Map<String, List<String>> getFormData() {
        return formData;
    }

    public Map<String, FormValue> getFormValueMap() {
        return formValueMap;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
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

    public String getTaskId() {
        return taskId;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public String getValidationId() {
        return validationId;
    }

    public static final class Builder {

        private PayloadType type;
        private ProcessInstance instance;
        private MultipartBody multipartBody;
        private Map<String, List<String>> formData;
        private Map<String, FormValue> formValueMap;
        private List<Attachment> attachments;
        private RequestDetails requestDetails;
        private String processDefinitionKey;
        private String processInstanceId;
        private String alias;
        private String requestId;
        private String taskId;
        private String validationId;

        public Builder() {

        }

        public Payload build() {
            return new Payload(this);
        }

        @SuppressWarnings("unchecked")
        public Builder processInstance(ProcessInstance instance) {
            this.type = PayloadType.INSTANCE;
            this.instance = instance;
            this.alias = instance.getAlias();
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder multipartBody(MultipartBody multipartBody) {
            this.type = PayloadType.MULTIPART;
            this.multipartBody = multipartBody;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder formData(Map<String, List<String>> formData) {
            this.type = PayloadType.FORMDATA;
            this.formData = formData;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder formValueMap(Map<String, FormValue> formValueMap) {
            this.type = PayloadType.FORMDATA;
            this.formValueMap = formValueMap;
            return this;
        }

        public Builder requestDetails(RequestDetails requestDetails) {
            this.requestDetails = requestDetails;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder validationId(String validationId) {
            this.validationId = validationId;
            return this;
        }

    }

}
