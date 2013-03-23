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
package piecework.form.model.builder;

import piecework.form.model.Endpoint;

/**
 * @author James Renfro
 */
public abstract class EndpointBuilder<E extends Endpoint> extends Builder {

	private String url;
	private String method;
	private String mediaType;
	
	public EndpointBuilder() {
		super();
	}
	
	public EndpointBuilder(String id) {
		super(id);
	}
	
	public EndpointBuilder(Endpoint contract) {
		super(contract.getId());
		this.url = contract.getUrl();
		this.method = contract.getMethod();
		this.mediaType = contract.getMediaType();
	}
	
	public abstract E build();
	
	public EndpointBuilder<E> url(String url) {
		this.url = url;
		return this;
	}
	
	public EndpointBuilder<E> method(String method) {
		this.method = method;
		return this;
	}
	
	public EndpointBuilder<E> mediaType(String mediaType) {
		this.mediaType = mediaType;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public String getMethod() {
		return method;
	}

	public String getMediaType() {
		return mediaType;
	}
}
