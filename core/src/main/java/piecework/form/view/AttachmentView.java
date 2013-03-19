package piecework.form.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import piecework.common.model.User;
import piecework.common.view.UserView;
import piecework.common.view.View;
import piecework.common.view.ViewContext;
import piecework.form.model.Attachment;

/**
 * This bean is used to generate xml and json representations for of a workflow
 * attachment. 
 * 
 * @author Leman Chung
 */
@XmlRootElement(name = AttachmentView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = AttachmentView.Constants.TYPE_NAME)
public final class AttachmentView extends View implements Attachment {

	private static final long serialVersionUID = -4270137246501437074L;
	
	@XmlElement(name = Elements.PROCESS_INSTANCE_ID)
	private final String processInstanceId;
	
	@XmlElement(name = Elements.LABEL)
	private final String label;
	
	@XmlElement(name = Elements.DESCRIPTION)
	private final String description;
	
	@XmlElement(name = Elements.CONTENT_TYPE)
	private final String contentType;
	
	@XmlElement(name = Elements.EXTERNAL_URL)
	private final String externalUrl;
	
	@XmlElement(name = Elements.USER)
	private final UserView user;
	
	@XmlElement(name = Elements.LAST_MODIFIED)
	private final String lastModified;
	
	private AttachmentView() {
		this(new AttachmentView.Builder(), new ViewContext());
	}
	
	private AttachmentView(AttachmentView.Builder builder, ViewContext context) {
		super(builder, context);
		this.processInstanceId = builder.processInstanceId;
		this.label = builder.label;
		this.description = builder.description;
		this.contentType = builder.contentType;
		this.externalUrl = builder.externalUrl;
		this.user = builder.user.build(context);
		this.lastModified = builder.lastModified;
	}
	
//	@Override
//	public String getUri() {
//		return uri;
//	}
	
	public String getLabel() {
		return label;
	}

	public String getLastModified() {
		return lastModified;
	}

	public String getDescription() {
		return description;
	}

	public String getContentType() {
		return contentType;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public UserView getUser() {
		return user;
	}
	
	public static class Constants {
		public static final String ROOT_ELEMENT_NAME = "attachment";
		public static final String TYPE_NAME = "AttachmentType";
		public static final String RESOURCE_LABEL = "Attachments";
	}
	
	static class Elements {
		static final String CONTENT_TYPE = "contentType";
		static final String DESCRIPTION = "description";
		static final String EXTERNAL_URL = "externalUrl";
		static final String LABEL = "label";
		static final String LAST_MODIFIED = "lastModified";
		static final String PROCESS_INSTANCE = "processInstance";
		static final String PROCESS_INSTANCE_ID = "processInstanceId";
		static final String USER = "user";
	}
	
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder extends View.Builder {
		
		private String processDefinitionKey;
		private String processInstanceId;
		private String label;
		private String description;
		private String contentType;
		private String externalUrl;
		private UserView.Builder user;
		private String lastModified;
				
		public AttachmentView build(ViewContext context) {
			return new AttachmentView(this, context);
		}
		
		@Override
		public AttachmentView.Builder id(String id) {
			this.id = id;
			return this;
		}
		
		public AttachmentView.Builder processDefinitionKey(String processDefinitionKey) {
			this.processDefinitionKey = processDefinitionKey;
			return this;
		}
		
		public AttachmentView.Builder processInstanceId(String processInstanceId) {
			this.processInstanceId = processInstanceId;
			return this;
		}
		
		public AttachmentView.Builder label(String label) {
			this.label = label;
			return this;
		}
		
		public AttachmentView.Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public AttachmentView.Builder contentType(String contentType) {
			this.contentType = contentType;
			return this;
		}
		
		public AttachmentView.Builder externalUrl(String externalUrl) {
			this.externalUrl = externalUrl;
			return this;
		}
		
		public AttachmentView.Builder user(UserView.Builder user) {
			this.user = user;
			return this;
		}
		
		public AttachmentView.Builder lastModified(String lastModified) {
			this.lastModified = lastModified;
			return this;
		}
		
//		@Override
//		public String getUri() {
//			StringBuilder builder = new StringBuilder();
//			
//			if (version != null && ProcessInstance.Constants.RESOURCE_PATH != null && namespace != null && processDefinitionKey != null && processInstanceId != null && id != null) {
//				builder.append(serviceUri).append("/")
//					.append(version).append("/")
//					.append(ProcessInstance.Constants.RESOURCE_PATH).append("/")
//					.append(namespace).append("/")
//					.append(processDefinitionKey).append("/")
//					.append(processInstanceId).append("/attachment/");
//				
//				builder.append(id);
//				
//				return builder.toString();
//			}
//			
//			return null;
//		}
		
	}
}
