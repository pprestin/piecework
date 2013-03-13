/*
 * Copyright 2011 University of Washington
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

import java.io.Serializable;


/**
 * Behind the scenes abstraction of an entity (e.g. user or group)
 * 
 * @author James Renfro
 */
public abstract class EntityReference implements Serializable {

	private static final long serialVersionUID = -5879385080201107668L;

	public enum EntityType { USER, GROUP, SYSTEM, SYSTEM_CREDENTIAL, NONE };
	
	protected final String id;
	protected final EntityType type;
	
	@SuppressWarnings("unused")
	private EntityReference() { 
		this(EntityType.NONE, null);
	}
	
	public EntityReference(EntityType type, String id) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public EntityType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityReference other = (EntityReference) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
}
