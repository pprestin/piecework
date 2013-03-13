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
package piecework.form.record;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.form.model.Form;
import piecework.form.model.Section;

/**
 * @author James Renfro
 */
@Document(collection = "form")
public class FormRecord implements Form {

	@Id
	private String id;
		
	private String name;
	
	private String label;
	
	private String layout;
	
	private String logoUrl;
	
	private String requestUrl;
	
	private String responseUrl;
	
	private List<SectionRecord> dialogs;
	
	private List<SectionRecord> sections;
	
	private String lastModifiedBy;
	
	private Long version;

	public FormRecord() {

	}
	
	@SuppressWarnings("rawtypes")
	public FormRecord(String taskDefinitionKey, Form contract, String lastModifiedBy) {
		this(taskDefinitionKey, contract, lastModifiedBy, null);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FormRecord(String taskDefinitionKey, Form contract, String lastModifiedBy, Long version) {
		this.id = taskDefinitionKey;
		this.name = contract.getName();
		this.label = contract.getLabel();
		this.layout = contract.getLayout();
		this.logoUrl = contract.getLogoUrl();
		this.requestUrl = contract.getRequestUrl();
		this.responseUrl = contract.getResponseUrl();
		List<? extends Section> dialogContracts = contract.getDialogs();
		if (dialogContracts != null && !dialogContracts.isEmpty()) {
			this.dialogs = new ArrayList<SectionRecord>(dialogContracts.size());
			for (Section dialogContract : dialogContracts) {
				this.dialogs.add(new SectionRecord(dialogContract));
			}
		}
		List<? extends Section> sectionContracts = contract.getSections();
		if (sectionContracts != null && !sectionContracts.isEmpty()) {
			this.sections = new ArrayList<SectionRecord>(sectionContracts.size());
			for (Section sectionContract : sectionContracts) {
				this.sections.add(new SectionRecord(sectionContract));
			}
		}
		this.lastModifiedBy = lastModifiedBy;
	}
	
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

	public String getRequestUrl() {
		return requestUrl;
	}

	public String getResponseUrl() {
		return responseUrl;
	}

	public void setResponseUrl(String responseUrl) {
		this.responseUrl = responseUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.requestUrl = remoteUrl;
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

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	
}
