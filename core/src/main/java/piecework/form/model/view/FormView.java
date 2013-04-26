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
package piecework.form.model.view;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import piecework.common.view.ViewContext;
import piecework.form.model.Form;
import piecework.form.model.Section;
import piecework.form.model.builder.FormBuilder;
import piecework.form.model.builder.SectionBuilder;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormView.Constants.TYPE_NAME)
public class FormView implements Form {
	
	private static final long serialVersionUID = -7335911090264165468L;

	@XmlAttribute
	@XmlID
	private final String id;
	
	@XmlAttribute(name=FormView.Attributes.PROCESS_INSTANCE_ID)
	private final String processInstanceId;
	
	@XmlAttribute(name = FormView.Attributes.READ_ONLY)
	private final String readOnly;
	
	@XmlAttribute(name = FormView.Attributes.SUBMISSION_ID)
	private final String submissionId;
	
	@XmlElement
	private final String name;
	
	@XmlElement
	private final String label;
	
	@XmlElement
	private final String taskDefinitionKey;
	
	@XmlElement
	private final String layout;
	
	@XmlElement
	private final String logoUrl;
	
	@XmlElement
	private final String message;
	
	@XmlElement
	private final String replaceFrom;

	@XmlElementWrapper(name = FormView.Lists.SECTIONS)
	@XmlElementRef
	private final List<SectionView> sections;
	
	@XmlElementWrapper(name = FormView.Lists.DIALOGS)
	@XmlElement(name = "dialog")
	private final List<SectionView> dialogs;
	
	@XmlElementWrapper(name = FormView.Lists.ATTACHMENTS)
	@XmlElementRef
	private final List<AttachmentView> attachments;
	
	@XmlTransient
	private final String processDefinitionKey;
	
	@XmlTransient
	private final String actionUrl;

	private FormView() {
		this(new FormView.Builder(), null);
	}
			
	private FormView(FormView.Builder builder, ViewContext context) {
		this.id = builder.getId();
		this.name = builder.getName();
		this.label = builder.getLabel();
		this.processDefinitionKey = builder.getProcessDefinitionKey();
		this.taskDefinitionKey = builder.getTaskDefinitionKey();
		this.processInstanceId = builder.getProcessInstanceId();
		this.sections = builder.buildSections(builder, builder.getSections());
		this.dialogs = builder.buildSections(null, builder.getDialogs());
		this.attachments = builder.buildAttachments(builder.getAttachments());
		this.layout = builder.getLayout();
		this.message = builder.getMessage();
		this.actionUrl = builder.getActionUrl();
		this.readOnly = builder.getReadOnly() != null ? builder.getReadOnly().toString() : null;
		this.replaceFrom = builder.getReplaceFrom();
		this.logoUrl = builder.getLogoUrl();
		this.submissionId = builder.getSubmissionId();
	}
		
//	private static List<SectionView> buildSections(FormView.Builder formBuilder, List<SectionBuilder<?>> builders) {
//		if (builders == null || builders.isEmpty())
//			return null;
//		
////		String taskDefinitionKey = formBuilder != null ? formBuilder.taskDefinitionKey : null;
////		LayoutUtil.Layout layout = LayoutUtil.getLayout(formBuilder != null ? formBuilder.layout : null);
//		
//		List<SectionView> sections = new ArrayList<SectionView>(builders.size());
////		boolean beforeSelectedSection = true;
////		boolean formReadOnly = formBuilder != null && formBuilder.readOnly != null ? formBuilder.readOnly.booleanValue() : false;
//		for (SectionBuilder<?> builder : builders) {
////			String sectionName = builder.getName();
////			boolean selected = false;
////			boolean readOnly = false;
////			boolean selectable = false;
////			switch (layout) {
////			case PANELS:
////				selected = beforeSelectedSection;
////				readOnly = formReadOnly;
////				selectable = true;
////				break;
////			case FLOW:
////			case WIZARD:
////				selected = LayoutUtil.isSelectedSection(layout, taskDefinitionKey, sectionName);
////				readOnly = formReadOnly || (!selected && beforeSelectedSection);
////				selectable = beforeSelectedSection;
////				break;
////			}
//			sections.add((SectionView) builder.build());
////			if (selected)
////				beforeSelectedSection = false;
//		}
//		return Collections.unmodifiableList(sections);
//	}
	
	@Override
	public String getId() {
		return id;
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

	@SuppressWarnings("unchecked")
	public List<SectionView> getSections() {
		return sections;
	}

	@SuppressWarnings("unchecked")
	public List<SectionView> getDialogs() {
		return dialogs;
	}

	public List<AttachmentView> getAttachments() {
		return attachments;
	}

	public String getLayout() {
		return layout;
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

//	public String getUri() {
//		return uri;
//	}

	static class Attributes {
		final static String ID = "id";
		final static String PROCESS_INSTANCE_ID = "processInstanceId";
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

	public final static class Builder extends FormBuilder<FormView> {

		public Builder() {
			super();
		}
		
		public Builder(Form form) {
			super(form);
		}
		
		@Override
		public FormView build() {
			return new FormView();
		}

		@Override
		public FormView build(ViewContext context) {
			return new FormView(this, context);
		}
		
		public List<AttachmentView> buildAttachments(List<AttachmentView.Builder> builders) {
			
			return Collections.emptyList();
		}
		
		@SuppressWarnings("unchecked")
		protected <S extends Section> SectionBuilder<S> sectionBuilder(S section) {
			return (SectionBuilder<S>) new SectionView.Builder(section);
		}
		
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}
}
	

