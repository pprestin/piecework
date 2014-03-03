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
import org.springframework.data.annotation.Id;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Option.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Option.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Option implements Serializable {

	private static final long serialVersionUID = 1368227956154386652L;

	@XmlAttribute
    @XmlID
    @Id
    private final String optionId;

    @XmlElement
    private final String name;

	@XmlElement
    private final String value;
    
	@XmlElement
    private final String label;
    
    @XmlAttribute
    private final boolean selected;
	
	private Option() {
        this(new Option.Builder(), new ViewContext());
    }

    private Option(Option.Builder builder, ViewContext context) {
        this.optionId = builder.optionId;
        this.name = builder.name;
        this.value = builder.value;
        this.label = builder.label;
        this.selected = builder.selected;
    }
	
	public String getOptionId() {
		return optionId;
	}

    public String getName() {
        return name;
    }

    public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public boolean isSelected() {
		return selected;
	}

	public final static class Builder {

    	private String optionId;
        private String name;
    	private String value;
        private String label;
        private boolean selected;

        public Builder() {
            super();
            this.optionId = UUID.randomUUID().toString();
        }
        
        public Builder(Option option, Sanitizer sanitizer) {
            this.optionId = option.optionId != null ? sanitizer.sanitize(option.optionId) : UUID.randomUUID().toString();
            this.name = sanitizer.sanitize(option.name);
            this.value = sanitizer.sanitize(option.value);
            this.label = sanitizer.sanitize(option.label);
            this.selected = option.selected;
        }
        
        public Option build() {
            return new Option(this, null);
        }

        public Option build(ViewContext context) {
            return new Option(this, context);
        }
        
        public Builder optionId(String optionId) {
            this.optionId = optionId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }
        
        public Builder label(String label) {
            this.label = label;
            return this;
        }
        
        public Builder selected() {
            this.selected = true;
            return this;
        }   
	}
	
	public static class Constants {
        public static final String RESOURCE_LABEL = "Option";
        public static final String ROOT_ELEMENT_NAME = "option";
        public static final String TYPE_NAME = "OptionType";
    }
}
