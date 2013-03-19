/*
 * Copyright 2011 University of Washington
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
package piecework.form.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import piecework.common.view.View;
import piecework.common.view.ViewContext;
import piecework.form.model.Form;
import piecework.form.model.Section;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormView.Constants.TYPE_NAME)
public class FormView extends View implements Form {
	
	private static final long serialVersionUID = -7335911090264165468L;

	@XmlElement(name = FormView.Elements.NAME, required = true)
	private final String name;
	
	@XmlElement(name = FormView.Elements.LABEL)
	private final String label;
	
	@XmlTransient
	private final String processDefinitionKey;
	
	@XmlElement(name = FormView.Elements.TASK_DEFINITION_KEY)
	private final String taskDefinitionKey;
	
	@XmlElementWrapper(name = FormView.Lists.SECTIONS)
	@XmlElementRef(name = SectionView.Constants.ROOT_ELEMENT_NAME)
	private final List<SectionView> sections;
	
	@XmlElementWrapper(name = FormView.Lists.DIALOGS)
	@XmlElement(name = "dialog")
	private final List<SectionView> dialogs;
	
	@XmlElementWrapper(name = FormView.Lists.ATTACHMENTS)
	@XmlElement(name = "attachment")
	private final List<AttachmentView> attachments;
	
	@XmlElement(name = FormView.Elements.LAYOUT)
	private final String layout;
	
	@XmlElement(name = FormView.Elements.MESSAGE)
	private final String message;
	
//	@XmlTransient
//	private final String actionUrl;
	
	@XmlElement(name = FormView.Elements.LOGO_URL)
	private final String logoUrl;
	
	@XmlElement(name = FormView.Elements.REQUEST_URL)
	private final String requestUrl;
	
	@XmlElement(name = FormView.Elements.RESPONSE_URL)
	private final String responseUrl;
	
	@XmlElement(name = FormView.Elements.REPLACE_FROM)
	private final String replaceFrom;
	
	@XmlAttribute(name = FormView.Attributes.READ_ONLY)
	private final String readOnly;
	
	@XmlAttribute(name = FormView.Attributes.SUBMISSION_ID)
	private final String submissionId;
	
	@SuppressWarnings("unused")
	private FormView() {
		this(new FormView.Builder(), new ViewContext());
	}
			
	FormView(FormView.Builder builder, ViewContext context) {
		super(builder, context);
		this.name = builder.name;
		this.label = builder.label;
		this.processDefinitionKey = builder.processDefinitionKey;
		this.taskDefinitionKey = builder.taskDefinitionKey;
		this.sections = buildSections(builder, builder.sections);
		this.dialogs = buildSections(null, builder.dialogs);
		this.attachments = buildAttachments(builder.attachments);
		this.layout = builder.layout;
		this.message = builder.message;
//		this.actionUrl = builder.actionUrl;
		this.requestUrl = builder.requestUrl;
		this.responseUrl = builder.responseUrl;
		this.readOnly = builder.readOnly != null ? builder.readOnly.toString() : null;
		this.replaceFrom = builder.replaceFrom;
		this.logoUrl = builder.logoUrl;
		this.submissionId = builder.submissionId;
	}
	
	private static List<AttachmentView> buildAttachments(List<AttachmentView.Builder> builders) {
		
		return Collections.emptyList();
	}
	
	private static List<SectionView> buildSections(FormView.Builder formBuilder, List<SectionView.Builder> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		
//		String taskDefinitionKey = formBuilder != null ? formBuilder.taskDefinitionKey : null;
//		LayoutUtil.Layout layout = LayoutUtil.getLayout(formBuilder != null ? formBuilder.layout : null);
		
		List<SectionView> sections = new ArrayList<SectionView>(builders.size());
//		boolean beforeSelectedSection = true;
//		boolean formReadOnly = formBuilder != null && formBuilder.readOnly != null ? formBuilder.readOnly.booleanValue() : false;
		for (SectionView.Builder builder : builders) {
//			String sectionName = builder.getName();
//			boolean selected = false;
//			boolean readOnly = false;
//			boolean selectable = false;
//			switch (layout) {
//			case PANELS:
//				selected = beforeSelectedSection;
//				readOnly = formReadOnly;
//				selectable = true;
//				break;
//			case FLOW:
//			case WIZARD:
//				selected = LayoutUtil.isSelectedSection(layout, taskDefinitionKey, sectionName);
//				readOnly = formReadOnly || (!selected && beforeSelectedSection);
//				selectable = beforeSelectedSection;
//				break;
//			}
			sections.add(builder.build());
//			if (selected)
//				beforeSelectedSection = false;
		}
		return Collections.unmodifiableList(sections);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public String getMessage() {
		return message;
	}

	public List<SectionView> getSections() {
		return sections;
	}

	public List<SectionView> getDialogs() {
		return dialogs;
	}

	public List<AttachmentView> getAttachments() {
		return attachments;
	}

	public String getLayout() {
		return layout;
	}
	
	@Override
	public String getRequestUrl() {
		return requestUrl;
	}

	public String getResponseUrl() {
		return responseUrl;
	}
	
//	@Override
//	public String getResourcePath() {
//		return Constants.ROOT_ELEMENT_NAME;
//	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}

	public String getReplaceFrom() {
		return replaceFrom;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public String getReadOnly() {
		return readOnly;
	}
	
	public String getSubmissionId() {
		return submissionId;
	}

	@Override
	public String getUri() {
		return uri;
	}

	static class Attributes {
		final static String ID = "id";
		final static String READ_ONLY = "readOnly";
		final static String SUBMISSION_ID = "submissionId";
	}
	
	public static class Constants {
		public static final String RESOURCE_LABEL = "Form";
		public static final String ROOT_ELEMENT_NAME = "form";
		public static final String TYPE_NAME = "FormType";
		public static final String START_TASK_DEFINITION_KEY = "__startTask";
		public static final String OVERSIGHT_DEFINITION_KEY = "__general";
	}

	static class Elements {
		static final String ACTION_SECTION = "actionSection";
		static final String ACTION_URL = "actionUrl";
		static final String FORM_ID = "formId";
		static final String NAME = "name";
		static final String LABEL = "label";
		static final String LAYOUT = "layout";
		static final String LOGO_URL = "logoUrl";
		static final String MESSAGE = "message";
		static final String REPLACE_FROM = "replaceFrom";
		static final String REQUEST_URL = "requestUrl";
		static final String RESPONSE_URL = "responseUrl";
		static final String TASK_DEFINITION_KEY = "taskId";
	}
	
	public static class Layouts {
		public static final String SINGLE_SCREEN = "Single Screen";
		public static final String WIZARD = "Wizard";
	}
	
	static class Lists {
		static final String ATTACHMENTS = "attachments";
		static final String DIALOGS = "dialogs";
		static final String FORM_IDS = "formIds";
		static final String SECTIONS = "sections";
	}

//	public String getActionUrl() {
//		StringBuilder builder = new StringBuilder();
//		
//		String serviceUri = getServiceUri();
//		String version = getVersion();
//		String resourcePath = "form"; //getResourcePath();
//		String namespace = getNamespace();
//		String container = getContainer();
//		
//		if (version != null && resourcePath != null && namespace != null) {
//			builder.append(serviceUri).append("/")
//				.append(version).append("/")
//				.append(resourcePath).append("/")
//				.append(namespace).append("/");
//			
//			if (container != null)
//				builder.append(container).append("/");
//			
//			return builder.toString();
//		}
//		
//		return null;
//	}

	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder extends View.Builder {

		private String name;
		private String label;
		private String processDefinitionKey;
		private String taskDefinitionKey;
		private List<String> formIds;
		private List<SectionView.Builder> sections;
		private List<SectionView.Builder> dialogs;
		private List<AttachmentView.Builder> attachments;
		private String layout;
		private String message;
		private String actionUrl;
		private String logoUrl;
		private String requestUrl;
		private String responseUrl;
		private String replaceFrom;
		private Boolean readOnly;
		private String submissionId;
				
		public Builder() {
			super();
		}
		
		public Builder(Form transfer) {
			super();
			this.name = transfer.getName();
			this.label = transfer.getLabel();
//			this.message = transfer.getMessage();
//			this.processDefinitionKey = transfer.getProcessDefinitionKey();
//			this.taskDefinitionKey = transfer.getTaskDefinitionKey();
			List<Section> dialogContracts = transfer.getDialogs();
			if (dialogContracts != null && !dialogContracts.isEmpty()) {
				this.dialogs = new ArrayList<SectionView.Builder>(dialogContracts.size());
				for (Section dialogContract : dialogContracts) {
					this.dialogs.add(new SectionView.Builder(dialogContract));
				}
			} else {
				this.dialogs = null;
			}
			List<Section> sectionContracts = transfer.getSections();
			if (sectionContracts != null && !sectionContracts.isEmpty()) {
				this.sections = new ArrayList<SectionView.Builder>(sectionContracts.size());
				for (Section sectionContract : sectionContracts) {
					this.sections.add(new SectionView.Builder(sectionContract));
				}
			} else {
				this.sections = null;
			}
			this.logoUrl = transfer.getLogoUrl();
			this.requestUrl = transfer.getRequestUrl();
			this.responseUrl = transfer.getResponseUrl();
			this.layout = transfer.getLayout();
//			this.submissionId = transfer.getSubmissionId();
		}
		
		public FormView build(ViewContext context) {
			return new FormView(this, context);
		}
		
		public FormView.Builder id(String id) {
			this.id = id;
			return this;
		}
		
		public FormView.Builder name(String name) {
			this.name = name;
			return this;
		}

		public FormView.Builder label(String label) {
			this.label = label;
			return this;
		}

		public FormView.Builder processDefinitionKey(String processDefinitionKey) {
//			super.container(processDefinitionKey);
			this.processDefinitionKey = processDefinitionKey;
			return this;
		}

		public FormView.Builder taskDefinitionKey(String taskDefinitionKey) {
			this.taskDefinitionKey = taskDefinitionKey;
			return this;
		}
		
		public FormView.Builder formId(String formId) {
			if (this.formIds == null)
				this.formIds = new ArrayList<String>();
			this.formIds.add(formId);
			return this;
		}
		
		public FormView.Builder submissionId(String submissionId) {
			this.submissionId = submissionId;
			return this;
		}
		
		public FormView.Builder section(SectionView.Builder section) {
			if (this.sections == null)
				this.sections = new ArrayList<SectionView.Builder>();
			this.sections.add(section);
			return this;
		}
		
		public FormView.Builder dialog(SectionView.Builder dialog) {
			if (this.dialogs == null)
				this.dialogs = new ArrayList<SectionView.Builder>();
			this.dialogs.add(dialog);
			return this;
		}
		
		public FormView.Builder attachment(AttachmentView.Builder attachment) {
			if (this.attachments == null)
				this.attachments = new ArrayList<AttachmentView.Builder>();
			this.attachments.add(attachment);
			return this;
		}
		
		public FormView.Builder layout(String layout) {
			this.layout = layout;
			return this;
		}
		
		public FormView.Builder message(String message) {
			this.message = message;
			return this;
		}
		
		public FormView.Builder actionUrl(String actionUrl) {
			this.actionUrl = actionUrl;
			return this;
		}
		
		public FormView.Builder logoUrl(String logoUrl) {
			this.logoUrl = logoUrl;
			return this;
		}
		
		public FormView.Builder requestUrl(String requestUrl) {
			this.requestUrl = requestUrl;
			return this;
		}
		
		public FormView.Builder responseUrl(String responseUrl) {
			this.responseUrl = responseUrl;
			return this;
		}
		
		public FormView.Builder replaceFrom(String replaceFrom) {
			this.replaceFrom = replaceFrom;
			return this;
		}
		
		public FormView.Builder readOnly(Boolean readOnly) {
			this.readOnly = readOnly;
			return this;
		}
		
		public void clearDialogs() {
			this.dialogs = new ArrayList<SectionView.Builder>();
		}

		public List<SectionView.Builder> getDialogs() { 
			return this.dialogs;
		}
		
		public List<SectionView.Builder> getSections() { 
			return this.sections;
		}
		
	}
	
}

