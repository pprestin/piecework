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
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Button.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Button.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Button implements Serializable {

	private static final long serialVersionUID = 2804904972632404820L;

	@XmlAttribute
    @XmlID
    @Id
    private final String buttonId;
	
	@XmlElement
    private final String label;
	
	@XmlElement
    private final String tooltip;
	
	@XmlElement
    private final String type;
	
	@XmlElement
    private final String value;
	
	@XmlElement
    private final String link;

	@XmlAttribute
    private final int ordinal;
	
    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private Button() {
        this(new Button.Builder(), new ViewContext());
    }

    private Button(Button.Builder builder, ViewContext context) {
        this.buttonId = builder.buttonId;
        this.label = builder.label;
        this.tooltip = builder.tooltip;
        this.type = builder.type;
        this.value = builder.value;
        this.ordinal = builder.ordinal;
        this.isDeleted = builder.isDeleted;
        this.link = builder.link;
    }
    
    public String getButtonId() {
		return buttonId;
	}

	public String getLabel() {
		return label;
	}

	public String getTooltip() {
		return tooltip;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getLink() {
		return link;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

    	private String buttonId;
    	private String processDefinitionKey;
        private String label;
        private String tooltip;
        private String type;
        private String value;
        private String link;
        private int ordinal;
        private boolean isDeleted;

        public Builder() {
            super();
            this.buttonId = UUID.randomUUID().toString();
        }

        public Builder(Button button, Sanitizer sanitizer) {
            this.buttonId = button.buttonId != null ? sanitizer.sanitize(button.buttonId) : UUID.randomUUID().toString();
            this.label = sanitizer.sanitize(button.label);
            this.tooltip = sanitizer.sanitize(button.tooltip);
            this.type = sanitizer.sanitize(button.type);
            this.value = sanitizer.sanitize(button.value);
            this.ordinal = button.ordinal;
            this.isDeleted = button.isDeleted;
        }

        public Button build() {
            return new Button(this, null);
        }

        public Button build(ViewContext context) {
            return new Button(this, context);
        }

        public Builder buttonId(String buttonId) {
            this.buttonId = buttonId;
            return this;
        }
        
        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }
        
        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder tooltip(String tooltip) {
            this.tooltip = tooltip;
            return this;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder link(String link) {
            this.link = link;
            return this;
        }
        
        public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }
        
        public Builder delete() {
            this.isDeleted = true;
            return this;
        }

        public Builder undelete() {
            this.isDeleted = false;
            return this;
        }
    }
    
	public static class Constants {
        public static final String RESOURCE_LABEL = "Button";
        public static final String ROOT_ELEMENT_NAME = "button";
        public static final String TYPE_NAME = "ButtonType";
    }
	
}
