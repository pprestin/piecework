/*
 * Copyright 2010 University of Washington
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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * Abstract data transfer object.
 * 
 * @author James Renfro
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class View implements Serializable {
	
	private static final long serialVersionUID = 2547958887552746776L;
	
	@XmlTransient
	private final String serviceUri;
		
	@XmlAttribute(name=View.Attributes.ID)
	@XmlID
	protected final String id;
	
	@XmlAttribute(name = Attributes.URI)
	protected final String uri;

	@SuppressWarnings("unused")
	private View() {
		this(new View.Builder(), new ViewContext());
	}
	
	protected View(View.Builder builder, ViewContext context) {
		this.id = builder.id;
		this.uri = context.getUri(builder.id);
		this.serviceUri = context.getServiceUri(builder.id);
	}
	
	public String getId() {
		return this.id;
	}

	public String getServiceUri() {
		return this.serviceUri;
	}
	
	public String getUri() {
		return this.uri;
	}
	
	static class Attributes {
        final static String ID = "id";
        final static String URN = "urn";
        final static String URI = "link";
        final static String VERSION = "version";
        final static String NAMESPACE = "namespace";
        final static String CONTAINER = "container";
    }
	
	public static class Builder {
		protected String id;
		
		public Builder() {
		}
	
		public Builder id(String id) {
			this.id = id;
			return this;
		}
	}
}
