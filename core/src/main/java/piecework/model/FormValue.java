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
package piecework.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.mongodb.core.mapping.DBRef;
import piecework.security.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormValue.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormValue.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormValue implements Serializable {

	private static final long serialVersionUID = 2672648053686796625L;

	@XmlElement
	private final String name;
	
	@XmlElement
	private final String value;

    @XmlElementWrapper(name="messages")
    @XmlElementRef
    private final List<Message> messages;
	
	@XmlTransient
	@JsonIgnore
    private final boolean restricted;

    @XmlTransient
    @JsonIgnore
    private final String contentType;

    @XmlTransient
    @JsonIgnore
    private final String location;

    @XmlElementWrapper(name="values")
	@XmlElement
	private final List<String> values;

    @XmlTransient
    @JsonIgnore
    @DBRef
    private final List<Secret> secrets;

	private FormValue() {
		this(new FormValue.Builder(), new ViewContext());
	}

	private FormValue(FormValue.Builder builder, ViewContext context) {
		this.name = builder.name;

		String temporarySingle = null;
		List<String> temporaryList = null;
		if (builder.values != null) {
			int size = builder.values.size();
			if (size == 1) 
				temporarySingle = builder.values.get(0);
			else
				temporaryList = Collections.unmodifiableList(builder.values);
		} 
		this.value = temporarySingle;
		this.values = temporaryList;
        this.secrets = builder.secrets;
		this.restricted = builder.restricted;
        this.contentType = builder.contentType;
        this.location = builder.location;
        this.messages = Collections.unmodifiableList(builder.messages);
    }
	
	public String getName() {
		return name;
	}

    @JsonIgnore
	public String getValue() {
		return value;
	}

	public List<String> getValues() {
		return values;
	}

    @JsonIgnore
	public List<String> getAllValues() {
		if (this.value != null)
			return Collections.singletonList(value);
		return this.values;
	}

	public boolean isRestricted() {
		return restricted;
	}

    public String getContentType() {
        return contentType;
    }

    public String getLocation() {
        return location;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<Secret> getSecrets() {
        return secrets;
    }

    public final static class Builder {

		private String name;
		private List<String> values;
        private List<Secret> secrets;
		private boolean restricted;
        private String contentType;
        private String location;
        private List<Message> messages;
		
		public Builder() {
			super();
            this.messages = new ArrayList<Message>();
            this.values = new ArrayList<String>();
		}

		public Builder(FormValue formValue, Sanitizer sanitizer) {
			this.name = sanitizer.sanitize(formValue.name);
			
			if (formValue.values != null && !formValue.values.isEmpty()) {
				this.values = new ArrayList<String>(formValue.values.size());
				for (String value : formValue.values) {
					this.values.add(sanitizer.sanitize(value));
				}
			} else {
                this.values = new ArrayList<String>();
            }

            if (formValue.value != null)
                this.values.add(formValue.value);

            if (formValue.messages != null && !formValue.messages.isEmpty()) {
                this.messages = new ArrayList<Message>(formValue.messages.size());
                for (Message message : formValue.messages) {
                    this.messages.add(new Message.Builder(message, sanitizer).build());
                }
            } else {
                this.messages = new ArrayList<Message>();
            }
			this.restricted = formValue.restricted;
		}

		public FormValue build() {
			return new FormValue(this, null);
		}

		public FormValue build(ViewContext context) {
			return new FormValue(this, context);
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}

        public Builder message(Message message) {
            if (this.messages == null)
                this.messages = new ArrayList<Message>();
            this.messages.add(message);
            return this;
        }

		public Builder value(String value) {
			if (this.values == null) 
				this.values = new ArrayList<String>();
			this.values.add(value);
			return this;
		}

        public Builder values(String ... values) {
            if (this.values == null)
                this.values = new ArrayList<String>();
            if (values != null)
                this.values.addAll(Arrays.asList(values));
            return this;
        }

        public Builder values(List<String> values) {
            if (this.values == null)
                this.values = new ArrayList<String>();
            if (values != null)
                this.values.addAll(values);
            return this;
        }
        
        public Builder restricted() {
        	this.restricted = true;
        	return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder secret(Secret secret) {
            if (this.secrets == null)
                this.secrets = new ArrayList<Secret>();
            this.secrets.add(secret);
            return this;
        }
	}
	
	public static class Constants {
        public static final String RESOURCE_LABEL = "FormValue";
        public static final String ROOT_ELEMENT_NAME = "formValue";
        public static final String TYPE_NAME = "FormValueType";
    }
	
}
