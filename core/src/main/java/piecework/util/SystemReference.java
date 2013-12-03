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
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

/**
 * Behind the scenes abstraction of an external system that has some relationship with
 * Restflow - e.g. producer of services, consumer of services
 * 
 * @author James Renfro
 */
@Deprecated
public final class SystemReference extends EntityReference implements Serializable {
	
	private static final long serialVersionUID = 7379301209689996131L;
	
	private final String name;
	private final List<SystemCredential> credentials;
	
	private SystemReference() {
		this(new Builder());
	}
	
	private SystemReference(Builder builder) {
		super(EntityType.SYSTEM, builder.id);
		this.name = builder.name;
		this.credentials = builder.credentials;
	}

	public String getName() {
		return name;
	}

	public List<SystemCredential> getCredentials() {
		return credentials;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SystemReference [name=").append(name)
				.append(", credentials=");
		
		if (credentials != null) {
			for (SystemCredential credential : credentials) {
				builder.append(credential.toString());
			}
		}
		
		builder.append("]");
		return builder.toString();
	}

	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		
		private static final String COMMON_NAME_PATTERN = "CN\\s*=\\s*([^,]*),";
		
		private String id;
		private String name;
		private List<SystemCredential> credentials;
		
		public Builder() {
			
		}
		
		public Builder(String id) {
			this.id = id;
		}
		
		public SystemReference build() {
			return new SystemReference(this);
		}
		
		public boolean hasCredentials() {
			return credentials != null && !credentials.isEmpty();
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder credential(String subject, String issuer) {
			if (credentials == null)
				credentials = new ArrayList<SystemCredential>();
			
			credentials.add(new SystemCredential(subject, issuer));
			return this;
		}
		
		public Builder credential(X509Certificate certificate) throws CertificateParsingException {
			String certificateIssuer = null;
			String certificateSubject = null;
			
			// Grab the certificate issuer common name in RFC2253 format
			X500Principal issuer = certificate.getIssuerX500Principal();
			String issuerPrincipalName = issuer.getName(X500Principal.RFC2253);
			certificateIssuer = getCommonName(issuerPrincipalName);

			Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();

			if (subjectAlternativeNames != null) {
				for (List<?> item : subjectAlternativeNames) {
					String rfc822Name = (String) item.get(1);
					certificateSubject = rfc822Name;
					break;
				}
			} else {
				X500Principal subject = certificate.getSubjectX500Principal();
				String subjectPrincipalName = subject.getName(X500Principal.RFC2253);
				certificateSubject = getCommonName(subjectPrincipalName);
			}
			
			if (certificateIssuer == null)
				throw new CertificateParsingException("Unable to determine certificate issuer");
			
			if (certificateSubject == null)
				throw new CertificateParsingException("Unable to determine certificate issuer");
			
			return credential(certificateSubject, certificateIssuer);
		}
		
		private static String getCommonName(String fullName) {
			if (fullName == null)
				return null;
			
			Pattern pattern = Pattern.compile(COMMON_NAME_PATTERN);
			Matcher matcher = pattern.matcher(fullName);
			boolean isFound = matcher.find();
			
			if (!isFound)
				return null;
			
			return matcher.group(1);
		}
	}
	
}
