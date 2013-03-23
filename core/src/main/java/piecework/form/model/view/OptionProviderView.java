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
package piecework.form.model.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import piecework.form.model.Endpoint;
import piecework.form.model.OptionProvider;
import piecework.form.model.builder.EndpointBuilder;
import piecework.form.model.builder.OptionProviderBuilder;

/**
 * @author James Renfro
 */
@XmlRootElement(name = OptionProviderView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = OptionProviderView.Constants.TYPE_NAME)
public class OptionProviderView implements OptionProvider<EndpointView> {

	@XmlAttribute(name = OptionProviderView.Attributes.ID)
	@XmlID
	private final String id;
	
	@XmlAttribute(name = OptionProviderView.Attributes.STORE_LOCAL)
	private final String storeLocal;
	
	@XmlElement(name = OptionProviderView.Elements.LABEL_EXPRESSION)
	private final String labelExpression;
	
	@XmlElement(name = OptionProviderView.Elements.VALUE_EXPRESSION)
	private final String valueExpression;
	
	@XmlElement(name = OptionProviderView.Elements.ENDPOINT)
	private final EndpointView endpoint;
	
	@XmlAttribute(name = OptionProviderView.Attributes.DISABLED)
	private final String disabled;
	
	private OptionProviderView() {
		this(new OptionProviderView.Builder());
	}
	
	private OptionProviderView(OptionProviderView.Builder builder) {
		this.id = builder.getId();
		this.labelExpression = builder.getLabelExpression();
		this.valueExpression = builder.getValueExpression();
		this.storeLocal = Boolean.toString(builder.isStoreLocal());
		this.endpoint = builder.buildEndpoint(builder.getEndpoint());
		this.disabled = Boolean.toString(builder.isDisabled());
	}
	
	public String getId() {
		return id;
	}

	public String getLabelExpression() {
		return labelExpression;
	}

	public String getValueExpression() {
		return valueExpression;
	}

	public String getStoreLocal() {
		return storeLocal;
	}

	public EndpointView getEndpoint() {
		return endpoint;
	}

	public String getDisabled() {
		return disabled;
	}

	static class Attributes {
		final static String DISABLED = "disabled";
		final static String ID = "id";
		final static String STORE_LOCAL = "storeLocal";
	}
	
	static class Constants {
		public static final String ROOT_ELEMENT_NAME = "optionProvider";
		public static final String TYPE_NAME = "OptionProviderType";
	}

	static class Elements {
		final static String ENDPOINT = "endpoint";
		final static String LABEL_EXPRESSION = "labelExpression";
		final static String VALUE_EXPRESSION = "valueExpression";
	}
	
	public final static class Builder extends OptionProviderBuilder<OptionProviderView> {

		public Builder() {
			super();
		}
		
		public Builder(OptionProvider<?> provider) {
			super(provider);
		}
		
		@Override
		public OptionProviderView build() {
			return new OptionProviderView(this);
		}

		@Override
		public EndpointBuilder<?> endpointBuilder(Endpoint endpoint) {
			return new EndpointView.Builder(endpoint);
		}
		
	}
}
