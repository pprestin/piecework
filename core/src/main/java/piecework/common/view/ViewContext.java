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
package piecework.common.view;

/**
 * @author James Renfro
 */
public class ViewContext {

	private final String baseUri;
	private final String baseServiceUri;
	private final String version;
	private final String resource;
	
	public ViewContext() {
		this(null, null, null, null);
	}
	
	public ViewContext(String baseUri, String baseServiceUri, String version, String resource) {
		this.baseUri = baseUri;
		this.baseServiceUri = baseServiceUri;
		this.version = version;
		this.resource = resource;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public String getBaseServiceUri() {
		return baseServiceUri;
	}

	public String getVersion() {
		return version;
	}

	public String getResource() {
		return resource;
	}
	
	public String getUri(String id) {
		StringBuilder builder = new StringBuilder();
		
		if (version != null && resource != null) {
			builder.append(baseUri).append("/")
				.append(version).append("/")
				.append(resource);
			
			if (id != null)
				builder.append("/").append(id);
			
			return builder.toString();
		}
		
		return null;
	}
	
	public String getServiceUri(String id) {
		StringBuilder builder = new StringBuilder();
		
		if (version != null && resource != null) {
			builder.append(baseServiceUri).append("/")
				.append(version).append("/")
				.append(resource);
			
			if (id != null)
				builder.append("/").append(id);
			
			return builder.toString();
		}
		
		return null;
	}
}
