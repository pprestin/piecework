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

import piecework.common.model.User;
import piecework.common.view.ViewContext;
import piecework.form.model.Attachment;

/**
 * @author James Renfro
 */
public abstract class AttachmentBuilder<A extends Attachment> extends Builder {
	private String processDefinitionKey;
	private String processInstanceId;
	private String label;
	private String description;
	private String contentType;
	private String externalUrl;
	private User user;
	private String lastModified;
			
	public AttachmentBuilder() {
		super();
	}
	
	public AttachmentBuilder(Attachment attachment) {
		super(attachment.getId());
		this.processInstanceId = attachment.getProcessInstanceId();
		this.label = attachment.getLabel();
		this.description = attachment.getDescription();
		this.contentType = attachment.getContentType();
		this.externalUrl = attachment.getExternalUrl();
		this.user = attachment.getUser();
		this.lastModified = attachment.getLastModified();
	}
	
	public abstract A build(ViewContext context);
		
//	protected abstract UserBuilder<?> userBuilder(User user);
//
//	@SuppressWarnings("unchecked")
//	public <U extends User> U buildUser(ViewContext context) {
//		if (user == null)
//			return null;
//
//		return (U)user.build(context);
//	}
	
	public AttachmentBuilder<?> processDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
		return this;
	}
	
	public AttachmentBuilder<?> processInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
		return this;
	}
	
	public AttachmentBuilder<?> label(String label) {
		this.label = label;
		return this;
	}
	
	public AttachmentBuilder<?> description(String description) {
		this.description = description;
		return this;
	}
	
	public AttachmentBuilder<?> contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	
	public AttachmentBuilder<?> externalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
		return this;
	}
	
	public AttachmentBuilder<?> user(User user) {
		this.user = user;
		return this;
	}
	
	public AttachmentBuilder<?> lastModified(String lastModified) {
		this.lastModified = lastModified;
		return this;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public String getContentType() {
		return contentType;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public User getUser() {
		return user;
	}

	public String getLastModified() {
		return lastModified;
	}
	
//	@Override
//	public String getUri() {
//		StringBuilder builder = new StringBuilder();
//		
//		if (version != null && ProcessInstance.Constants.RESOURCE_PATH != null && namespace != null && processDefinitionKey != null && processInstanceId != null && id != null) {
//			builder.append(serviceUri).append("/")
//				.append(version).append("/")
//				.append(ProcessInstance.Constants.RESOURCE_PATH).append("/")
//				.append(namespace).append("/")
//				.append(processDefinitionKey).append("/")
//				.append(processInstanceId).append("/attachment/");
//			
//			builder.append(id);
//			
//			return builder.toString();
//		}
//		
//		return null;
//	}
}
