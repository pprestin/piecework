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

import piecework.common.model.User;

/**
 * @author James Renfro
 */
public class PageContext {

	private final String applicationTitle;
	private final String pageTitle;
	private final String assetsUrl;
	private final Object resource;
	private final User user;
	
	public PageContext(Builder builder) {
		this.applicationTitle = builder.applicationTitle;
		this.pageTitle = builder.pageTitle;
		this.assetsUrl = builder.assetsUrl;
		this.resource = builder.resource;
		this.user = builder.user;
	}

	public Object getResource() {
		return resource;
	}

	public User getUser() {
		return user;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public String getApplicationTitle() {
		return applicationTitle;
	}
	
	public String getStatic() {
		return assetsUrl;
	}


    public final static class Builder {
        private String applicationTitle;
        private String pageTitle;
        private String assetsUrl;
        private Object resource;
        private User user;

        public Builder() {

        }

        public PageContext build() {
            return new PageContext(this);
        }

        public Builder applicationTitle(String applicationTitle) {
            this.applicationTitle = applicationTitle;
            return this;
        }

        public Builder pageTitle(String pageTitle) {
            this.pageTitle = pageTitle;
            return this;
        }

        public Builder assetsUrl(String assetsUrl) {
            this.assetsUrl = assetsUrl;
            return this;
        }

        public Builder resource(Object resource) {
            this.resource = resource;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

    }


}
