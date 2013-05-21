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
package piecework.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import piecework.Constants;
import piecework.model.Attachment;
import piecework.model.FormValue;
import piecework.model.ValidationResult;

/**
 * @author James Renfro
 */
public class FormValidation implements Serializable {

	private static final long serialVersionUID = 8445504602269832413L;
	
	private final List<FormValue> formData;
	
	private final List<Attachment> attachments;
	
	private final List<ValidationResult> results;
	
	private final Set<String> unchangedFields;
	
	private FormValidation() {
		this(new Builder());
	}
	
	private FormValidation(Builder builder) {
		this.results = builder.results != null ? Collections.unmodifiableList(builder.results) : null;
		this.formData = builder.formData != null ? Collections.unmodifiableList(builder.formData) : null;
		this.attachments = builder.attachments != null ? Collections.unmodifiableList(builder.attachments) : null;
		this.unchangedFields = builder.unchangedFields != null ? Collections.unmodifiableSet(builder.unchangedFields) : null;
	}
	
	public List<FormValue> getFormData() {
		return formData;
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

	public final static class Builder {

        private List<ValidationResult> results;
        private List<FormValue> formData;
        private List<Attachment> attachments;
        private Set<String> unchangedFields;

        public Builder() {
            super();
        }
        
        public FormValidation build() {
        	return new FormValidation(this);
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
        	this.attachments = attachments;
        	return this;
        }
        
        public Builder unchangedField(String unchangedField) {
        	if (this.unchangedFields == null)
        		this.unchangedFields = new HashSet<String>();
        	this.unchangedFields.add(unchangedField);
        	return this;
        }
        
	}
	
}
