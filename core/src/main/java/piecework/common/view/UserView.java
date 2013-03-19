/*
 * Copyright 2010 University of Washington
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
package piecework.common.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import piecework.common.model.User;


/**
 * This is the bean that is used to generate xml and json representations of a person
 * in the system who is involved in a workflow. 
 * 
 * @author James Renfro
 * @since 1.0.2.1
 * @added 8/9/2010
 */
@XmlRootElement
public final class UserView extends View {

	private static final long serialVersionUID = -3377941728797595948L;

	@XmlAttribute(name = UserView.Attributes.VISIBLE_ID)
	private final String visibleId;
	
	@XmlElement(name = UserView.Elements.DISPLAY_NAME)
	private final String displayName;
	
	@XmlTransient
	private final String sortName;
	
	@XmlElement(name = UserView.Elements.FIRST_NAME)
	private final String firstName;
	
	@XmlElement(name = UserView.Elements.MIDDLE_NAME)
	private final String middleName;
	
	@XmlElement(name = UserView.Elements.LAST_NAME)
	private final String lastName;
	
	@XmlElement(name = UserView.Elements.EMAIL_ADDRESS)
	private final String emailAddress;
	
	@XmlTransient
	private final Map<String, String> identifiers;
	
	@XmlTransient
	private final boolean isFakeUser;
	
	@XmlElement(name = UserView.Elements.PROVIDER)
	private final String provider;
	
	private UserView() {
		this(new UserView.Builder(), new ViewContext());
	}
	
	private UserView(UserView.Builder builder, ViewContext context) {
		super(builder, context);
		this.visibleId = builder.visibleId;
		this.displayName = builder.displayName;
		this.sortName = builder.sortName;
		this.firstName = builder.firstName;
		this.middleName = builder.middleName;
		this.lastName = builder.lastName;
		this.emailAddress = builder.emailAddress;
		this.identifiers = builder.identifiers != null ? Collections.unmodifiableMap(builder.identifiers) : null;
		this.provider = builder.provider;
		this.isFakeUser = builder.isFakeUser;
	}

	public String getVisibleId() {
		return visibleId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public Map<String, String> getIdentifiers() {
		return identifiers;
	}

	public boolean isFakeUser() {
		return isFakeUser;
	}
	
	public String getProvider() {
		return provider;
	}

	public String getSortName() {
		return sortName;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder extends View.Builder {
		private String visibleId;
		private String displayName;
		private String sortName;
		private String firstName;
		private String middleName;
		private String lastName;
		private String emailAddress;
		private Map<String, String> identifiers;
		private boolean isFakeUser;
		private String provider;
				
		public Builder() {
			super();
		}
		
		public Builder(User user) {
			this();
			this.visibleId = user.getVisibleId();
			this.displayName = user.getDisplayName();
			this.sortName = user.getSortName();
			this.firstName = user.getFirstName();
			this.middleName = user.getMiddleName();
			this.lastName = user.getLastName();
			this.emailAddress = user.getEmailAddress();
			this.identifiers = user.getIdentifiers();
			this.isFakeUser = user.isFakeUser();
			this.provider = user.getProvider();
		}
		
		public UserView build(ViewContext context) {
			return new UserView(this, context);
		}
		
		public Builder visibleId(String visibleId) {
			this.visibleId = visibleId;
			return this;
		}
		
		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder sortName(String sortName) {
			this.sortName = sortName;
			return this;
		}
		
		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}
		
		public Builder middleName(String middleName) {
			this.middleName = middleName;
			return this;
		}
		
		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}
		
		public Builder emailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
			return this;
		}
		
		public Builder fakeUser(boolean isFakeUser) {
			this.isFakeUser = isFakeUser;
			return this;
		}
		
		public Builder identifier(String name, String value) {
			if (name != null && value != null) {
				if (this.identifiers == null) 
					this.identifiers = new HashMap<String, String>();
				
				this.identifiers.put(name, value);
			}
			return this;
		}
		
		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}
		
//		@Override
//		public String getUri() {
//			StringBuilder builder = new StringBuilder();
//			
//			if (serviceUri != null && version != null) {
//				builder.append(serviceUri).append("/")
//					.append(version).append("/")
//					.append(resource);
//				
//				if (id != null) {
//					builder.append("/").append(id);
//				}
//				
//				return builder.toString();
//			}
//			
//			return null;
//		}
	}

	static class Attributes {
		static final String ID = "id";
		static final String URI = "link";
		static final String VISIBLE_ID = "visibleId";
	}
	
	public static class Constants {
		public static final String ROOT_ELEMENT_NAME = "user";
		public static final String TYPE_NAME = "UserType";
		public static final String RESOURCE_LABEL = "User";
	}
	
	static class Elements {
		static final String DISPLAY_NAME = "name";
		static final String FIRST_NAME = "firstName";
		static final String MIDDLE_NAME = "middleName";
		static final String LAST_NAME = "lastName";
		static final String EMAIL_ADDRESS = "emailAddress";
		static final String PROVIDER = "provider";
	}
	
}
