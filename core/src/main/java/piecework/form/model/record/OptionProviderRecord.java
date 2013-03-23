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

/**
 * @author James Renfro
 */
public class OptionProviderRecord implements OptionProvider<EndpointRecord> {

	private String id;
	private String labelExpression;
	private String valueExpression;
	private String storeLocal;
	private String disabled;
	private EndpointRecord endpoint;
	
	public OptionProviderRecord() {
		
	}
	
	public OptionProviderRecord(OptionProvider<? extends Endpoint> contract) {
		this.id = contract.getId();
		this.labelExpression = contract.getLabelExpression();
		this.valueExpression = contract.getValueExpression();
		this.storeLocal = contract.getStoreLocal();
		this.disabled = contract.getDisabled();
		this.endpoint = new EndpointRecord(contract.getEndpoint());
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

	public String getStoreLocal() {
		return storeLocal;
	}
	public void setStoreLocal(String storeLocal) {
		this.storeLocal = storeLocal;
	}
	public String getDisabled() {
		return disabled;
	}
	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}
	public EndpointRecord getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(EndpointRecord endpoint) {
		this.endpoint = endpoint;
	}
	

}
