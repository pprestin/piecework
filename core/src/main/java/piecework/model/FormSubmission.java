/*
 * Copyright 2012 University of Washington
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
package piecework.model;

import java.util.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Document(collection = "submission")
public class FormSubmission {

    @Id
    private final String submissionId;

    private final String submissionType;

    private final String requestId;

    private final List<FormValue> formData;
    
    @DBRef
    private final List<Attachment> attachments;

    private final Date submissionDate;

    private final String submitterId;

    private FormSubmission() {
        this(new FormSubmission.Builder());
    }

    private FormSubmission(FormSubmission.Builder builder) {
        this.submissionId = builder.submissionId;
        this.submissionType = builder.submissionType;
        this.requestId = builder.requestId;
        this.formData = builder.formData != null ? Collections.unmodifiableList(builder.formData) : null;
        this.attachments = builder.attachments != null ? Collections.unmodifiableList(builder.attachments) : null;
        this.submissionDate = builder.submissionDate;
        this.submitterId = builder.submitterId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public String getRequestId() {
        return requestId;
    }

    public List<FormValue> getFormData() {
        return formData;
    }
    
    public ManyMap<String, String> getFormValueMap() {
    	ManyMap<String, String> map = new ManyMap<String, String>();
    	if (formData != null && !formData.isEmpty()) {
    		for (FormValue formValue : formData) {
    			map.put(formValue.getName(), formValue.getAllValues());
    		}
    	}
    	return map;
    }

    public List<Attachment> getAttachments() {
		return attachments;
	}

	public Date getSubmissionDate() {
        return submissionDate;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public final static class Builder {

        private String submissionId;
        private String submissionType;
        private String requestId;
        private List<FormValue> formData;
        private List<Attachment> attachments;
        private Date submissionDate;
        private String submitterId;

        public Builder() {
            super();
        }

        public Builder(FormSubmission submission, Sanitizer sanitizer) {
            this.submissionId = sanitizer.sanitize(submission.submissionId);
            this.submissionType = sanitizer.sanitize(submission.submissionType);
            this.requestId = sanitizer.sanitize(submission.requestId);
            this.submissionDate = submission.submissionDate;
            this.submissionId = sanitizer.sanitize(submissionId);

            if (submission.formData != null && !submission.formData.isEmpty()) {
                this.formData = new ArrayList<FormValue>(submission.formData.size());
                for (FormValue formValue : submission.formData) {
                    this.formData.add(new FormValue.Builder(formValue, sanitizer).build());
                }
            }
            
            if (submission.attachments != null && !submission.attachments.isEmpty()) {
                this.attachments = new ArrayList<Attachment>(submission.attachments.size());
                for (Attachment attachment : submission.attachments) {
                    this.attachments.add(new Attachment.Builder(attachment, sanitizer).build());
                }
            }
        }

        public FormSubmission build() {
            return new FormSubmission(this);
        }

        public Builder submissionId(String submissionId) {
            this.submissionId = submissionId;
            return this;
        }

        public Builder submissionType(String submissionType) {
            this.submissionType = submissionType;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder submissionDate(Date submissionDate) {
            this.submissionDate = submissionDate;
            return this;
        }

        public Builder submitterId(String submitterId) {
            this.submitterId = submitterId;
            return this;
        }

        public Builder formContent(String contentType, String key, String value, String location) {
            if (this.formData == null)
                this.formData = new ArrayList<FormValue>();
            this.formData.add(new FormValue.Builder().contentType(contentType).name(key).value(value).location(location).build());
            return this;
        }

        public Builder formData(Map<String, List<String>> formData) {
            for (Map.Entry<String, List<String>> entry : formData.entrySet()) {
                formValue(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder formValue(String key, String ... values) {
            if (this.formData == null)
                this.formData = new ArrayList<FormValue>();
            this.formData.add(new FormValue.Builder().name(key).values(values).build());
            return this;
        }

        public Builder formValue(String key, List<String> values) {
            if (this.formData == null)
                this.formData = new ArrayList<FormValue>();
            this.formData.add(new FormValue.Builder().name(key).values(values).build());
            return this;
        }
        
        public Builder attachment(Attachment attachment) {
            if (this.attachments == null)
                this.attachments = new ArrayList<Attachment>();
            this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<Attachment> attachments) {
            if (this.attachments == null)
                this.attachments = new ArrayList<Attachment>();
            this.attachments.addAll(attachments);
            return this;
        }
    }
}
