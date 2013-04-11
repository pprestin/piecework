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
package piecework.form.model.record;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.common.view.ViewContext;
import piecework.form.model.Form;
import piecework.form.model.Section;
import piecework.form.model.builder.FormBuilder;
import piecework.form.model.builder.SectionBuilder;
import piecework.form.model.view.SectionView;

/**
 * @author James Renfro
 */
@Document(collection = "form")
public class FormRecord implements Form {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;
		
	private String processDefinitionKey;
	
	private String taskDefinitionKey;
	
	private String name;
	
	private String label;
	
	private String layout;
	
	private String logoUrl;
	
	private List<SectionRecord> dialogs;
	
	private List<SectionRecord> sections;
	
	private String lastModifiedBy;
	
	private Long version;

	private FormRecord() {

	}
	
	private FormRecord(FormRecord.Builder builder) {
		this.id = builder.getId();
		this.name = builder.getName();
		this.label = builder.getLabel();
		this.processDefinitionKey = builder.getProcessDefinitionKey();
		this.taskDefinitionKey = builder.getTaskDefinitionKey();
		this.sections = builder.buildSections(builder, builder.getSections());
		this.dialogs = builder.buildSections(null, builder.getDialogs());
//		this.attachments = buildAttachments(builder.getAttachments());
		this.layout = builder.getLayout();
//		this.message = builder.getMessage();
//		this.actionUrl = builder.getActionUrl();
//		this.readOnly = builder.getReadOnly() != null ? builder.getReadOnly().toString() : null;
//		this.replaceFrom = builder.getReplaceFrom();
		this.logoUrl = builder.getLogoUrl();
//		this.submissionId = builder.getSubmissionId();
	}
	
//	@SuppressWarnings("rawtypes")
//	public FormRecord(String taskDefinitionKey, Form contract, String lastModifiedBy) {
//		this(taskDefinitionKey, contract, lastModifiedBy, null);
//	}
//	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public FormRecord(String taskDefinitionKey, Form contract, String lastModifiedBy, Long version) {
//		this.id = taskDefinitionKey;
//		this.name = contract.getName();
//		this.label = contract.getLabel();
//		this.layout = contract.getLayout();
//		this.logoUrl = contract.getLogoUrl();
//		this.requestUrl = contract.getRequestUrl();
//		this.responseUrl = contract.getResponseUrl();
//		List<? extends Section> dialogContracts = contract.getDialogs();
//		if (dialogContracts != null && !dialogContracts.isEmpty()) {
//			this.dialogs = new ArrayList<SectionRecord>(dialogContracts.size());
//			for (Section dialogContract : dialogContracts) {
//				this.dialogs.add(new SectionRecord(dialogContract));
//			}
//		}
//		List<? extends Section> sectionContracts = contract.getSections();
//		if (sectionContracts != null && !sectionContracts.isEmpty()) {
//			this.sections = new ArrayList<SectionRecord>(sectionContracts.size());
//			for (Section sectionContract : sectionContracts) {
//				this.sections.add(new SectionRecord(sectionContract));
//			}
//		}
//		this.lastModifiedBy = lastModifiedBy;
//	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public List<SectionRecord> getDialogs() {
		return dialogs;
	}

	public void setDialogs(List<SectionRecord> dialogs) {
		this.dialogs = dialogs;
	}

	public List<SectionRecord> getSections() {
		return sections;
	}

	public void setSections(List<SectionRecord> sections) {
		this.sections = sections;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	
//	protected String constructId(String namespace, String processDefinitionKey, String taskDefinitionKey, Long version) {
//		if (version != null) 
//			return IdentifierUtil.constructId(namespace, processDefinitionKey, taskDefinitionKey, String.valueOf(version));
//		
//		return IdentifierUtil.constructId(namespace, processDefinitionKey, taskDefinitionKey);
//	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	
	public final static class Builder extends FormBuilder<FormRecord> {

		public Builder() {
			super();
		}
		
		public Builder(Form form) {
			super(form);
		}
		
		@Override
		public FormRecord build() {
			return new FormRecord(this);
		}

		@Override
		public FormRecord build(ViewContext context) {
			return new FormRecord(this);
		}
		
		@SuppressWarnings("unchecked")
		protected <S extends Section> SectionBuilder<S> sectionBuilder(S section) {
			return (SectionBuilder<S>) new SectionRecord.Builder((SectionRecord) section);
		}
		
	}


	@Override
	public String getMessage() {
		return null;
	}

	@Override
	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	@Override
	public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}

	@Override
	public String getSubmissionId() {
		return null;
	}

	@Override
	public String getProcessInstanceId() {
		return null;
	}
	
}
