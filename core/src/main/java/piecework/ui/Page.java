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
package piecework.ui;

import piecework.common.view.UserView;



/**
 * @author James Renfro
 */
public class Page {

	private final String applicationName;
	private final String pageName;
	private final String urlbase;
	private final Object resource;
	private final String json;
	private final UserView user;
	
	public Page(String applicationName, String pageName, String urlbase, Object resource, String json, UserView user) {
		this.applicationName = applicationName;
		this.pageName = pageName;
		this.urlbase = urlbase;
		this.resource = resource;
		this.json = json;
		this.user = user;
	}

	public Object getResource() {
		return resource;
	}

	public String getJson() {
		return json;
	}

	public UserView getUser() {
		return user;
	}

	public String getPageName() {
		return pageName;
	}

	public String getApplicationName() {
		return applicationName;
	}
	
	public String getStatic() {
		return urlbase;
	}
	
}
