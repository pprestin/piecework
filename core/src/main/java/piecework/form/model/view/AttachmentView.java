package piecework.form.model.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import piecework.common.model.User;
import piecework.common.view.ViewContext;
import piecework.form.model.Attachment;
import piecework.form.model.builder.AttachmentBuilder;
import piecework.form.model.builder.UserBuilder;

/**
 * This bean is used to generate xml and json representations for of a workflow
 * attachment. 
 * 
 * @author Leman Chung
 */
@XmlRootElement(name = AttachmentView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = AttachmentView.Constants.TYPE_NAME)
public final class AttachmentView implements Attachment {

	private static final long serialVersionUID = -4270137246501437074L;
	
	@XmlAttribute(name=FormView.Attributes.ID)
	@XmlID
	protected final String id;
	
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
	private final User user;
	
	@XmlElement(name = Elements.LAST_MODIFIED)
	private final String lastModified;
	
	private AttachmentView() {
		this(new AttachmentView.Builder(), new ViewContext());
	}
	
	private AttachmentView(AttachmentBuilder<?> builder, ViewContext context) {
		this.id = builder.getId();
		this.processInstanceId = builder.getProcessInstanceId();
		this.label = builder.getLabel();
		this.description = builder.getDescription();
		this.contentType = builder.getContentType();
		this.externalUrl = builder.getExternalUrl();
//		this.user = builder.buildUser(context);
        this.user = null;
		this.lastModified = builder.getLastModified();
	}
	
//	@Override
//	public String getUri() {
//		return uri;
//	}
	
	public String getId() {
		return id;
	}
	
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

	public <U extends User> U getUser() {
		return (U)user;
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
	
	public final static class Builder extends AttachmentBuilder<AttachmentView> {

		public Builder() {
			super();
		}
		
		public Builder(Attachment attachment) {
			super(attachment);
		}
		
		@Override
		public AttachmentView build(ViewContext context) {
			return new AttachmentView(this, context);
		}

		
	}
}
