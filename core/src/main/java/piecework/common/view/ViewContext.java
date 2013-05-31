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

	private final String baseApplicationUri;
	private final String baseServiceUri;
	private final String version;
	private final String path;
	private final String label;
	
	public ViewContext() {
		this(null, null, null, null, null);
	}
	
	public ViewContext(String baseUri, String baseServiceUri, String version, String path, String label) {
		this.baseApplicationUri = baseUri;
		this.baseServiceUri = baseServiceUri;
		this.version = version;
		this.path = path;
		this.label = label;
	}

	public String getBaseApplicationUri() {
		return baseApplicationUri;
	}

	public String getBaseServiceUri() {
		return baseServiceUri;
	}

	public String getVersion() {
		return version;
	}

	public String getPath() {
		return path;
	}
	
	public String getLabel() {
		return label;
	}

	public String getApplicationUri(String ... ids) {

		return buildUri(false, baseApplicationUri, ids);
	}
	
	public String getServiceUri(String ... ids) {

		return buildUri(true, baseServiceUri, ids);
	}
	
	protected String buildUri(boolean includeVersion, String base, String ... ids) {
		StringBuilder builder = new StringBuilder();
		
		if (path != null) {
			builder.append(base).append("/");
            if (includeVersion && version != null)
                builder.append(version).append("/");
            builder.append(path);
			
			if (ids != null && ids.length > 0) {
				for (String id : ids) 
					builder.append("/").append(id);
			}
			
			return builder.toString();
		}
		
		return null;
	}
	
}
