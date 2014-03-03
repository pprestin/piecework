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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @XmlElementWrapper(name="messages")
    @XmlElementRef
    private final List<Message> messages;

    @XmlElementWrapper(name="messages")
	@XmlElement(name="value")
	private final List<Value> values;

    @XmlAttribute
    private final String link;

	private FormValue() {
		this(new FormValue.Builder(), new ViewContext());
	}

	private FormValue(FormValue.Builder builder, ViewContext context) {
		this.name = builder.name;
		this.values = Collections.unmodifiableList(builder.values);
        this.messages = Collections.unmodifiableList(builder.messages);
        this.link = context != null && builder.processDefinitionKey != null && builder.formInstanceId != null && builder.name != null ? context.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.formInstanceId, Constants.ROOT_ELEMENT_NAME, builder.name) : null;
    }
	
	public String getName() {
		return name;
	}

	public List<Value> getValues() {
		return values;
	}

    public List<Message> getMessages() {
        return messages;
    }

    public String getLink() {
        return link;
    }

    public final static class Builder {

		private String name;
        private String processDefinitionKey;
        private String formInstanceId;
		private List<Value> values;
        private List<Message> messages;

		public Builder() {
			super();
            this.messages = new ArrayList<Message>();
            this.values = new ArrayList<Value>();
		}

		public Builder(FormValue formValue, Sanitizer sanitizer) {
			this.name = sanitizer.sanitize(formValue.name);

			if (formValue.values != null && !formValue.values.isEmpty()) {
				this.values = new ArrayList<Value>(formValue.values.size());
				for (Value value : formValue.values) {
					this.values.add(value);
				}
			} else {
                this.values = new ArrayList<Value>();
            }

            if (formValue.messages != null && !formValue.messages.isEmpty()) {
                this.messages = new ArrayList<Message>(formValue.messages.size());
                for (Message message : formValue.messages) {
                    this.messages.add(new Message.Builder(message, sanitizer).build());
                }
            } else {
                this.messages = new ArrayList<Message>();
            }
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

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder formInstanceId(String formInstanceId) {
            this.formInstanceId = formInstanceId;
            return this;
        }

        public Builder message(Message message) {
            if (this.messages == null)
                this.messages = new ArrayList<Message>();
            this.messages.add(message);
            return this;
        }

        public Builder values(List<Value> values) {
            if (values != null)
                this.values.addAll(values);
            return this;
        }

	}
	
	public static class Constants {
        public static final String RESOURCE_LABEL = "FormValue";
        public static final String ROOT_ELEMENT_NAME = "variable";
        public static final String TYPE_NAME = "FormValueType";
    }
	
}
