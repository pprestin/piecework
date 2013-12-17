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
package piecework.util;

import piecework.common.ManyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Behind the scenes abstraction of a user to bridge the gap between the
 * view model objects and the various persistence model objects that may
 * come back from identity provider systems. 
 * 
 * @author James Renfro
 */
@Deprecated
public final class UserReference extends EntityReference implements UserContract {

	public static final UserReference ANY_VALID_USER = new Builder("*").build();
	
	private static final long serialVersionUID = -8765121708709323338L;
	
	private final String visibleId;
	private final String displayName;
	private final String sortName;
	private final String firstName;
	private final String middleName;
	private final String lastName;
	private final String emailAddress;
	private final Map<String, String> identifiers;
	private final String provider;
	private final boolean isFakeUser;
	private final List<GroupReference> groups;
	private final Map<String, List<String>> qualifications;
	
	private UserReference() {
		this(new Builder());
	}
	
	private UserReference(Builder builder) {
		super(EntityType.USER, builder.id);
		this.visibleId = builder.visibleId;
		this.displayName = builder.displayName;
		this.sortName = builder.sortName;
		this.firstName = builder.firstName;
		this.middleName = builder.middleName;
		this.lastName = builder.lastName;
		this.emailAddress = builder.emailAddress;
		this.identifiers = builder.identifiers != null ? Collections.unmodifiableMap(builder.identifiers) : null;
		this.qualifications = builder.qualifications != null ? Collections.unmodifiableMap(builder.qualifications) : null;
		this.provider = builder.provider;
		this.isFakeUser = false;
		this.groups = builder.groups;
	}
	
	public String getPrimaryId() {
		return getId();
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

	public Map<String, List<String>> getQualifications() {
		return qualifications;
	}

	public String getProvider() {
		return provider;
	}
	
	public List<GroupReference> getGroups() {
		return groups;
	}

	public boolean isFakeUser() {
		return isFakeUser;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserReference [id=").append(id).append(", visibleId=")
				.append(visibleId).append(", displayName=").append(displayName)
				.append("]");
		return builder.toString();
	}
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		private String id;
		private String visibleId;
		private String displayName;
		private String sortName;
		private String firstName;
		private String middleName;
		private String lastName;
		private String emailAddress;
		private Map<String, String> identifiers;
		private String provider;
		private List<GroupReference> groups;
		private ManyMap<String, String> qualifications;
		
		public Builder() {
			
		}
		
		public Builder(String id) {
			this.id = id;
		}
		
		public Builder(UserContract contract) {
			this.id = contract.getId();
			this.visibleId = contract.getVisibleId();
			this.displayName = contract.getDisplayName();
			this.sortName = contract.getSortName();
			this.firstName = contract.getFirstName();
			this.middleName = contract.getMiddleName();
			this.lastName = contract.getLastName();
			this.emailAddress = contract.getEmailAddress();
			this.identifiers = contract.getIdentifiers();
			this.provider = contract.getProvider();
		}
		
		public UserReference build() {
			return new UserReference(this);
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
		
		public Builder group(GroupReference group) {
			if (group != null) {
				if (this.groups == null)
					this.groups = new ArrayList<GroupReference>();
				
				this.groups.add(group);
			}
			return this;
		}
		
		public Builder groups(List<GroupReference> groups) {
			if (groups != null && !groups.isEmpty()) {
				if (this.groups == null)
					this.groups = groups;
				else
					this.groups.addAll(groups);
			}
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
		
		public Builder qualification(String name, String value) {
			if (name != null && value != null) {
				if (this.qualifications == null) 
					this.qualifications = new ManyMap<String, String>();
				
				this.qualifications.putOne(name, value);
			}
			return this;
		}
		
		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}
		
	}
	
}
