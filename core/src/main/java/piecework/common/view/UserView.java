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
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import piecework.common.model.User;
import piecework.form.model.builder.UserBuilder;


/**
 * This is the bean that is used to generate xml and json representations of a person
 * in the system who is involved in a workflow. 
 * 
 * @author James Renfro
 */
@XmlRootElement
public final class UserView implements User {

	private static final long serialVersionUID = -3377941728797595948L;

	@XmlAttribute(name=UserView.Attributes.ID)
	@XmlID
	protected final String id;
	
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
	
	private UserView(UserBuilder<?> builder, ViewContext context) {
		this.id = builder.getId();
		this.visibleId = builder.getVisibleId();
		this.displayName = builder.getDisplayName();
		this.sortName = builder.getSortName();
		this.firstName = builder.getFirstName();
		this.middleName = builder.getMiddleName();
		this.lastName = builder.getLastName();
		this.emailAddress = builder.getEmailAddress();
		this.identifiers = buildIdentifiers(builder.getIdentifiers());
		this.provider = builder.getProvider();
		this.isFakeUser = builder.isFakeUser();
	}
	
	private static Map<String, String> buildIdentifiers(Map<String, String> identifiers) {
		return identifiers != null ? Collections.unmodifiableMap(identifiers) : null;
	}

	public String getId() {
		return id;
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
	public final static class Builder extends UserBuilder<UserView> {
		
		public Builder() {
			super();
		}
		
		public Builder(User user) {
			super(user);
		}

		@Override
		public UserView build(ViewContext context) {
			return new UserView(this, context);
		}
		
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
