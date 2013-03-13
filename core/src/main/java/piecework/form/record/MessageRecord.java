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

import piecework.form.model.Message;

/**
 * @author James Renfro
 */
public class MessageRecord implements Message {

	public enum MessageLevel { FORM, SECTION, FIELD, ELEMENT };
	
	private final String messageLevel;
	private final String id;
	private final String referenceId;
	private final String referenceName;
	private final String message;
	private final String messageType;
	
	private MessageRecord() {
		this(new MessageRecord.Builder(MessageLevel.FIELD.toString(), null));
	}
	
	private MessageRecord(MessageRecord.Builder builder) {
		this.messageLevel = builder.messageLevel;
		this.id = builder.id;
		this.referenceId = builder.referenceId;
		this.referenceName = builder.referenceName;
		this.message = builder.message;
		this.messageType = builder.messageType;
	}

	public String getMessageLevel() {
		return messageLevel;
	}

	public String getId() {
		return id;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public String getReferenceName() {
		return referenceName;
	}

	public String getMessage() {
		return message;
	}

	public String getMessageType() {
		return messageType;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(referenceName != null ? referenceName : "Reference name not found")
			.append(": ").append(messageType).append(": ").append(message);
		
		return builder.toString();
	}

	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		
		private final String id;
		private final String messageLevel;
		private String referenceId;
		private String referenceName;
		private String message;
		private String messageType;
		
		public Builder(String messageLevel, String id) {
			this.messageLevel = messageLevel;
			this.id = id;
		}
		
		public Builder(Message reference) {
			this.id = reference.getId();
			this.messageLevel = reference.getMessageLevel().toString();
			this.referenceId = reference.getReferenceId();
			this.referenceName = reference.getReferenceName();
			this.message = reference.getMessage();
			this.messageType = reference.getMessageType();
		}
		
		public MessageRecord build() {
			return new MessageRecord(this);
		}
		
		public MessageRecord.Builder referenceId(String referenceId) {
			this.referenceId = referenceId;
			return this;
		}
		
		public MessageRecord.Builder referenceName(String referenceName) {
			this.referenceName = referenceName;
			return this;
		}
		
		public MessageRecord.Builder message(String message) {
			this.message = message;
			return this;
		}
		
		public MessageRecord.Builder messageType(String messageType) {
			this.messageType = messageType;
			return this;
		}
		
	}
}
