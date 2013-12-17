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
package piecework.validation;

import java.io.Serializable;
import java.util.*;

import piecework.Constants;
import piecework.model.*;
import piecework.model.Process;
import piecework.common.ManyMap;

/**
 * @author James Renfro
 */
public class Validation implements Serializable {

	private static final long serialVersionUID = 8445504602269832413L;

    private final String title;

    private final Map<String, List<Value>> data;
	
	private final List<Attachment> attachments;
	
	private final Map<String, List<Message>> results;
	
	private final Set<String> unchangedFields;

    private final Submission submission;

    private final Process process;

    private final ProcessInstance instance;

    private final Task task;

    private final String applicationStatusExplanation;

    private final boolean hasError;
	
	private Validation() {
		this(new Builder());
	}
	
	private Validation(Builder builder) {
        this.title = builder.title;
		this.results = builder.results != null ? Collections.unmodifiableMap(builder.results) : null;
        this.data = builder.data;
        this.attachments = builder.attachments != null ? Collections.unmodifiableList(builder.attachments) : null;
		this.unchangedFields = builder.unchangedFields != null ? Collections.unmodifiableSet(builder.unchangedFields) : null;
	    this.submission = builder.submission;
        this.process = builder.process;
        this.instance = builder.instance;
        this.task = builder.task;
        this.applicationStatusExplanation = builder.applicationStatusExplanation;
        this.hasError = builder.hasError;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, List<Value>> getData() {
        return data;
    }

    public List<Attachment> getAttachments() {
		return attachments;
	}

	public Map<String, List<Message>> getResults() {
		return results;
	}

	public Set<String> getUnchangedFields() {
		return unchangedFields;
	}

    public Submission getSubmission() {
        return submission;
    }

    public Process getProcess() {
        return process;
    }

    public ProcessInstance getInstance() {
        return instance;
    }

    public Task getTask() {
        return task;
    }

    public String getApplicationStatusExplanation() {
        return applicationStatusExplanation;
    }

    public boolean isHasError() {
        return hasError;
    }

    public final static class Builder {

        private String title;
        private ManyMap<String, Value> data;
        private ManyMap<String, Message> results;
        private List<Attachment> attachments;
        private Set<String> unchangedFields;
        private Submission submission;
        private Process process;
        private ProcessInstance instance;
        private Task task;
        private String applicationStatusExplanation;
        private boolean hasError;

        public Builder() {
            super();
            this.attachments = new ArrayList<Attachment>();
            this.data = new ManyMap<String, Value>();
            this.results = new ManyMap<String, Message>();
        }

        public Builder(Validation validation) {
            this.attachments = validation.getAttachments() != null ? new ArrayList<Attachment>(validation.getAttachments()) : new ArrayList<Attachment>();
            this.data = validation.getData() != null ? new ManyMap<String, Value>(validation.getData()) : new ManyMap<String, Value>();
            this.results = validation.getResults() != null ? new ManyMap<String, Message>(validation.getResults()) : new ManyMap<String, Message>();
            this.title = validation.getTitle();
            this.applicationStatusExplanation = validation.getApplicationStatusExplanation();
            this.unchangedFields = validation.getUnchangedFields() != null ? new HashSet<String>(validation.getUnchangedFields()) : new HashSet<String>();
            this.hasError = validation.isHasError();
            this.process = validation.getProcess();
            this.instance = validation.getInstance();
            this.task = validation.getTask();
            this.submission = validation.getSubmission();
        }
        
        public Validation build() {
        	return new Validation(this);
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder error(String name, String message) {
            this.results.putOne(name, new Message.Builder().type(Constants.ValidationStatus.ERROR).text(message).build());
        	return this;
        }
        
        public Builder warning(String name, String message) {
            this.results.putOne(name, new Message.Builder().type(Constants.ValidationStatus.WARNING).text(message).build());
        	return this;
        }
        
        public Builder result(String type, String name, String message) {
            this.results.putOne(name, new Message.Builder().type(type).text(message).build());
        	return this;
        }

        public Builder data(ManyMap<String, Value> data) {
            if (data != null && !data.isEmpty()) {
                this.data.putAll(data);
            }
            return this;
        }

        public Builder messages(Map<String, List<Message>> messages) {
            if (messages != null)
                this.results.putAll(messages);
            return this;
        }

        public <V extends Value> Builder formValue(String key, V ... values) {
            if (values != null && values.length > 0)
                this.data.put(key, Arrays.<Value>asList(values));
            else
                this.data.put(key, Collections.<Value>emptyList());
            return this;
        }

        public <V extends Value> Builder formValue(String key, Collection<V> values) {
            if (values != null && values.size() > 0)
                this.data.put(key, new ArrayList<Value>(values));
            else
                this.data.put(key, Collections.<Value>emptyList());
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
            if (attachments != null)
        	    this.attachments.addAll(attachments);
        	return this;
        }
        
        public Builder unchangedField(String unchangedField) {
        	if (this.unchangedFields == null)
        		this.unchangedFields = new HashSet<String>();
        	this.unchangedFields.add(unchangedField);
        	return this;
        }

        public Builder submission(Submission submission) {
            this.submission = submission;
            if (submission != null)
                attachments(submission.getAttachments());
            return this;
        }

        public Builder process(Process process) {
            this.process = process;
            return this;
        }

        public Builder instance(ProcessInstance instance) {
            this.instance = instance;
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
            return this;
        }

        public Builder applicationStatusExplanation(String applicationStatusExplanation) {
            this.applicationStatusExplanation = applicationStatusExplanation;
            return this;
        }

        public Builder hasError(boolean hasError) {
            this.hasError = hasError;
            return this;
        }
	}
	
}
