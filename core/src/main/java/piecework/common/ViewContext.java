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
package piecework.common;

/**
 * @author James Renfro
 */
public class ViewContext {

	private final String baseApplicationUri;
	private final String baseServiceUri;
	private final String version;
	
	public ViewContext() {
		this(null, null, null);
	}
	
	public ViewContext(String baseUri, String baseServiceUri, String version) {
		this.baseApplicationUri = baseUri;
		this.baseServiceUri = baseServiceUri;
		this.version = version;
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

	public String getApplicationUri(String ... fragments) {

		return buildUri(false, baseApplicationUri, fragments);
	}
	
	public String getServiceUri(String ... fragments) {

		return buildUri(true, baseServiceUri, fragments);
	}
	
	protected String buildUri(boolean includeVersion, String base, String ... fragments) {

		if (base != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(base);
            if (includeVersion && version != null)
                builder.append("/").append(version);

			if (fragments != null && fragments.length > 0) {
				for (String id : fragments) {
                    if (id == null)
                        continue;
					builder.append("/").append(id);
                }
			}
			
			return builder.toString();
		}
		
		return null;
	}
	
}
