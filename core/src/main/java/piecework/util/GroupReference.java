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

import java.util.ArrayList;
import java.util.List;

/**
 * Behind the scenes abstraction of a group to bridge the gap between the
 * view model objects and the various persistence model objects that may
 * come back from access provider systems. 
 * 
 * @author James Renfro
 */
public final class GroupReference extends EntityReference implements GroupContract<UserReference> {

	private static final long serialVersionUID = -4916081048504607164L;
	
	private final String name;
	private final String visibleId;
	private final String description;
	private final String component;
	private final String provider;
	private final List<UserReference> members;
	
	private GroupReference() {
		this(new Builder());
	}
	
	private GroupReference(Builder builder) {
		super(EntityType.GROUP, builder.id);
		this.name = builder.name;
		this.visibleId = builder.visibleId;
		this.description = builder.description;
		this.members = builder.members;
		this.provider = builder.provider;
		this.component = builder.component;	
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getComponent() {
		return component;
	}

	public String getProvider() {
		return provider;
	}

	public String getVisibleId() {
		return visibleId;
	}

	public List<UserReference> getMembers() {
		return members;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((provider == null) ? 0 : provider.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupReference other = (GroupReference) obj;
		if (provider == null) {
			if (other.provider != null)
				return false;
		} else if (!provider.equals(other.provider))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("GroupReference [id=").append(id).append(", type=")
				.append(type).append(", name=").append(name)
				.append(", visibleId=").append(visibleId)
				.append(", component=").append(component).append(", provider=")
				.append(provider).append("]");
		return builder2.toString();
	}

	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		private final String id;
		private String visibleId;
		private String provider;
		private String component;
		private String name;
		private String description;
		private List<UserReference> members;
		
		public Builder() {
			this((String)null);
		}
		
		public Builder(String id) {
			this.id = id;
		}
		
		public Builder(GroupContract<UserContract> group) {
			this.id = group.getId();
			this.visibleId = group.getVisibleId();
			this.provider = group.getProvider();
			this.component = group.getComponent();
			this.name = group.getName();
			this.description = group.getDescription();
			List<? extends UserContract> memberContracts = group.getMembers();
			if (memberContracts != null && !memberContracts.isEmpty()) {
				this.members = new ArrayList<UserReference>(memberContracts.size());
				for (UserContract memberContract : memberContracts) {
					this.members.add(new UserReference.Builder(memberContract).build());
				}
			}
		}
		
		public GroupReference build() {
			return new GroupReference(this);
		}
		
		public Builder visibleId(String visibleId) {
			this.visibleId = visibleId;
			return this;
		}
		
		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}

		public Builder component(String component) {
			this.component = component;
			return this;
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public Builder member(UserReference member) {
			if (this.members == null) 
				this.members = new ArrayList<UserReference>();
			this.members.add(member);
			return this;
		}
		
	}

}
