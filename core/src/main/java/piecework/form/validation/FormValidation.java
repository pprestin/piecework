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
package piecework.form.validation;

import java.io.Serializable;
import java.util.*;

import piecework.Constants;
import piecework.model.*;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
public class FormValidation implements Serializable {

	private static final long serialVersionUID = 8445504602269832413L;

    private final String title;
	
	private final Map<String, List<String>> formValueMap;

    private final Map<String, List<String>> restrictedValueMap;
	
	private final List<Attachment> attachments;
	
	private final List<ValidationResult> results;
	
	private final Set<String> unchangedFields;

    private final FormSubmission submission;

    private final ProcessInstance instance;
	
	private FormValidation() {
		this(new Builder());
	}
	
	private FormValidation(Builder builder) {
        this.title = builder.title;
		this.results = builder.results != null ? Collections.unmodifiableList(builder.results) : null;
		this.formValueMap = builder.formValueMap != null ? Collections.unmodifiableMap(builder.formValueMap) : null;
        this.restrictedValueMap = builder.restrictedValueMap != null ? Collections.unmodifiableMap(builder.restrictedValueMap) : null;
		this.attachments = builder.attachments != null ? Collections.unmodifiableList(builder.attachments) : null;
		this.unchangedFields = builder.unchangedFields != null ? Collections.unmodifiableSet(builder.unchangedFields) : null;
	    this.submission = builder.submission;
        this.instance = builder.instance;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, List<String>> getFormValueMap() {
		return formValueMap;
	}

    public Map<String, List<String>> getRestrictedValueMap() {
        return restrictedValueMap;
    }

    public List<Attachment> getAttachments() {
		return attachments;
	}

	public List<ValidationResult> getResults() {
		return results;
	}

	public Set<String> getUnchangedFields() {
		return unchangedFields;
	}

    public FormSubmission getSubmission() {
        return submission;
    }

    public ProcessInstance getInstance() {
        return instance;
    }

    public final static class Builder {

        private String title;
        private List<ValidationResult> results;
        private ManyMap<String, String> formValueMap;
        private ManyMap<String, String> restrictedValueMap;
        private List<Attachment> attachments;
        private Set<String> unchangedFields;
        private FormSubmission submission;
        private ProcessInstance instance;

        public Builder() {
            super();
        }
        
        public FormValidation build() {
        	return new FormValidation(this);
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder error(String name, String message) {
        	if (results == null)
        		results = new ArrayList<ValidationResult>();
        	this.results.add(new ValidationResult(Constants.ValidationStatus.ERROR, name, message));
        	return this;
        }
        
        public Builder warning(String name, String message) {
        	if (results == null)
        		results = new ArrayList<ValidationResult>();
        	this.results.add(new ValidationResult(Constants.ValidationStatus.WARNING, name, message));
        	return this;
        }
        
        public Builder result(String type, String name, String message) {
        	if (results == null)
        		results = new ArrayList<ValidationResult>();
        	this.results.add(new ValidationResult(type, name, message));
        	return this;
        }
        
        public Builder formValue(String key, String ... values) {
            if (this.formValueMap == null)
                this.formValueMap = new ManyMap<String, String>();
            this.formValueMap.put(key, Arrays.asList(values));
            return this;
        }

        public Builder restrictedValue(String key, String ... values) {
            if (this.restrictedValueMap == null)
                this.restrictedValueMap = new ManyMap<String, String>();
            this.restrictedValueMap.put(key, Arrays.asList(values));
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
        
        public Builder unchangedField(String unchangedField) {
        	if (this.unchangedFields == null)
        		this.unchangedFields = new HashSet<String>();
        	this.unchangedFields.add(unchangedField);
        	return this;
        }

        public Builder submission(FormSubmission submission) {
            this.submission = submission;
            return this;
        }

        public Builder instance(ProcessInstance instance) {
            this.instance = instance;
            return this;
        }
        
	}
	
}
