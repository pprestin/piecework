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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.util.ManyMap;

import javax.xml.bind.annotation.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormSubmission.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormSubmission.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "submission")
public class FormSubmission {

    @XmlAttribute
    @XmlID
    @Id
    private final String submissionId;

    @XmlTransient
    private final String submissionType;

    @XmlElement
    private final String requestId;

    @XmlElementWrapper(name="formData")
    @XmlElementRef
    private final List<FormValue> formData;

    @XmlElementWrapper(name="attachments")
    @XmlElementRef
    @DBRef
    private final List<Attachment> attachments;

    @XmlTransient
    private final Date submissionDate;

    @XmlTransient
    private final String submitterId;

    private FormSubmission() {
        this(new FormSubmission.Builder());
    }

    private FormSubmission(FormSubmission.Builder builder) {
        this.submissionId = builder.submissionId;
        this.submissionType = builder.submissionType;
        this.requestId = builder.requestId;

        List<FormValue> formValues = new ArrayList<FormValue>();
        if (builder.formValueBuilderMap != null) {
            for (FormValue.Builder formValueBuilder : builder.formValueBuilderMap.values()) {
                formValues.add(formValueBuilder.build());
            }
        }
        this.formData = Collections.unmodifiableList(formValues);
        this.attachments = builder.attachments != null ? Collections.unmodifiableList(builder.attachments) : null;
        this.submissionDate = builder.submissionDate;
        this.submitterId = builder.submitterId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    @JsonIgnore
    public String getSubmissionType() {
        return submissionType;
    }

    public String getRequestId() {
        return requestId;
    }

    public List<FormValue> getFormData() {
        return formData;
    }

    @JsonIgnore
    public ManyMap<String, Attachment> getAttachmentMap() {
        ManyMap<String, Attachment> map = new ManyMap<String, Attachment>();
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                map.putOne(attachment.getName(), attachment);
            }
        }
        return map;
    }

    @JsonIgnore
    public Map<String, FormValue> getFormValueMap() {
        Map<String, FormValue> map = new HashMap<String, FormValue>();
    	if (formData != null && !formData.isEmpty()) {
    		for (FormValue formValue : formData) {
                String name = formValue.getName();
                List<String> values = formValue.getAllValues();
                if (name != null && values != null)
    			    map.put(name, formValue);
    		}
    	}
    	return map;
    }

//    public ManyMap<String, String> getFormValueContentMap() {
//    	ManyMap<String, String> map = new ManyMap<String, String>();
//    	if (formData != null && !formData.isEmpty()) {
//    		for (FormValue formValue : formData) {
//                String name = formValue.getName();
//                List<String> values = formValue.getAllValues();
//                if (name != null && values != null)
//    			    map.put(name, values);
//    		}
//    	}
//    	return map;
//    }

    public List<Attachment> getAttachments() {
		return attachments;
	}

    @JsonIgnore
	public Date getSubmissionDate() {
        return submissionDate;
    }

    @JsonIgnore
    public String getSubmitterId() {
        return submitterId;
    }

    public final static class Builder {

        private String submissionId;
        private String submissionType;
        private String requestId;
        private Map<String, FormValue.Builder> formValueBuilderMap = new HashMap<String, FormValue.Builder>();
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
                for (FormValue formValue : submission.formData) {
                    formValueBuilderMap.put(formValue.getName(), new FormValue.Builder(formValue, sanitizer));
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

//        public Builder formContent(String contentType, String key, String value, String location) {
//            if (this.formData == null)
//                this.formData = new ArrayList<FormValue>();
//            this.formData.add(new FormValue.Builder().contentType(contentType).name(key).value(value).location(location).build());
//            return this;
//        }

        public Builder formData(Map<String, List<String>> formData) {
            for (Map.Entry<String, List<String>> entry : formData.entrySet()) {
                formValue(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder formValue(String key, String ... values) {
            FormValue.Builder formValueBuilder = formValueBuilderMap.get(key);
            if (formValueBuilder == null)
                formValueBuilder = new FormValue.Builder();

            if (values != null) {
                for (String value : values) {
                    formValueBuilder.value(value);
                }
            }

            return this;
        }

        public Builder formValue(String key, List<String> values) {
            if (values == null)
                formValue(key);

            return formValue(key, values.toArray(new String[values.size()]));
        }

        public Builder formValue(FormValue formValue) {
            return formValue(formValue.getName(), formValue.getAllValues());
        }

        public Builder formValueMap(Map<String, FormValue> formValueMap) {
            for (Map.Entry<String, FormValue> entry : formValueMap.entrySet()) {
                formValue(entry.getValue());
            }
            return this;
        }

        public Builder attachment(Attachment attachment) {
            if (this.attachments == null)
                this.attachments = new ArrayList<Attachment>();
            this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<Attachment> attachments) {
            if (attachments != null) {
                if (this.attachments == null)
                    this.attachments = new ArrayList<Attachment>();
                this.attachments.addAll(attachments);
            }
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Submission";
        public static final String ROOT_ELEMENT_NAME = "submission";
        public static final String TYPE_NAME = "SubmissionType";
    }
}
