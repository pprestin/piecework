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
import piecework.form.model.OptionProvider;

/**
 * @author James Renfro
 */
public abstract class OptionProviderBuilder<P extends OptionProvider<?>> extends Builder {

	private String labelExpression;
	private String valueExpression;
	private boolean disabled;
	private boolean storeLocal;
	private EndpointBuilder<?> endpoint;
	
	public OptionProviderBuilder() {
		super();
	}
	
	public OptionProviderBuilder(String id) {
		super(id);
	}
	
	public OptionProviderBuilder(OptionProvider<?> provider) {
		super(provider.getId());
		this.labelExpression = provider.getLabelExpression();
		this.valueExpression = provider.getValueExpression();
		this.disabled = Boolean.valueOf(provider.getDisabled()).booleanValue();
		this.storeLocal = Boolean.valueOf(provider.getStoreLocal()).booleanValue();
		this.endpoint = endpointBuilder(provider.getEndpoint());
	}
	
	public abstract P build();
	
	public abstract EndpointBuilder<?> endpointBuilder(Endpoint endpoint);
	
	public <E extends Endpoint> E buildEndpoint(EndpointBuilder<?> builder) {
		if (builder == null)
			return null;
		
		return (E)builder.build();
	}
	
	public OptionProviderBuilder<P> labelExpression(String labelExpression) {
		this.labelExpression = labelExpression;
		return this;
	}
	
	public OptionProviderBuilder<P> valueExpression(String valueExpression) {
		this.valueExpression = valueExpression;
		return this;
	}
	
	public OptionProviderBuilder<P> disable() {
		this.disabled = true;
		return this;
	}
	
	public OptionProviderBuilder<P> enable() {
		this.disabled = false;
		return this;
	}
	
	public OptionProviderBuilder<P> endpoint(EndpointBuilder<?> endpoint) {
		this.endpoint = endpoint;
		return this;
	}
	
//	public OptionProviderBuilder<P> endpoint(EndpointBuilder<?> endpoint) {
//		this.endpoint = endpointBuilder(endpoint);
//		return this;
//	}
	
	public OptionProviderBuilder<P> storeLocal(boolean storeLocal) {
		this.storeLocal = storeLocal;
		return this;
	}

	public String getLabelExpression() {
		return labelExpression;
	}

	public String getValueExpression() {
		return valueExpression;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public boolean isStoreLocal() {
		return storeLocal;
	}

	public EndpointBuilder<?> getEndpoint() {
		return endpoint;
	}
}
