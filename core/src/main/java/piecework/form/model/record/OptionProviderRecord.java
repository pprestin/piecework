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
package piecework.form.model.record;

import piecework.form.model.Endpoint;
import piecework.form.model.OptionProvider;
import piecework.form.model.builder.EndpointBuilder;
import piecework.form.model.builder.OptionProviderBuilder;

/**
 * @author James Renfro
 */
public class OptionProviderRecord implements OptionProvider<EndpointRecord> {

	private String id;
	private String labelExpression;
	private String valueExpression;
	private boolean storeLocal;
	private boolean disabled;
	private EndpointRecord endpoint;
	
	private OptionProviderRecord() {
		
	}
	
	private OptionProviderRecord(OptionProviderRecord.Builder builder) {
		this.id = builder.getId();
		this.labelExpression = builder.getLabelExpression();
		this.valueExpression = builder.getValueExpression();
		this.storeLocal = builder.isStoreLocal();
		this.disabled = builder.isDisabled();
		this.endpoint = (EndpointRecord) (builder.getEndpoint() != null ? builder.getEndpoint().build() : null);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabelExpression() {
		return labelExpression;
	}

	public void setLabelExpression(String labelExpression) {
		this.labelExpression = labelExpression;
	}

	public String getValueExpression() {
		return valueExpression;
	}

	public void setValueExpression(String valueExpression) {
		this.valueExpression = valueExpression;
	}

	public boolean getStoreLocal() {
		return storeLocal;
	}
	public void setStoreLocal(boolean storeLocal) {
		this.storeLocal = storeLocal;
	}
	public boolean getDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public EndpointRecord getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(EndpointRecord endpoint) {
		this.endpoint = endpoint;
	}
	
	public final static class Builder extends OptionProviderBuilder<OptionProviderRecord> {

		public Builder() {
			super();
		}
		
		public Builder(OptionProvider<?> provider) {
			super(provider);
		}
		
		@Override
		public OptionProviderRecord build() {
			return new OptionProviderRecord(this);
		}

		@Override
		public EndpointBuilder<?> endpointBuilder(Endpoint endpoint) {
			return new EndpointRecord.Builder(endpoint);
		}
		
	}
}
