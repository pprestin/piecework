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

import java.util.HashMap;
import java.util.Map;

import piecework.common.model.User;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
public abstract class UserBuilder<U extends User> extends Builder {
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
			
	public UserBuilder() {
		super();
	}
	
	public UserBuilder(User user) {
		super(user.getId());
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
	
	public abstract U build(ViewContext context);
		
	public UserBuilder<U> id(String id) {
		super.id(id);
		return this;
	}
	
	public UserBuilder<U> visibleId(String visibleId) {
		this.visibleId = visibleId;
		return this;
	}
	
	public UserBuilder<U> displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public UserBuilder<U> sortName(String sortName) {
		this.sortName = sortName;
		return this;
	}
	
	public UserBuilder<U> firstName(String firstName) {
		this.firstName = firstName;
		return this;
	}
	
	public UserBuilder<U> middleName(String middleName) {
		this.middleName = middleName;
		return this;
	}
	
	public UserBuilder<U> lastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
	
	public UserBuilder<U> emailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
		return this;
	}
	
	public UserBuilder<U> fakeUser(boolean isFakeUser) {
		this.isFakeUser = isFakeUser;
		return this;
	}
	
	public UserBuilder<U> identifier(String name, String value) {
		if (name != null && value != null) {
			if (this.identifiers == null) 
				this.identifiers = new HashMap<String, String>();
			
			this.identifiers.put(name, value);
		}
		return this;
	}
	
	public UserBuilder<U> provider(String provider) {
		this.provider = provider;
		return this;
	}

	public String getVisibleId() {
		return visibleId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getSortName() {
		return sortName;
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
	
//	@Override
//	public String getUri() {
//		StringBuilder builder = new StringBuilder();
//		
//		if (serviceUri != null && version != null) {
//			builder.append(serviceUri).append("/")
//				.append(version).append("/")
//				.append(resource);
//			
//			if (id != null) {
//				builder.append("/").append(id);
//			}
//			
//			return builder.toString();
//		}
//		
//		return null;
//	}
	

}
