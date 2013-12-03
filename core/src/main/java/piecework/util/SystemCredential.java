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

import java.io.Serializable;

/**
 * @author James Renfro
 */
@Deprecated
public final class SystemCredential extends EntityReference implements Serializable {

	private static final long serialVersionUID = 8699193188231430851L;
	
	private final String subject;
	private final String issuer;
	
	public SystemCredential(String subject, String issuer) {
		super(EntityType.SYSTEM_CREDENTIAL, null);
		this.subject = subject;
		this.issuer = issuer;
	}

	public String getSubject() {
		return subject;
	}

	public String getIssuer() {
		return issuer;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SystemCredential[issuer=").append(issuer)
				.append(",subject=").append(subject).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	/**
	 * Note that this does a case-insenstive check of the issuer and subject common names, as per RFC 5280
	 * @see http://tools.ietf.org/html/rfc5280
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SystemCredential other = (SystemCredential) obj;
		if (issuer == null) {
			if (other.issuer != null)
				return false;
		} else if (!issuer.equalsIgnoreCase(other.issuer))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equalsIgnoreCase(other.subject))
			return false;
		return true;
	}
	
}
