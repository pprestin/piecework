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
package piecework.form.model.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import piecework.common.view.ViewContext;
import piecework.form.model.Form;
import piecework.form.model.Section;
import piecework.form.model.view.AttachmentView;
import piecework.form.model.view.SectionView;
import piecework.util.LayoutUtil;

/**
 * @author James Renfro
 */
public abstract class FormBuilder<F extends Form> extends Builder {
	private String name;
	private String label;
	private String processDefinitionKey;
	private String taskDefinitionKey;
	private String processInstanceId;
	private List<String> formIds;
	private List<SectionBuilder<?>> sections;
	private List<SectionBuilder<?>> dialogs;
	private List<AttachmentView.Builder> attachments;
	private LayoutUtil.Layout layout;
	private String message;
	private String actionUrl;
	private String logoUrl;
	private String requestUrl;
	private String responseUrl;
	private String replaceFrom;
	private Boolean readOnly;
	private String submissionId;

	public FormBuilder() {
		super();
	}

	public FormBuilder(Form form) {
		super(form.getId());
		this.name = form.getName();
		this.label = form.getLabel();
		this.message = form.getMessage();
		this.layout = LayoutUtil.getLayout(form.getLayout());
		this.processDefinitionKey = form.getProcessDefinitionKey();
		this.taskDefinitionKey = form.getTaskDefinitionKey();
		this.dialogs = sectionBuilders(form.getDialogs());
		this.sections = sectionBuilders(form.getSections());
		this.logoUrl = form.getLogoUrl();
		this.submissionId = form.getSubmissionId();
	}
	
	public abstract F build();
	
	public abstract F build(ViewContext context);
	
	public <S extends Section> List<S> buildSections(FormBuilder<?> formBuilder, List<SectionBuilder<?>> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		
		List<S> sections = new ArrayList<S>(builders.size());
		boolean beforeSelectedSection = true;
		boolean formReadOnly = formBuilder != null && formBuilder.readOnly != null ? formBuilder.readOnly.booleanValue() : false;
		for (SectionBuilder<?> builder : builders) {
			String sectionName = builder.getName();
			boolean selected = false;
			boolean readOnly = false;
			boolean selectable = false;
			switch (layout) {
			case PANELS:
				selected = beforeSelectedSection;
				readOnly = formReadOnly;
				selectable = true;
				break;
			case FLOW:
			case WIZARD:
				selected = LayoutUtil.isSelectedSection(layout, taskDefinitionKey, sectionName);
				readOnly = formReadOnly || (!selected && beforeSelectedSection);
				selectable = beforeSelectedSection;
				break;
			}
			builder.editable(!readOnly);
			builder.selected(selected);
			builder.visible(selectable);
			sections.add((S) builder.build());
			if (selected)
				beforeSelectedSection = false;
		}
		return Collections.unmodifiableList(sections);
	}
	
	protected abstract <S extends Section> SectionBuilder<S> sectionBuilder(S section);
	
	private List<SectionBuilder<?>> sectionBuilders(List<Section> sections) {
		if (sections == null || sections.isEmpty())
			return null;
		
		List<SectionBuilder<?>> builders = new ArrayList<SectionBuilder<?>>(sections.size());
		for (Section section : sections) {
			builders.add(sectionBuilder(section));
		}
		return Collections.unmodifiableList(builders);
	}
	
	public FormBuilder<F> name(String name) {
		this.name = name;
		return this;
	}

	public FormBuilder<F> label(String label) {
		this.label = label;
		return this;
	}

	public FormBuilder<F> processDefinitionKey(String processDefinitionKey) {
		// super.container(processDefinitionKey);
		this.processDefinitionKey = processDefinitionKey;
		return this;
	}

	public FormBuilder<F> taskDefinitionKey(String taskDefinitionKey) {
		this.taskDefinitionKey = taskDefinitionKey;
		return this;
	}
	
	public FormBuilder<F> processInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
		return this;
	}

	public FormBuilder<F> formId(String formId) {
		if (this.formIds == null)
			this.formIds = new ArrayList<String>();
		this.formIds.add(formId);
		return this;
	}

	public FormBuilder<F> submissionId(String submissionId) {
		this.submissionId = submissionId;
		return this;
	}

	public FormBuilder<F> section(SectionView.Builder section) {
		if (this.sections == null)
			this.sections = new ArrayList<SectionBuilder<?>>();
		this.sections.add(section);
		return this;
	}

	public FormBuilder<F> dialog(SectionView.Builder dialog) {
		if (this.dialogs == null)
			this.dialogs = new ArrayList<SectionBuilder<?>>();
		this.dialogs.add(dialog);
		return this;
	}

	public FormBuilder<F> attachment(AttachmentView.Builder attachment) {
		if (this.attachments == null)
			this.attachments = new ArrayList<AttachmentView.Builder>();
		this.attachments.add(attachment);
		return this;
	}

	public FormBuilder<F> layout(String layout) {
		this.layout = LayoutUtil.getLayout(layout);
		return this;
	}

	public FormBuilder<F> message(String message) {
		this.message = message;
		return this;
	}

	public FormBuilder<F> actionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
		return this;
	}

	public FormBuilder<F> logoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
		return this;
	}

	public FormBuilder<F> requestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
		return this;
	}

	public FormBuilder<F> responseUrl(String responseUrl) {
		this.responseUrl = responseUrl;
		return this;
	}

	public FormBuilder<F> replaceFrom(String replaceFrom) {
		this.replaceFrom = replaceFrom;
		return this;
	}

	public FormBuilder<F> readOnly(Boolean readOnly) {
		this.readOnly = readOnly;
		return this;
	}

	public void clearDialogs() {
		this.dialogs = new ArrayList<SectionBuilder<?>>();;
	}

	public List<SectionBuilder<?>> getDialogs() {
		return this.dialogs;
	}

	public List<SectionBuilder<?>> getSections() {
		return this.sections;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}

	public List<String> getFormIds() {
		return formIds;
	}

	public List<AttachmentView.Builder> getAttachments() {
		return attachments;
	}

	public String getLayout() {
		return layout.toString();
	}

	public String getMessage() {
		return message;
	}

	public String getActionUrl() {
		return actionUrl;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public String getResponseUrl() {
		return responseUrl;
	}

	public String getReplaceFrom() {
		return replaceFrom;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

}
