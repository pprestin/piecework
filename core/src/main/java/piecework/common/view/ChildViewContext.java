/*
 * Copyright 2013 University of Washington
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
public class ChildViewContext extends ViewContext {

	private final ViewContext parent;
	private final String path;
	private final String label;
	
	public ChildViewContext() {
		this(null, null, null);
	}
	
	public ChildViewContext(ViewContext parent, String path, String label) {
		this.parent = parent;
		this.path = path;
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
		
	@Override
	protected String buildUri(boolean includeVersion, String base, String ... ids) {
		StringBuilder builder = new StringBuilder();
		
		if (parent != null && path != null && ids != null && ids.length > 0) {
			builder.append(parent.getServiceUri(ids[0])).append("/")
			.append(path);
			
			if (ids.length > 1)
				builder.append("/").append(ids[1]);
			
			return builder.toString();
		}
		
		return null;
	}
	
}
