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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Field.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Field.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field implements Serializable, Comparable<Field> {

	private static final long serialVersionUID = -8642937056889667692L;

	@XmlAttribute
    @XmlID
    @Id
    private final String fieldId;

    @XmlElement
    private final String label;

    @XmlElement
    private final String header;

	@XmlElement
    private final String name;
    
	@XmlElement
    private final String type;

    @XmlElement
    private final String mask;

    @XmlAttribute
    private final String accept;

    @XmlElement
    private final String pattern;

    @XmlElement
    private final String customValidity;
    
	@XmlAttribute
    private final boolean editable;

    @XmlAttribute
    private final boolean readonly;
	
    @XmlAttribute
    private final boolean required;
    
    @XmlAttribute
    private final boolean restricted;

    @XmlAttribute
    private final boolean visible;

    @XmlElement
    private final String defaultValue;

    @XmlElement
    private final String placeholder;
        
    @XmlAttribute
    private final int displayValueLength;
    
    @XmlAttribute
    private final int maxValueLength;
    
    @XmlAttribute
    private final int minValueLength;
    
    @XmlAttribute
    private final int maxInputs;

    @XmlAttribute
    private final int minInputs;

    @XmlElementWrapper(name="constraints")
    @XmlElementRef
    private final List<Constraint> constraints;
    
    @XmlElementWrapper(name="options")
    @XmlElementRef
    private final List<Option> options;

    private final Map<String, String> metadataTemplates;
    
    @XmlAttribute
    private final int ordinal;

    @XmlAttribute
    private final String link;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private Field() {
        this(new Field.Builder(), new ViewContext());
    }

    private Field(Field.Builder builder, ViewContext context) {
        this.fieldId = builder.fieldId;
        this.label = builder.label;
        this.header = builder.header;
        this.name = builder.name;
        this.type = builder.type;
        this.editable = builder.editable;
        this.readonly = builder.readonly;
        this.required = builder.required;
        this.restricted = builder.restricted;
        this.mask = builder.mask;
        this.accept = builder.accept;
        this.pattern = builder.pattern;
        this.customValidity = builder.customValidity;
        this.visible = builder.visible;
        this.defaultValue = builder.defaultValue;
        this.placeholder = builder.placeholder;
        this.ordinal = builder.ordinal;
        this.displayValueLength = builder.displayValueLength;
        this.maxValueLength = builder.maxValueLength;
        this.minValueLength = builder.minValueLength;
        this.maxInputs = builder.maxInputs;
        this.minInputs = builder.minInputs;
        this.isDeleted = builder.isDeleted;
        this.constraints = builder.constraints != null ? Collections.unmodifiableList(builder.constraints) : null;
        this.options = builder.options != null ? Collections.unmodifiableList(builder.options) : null;
        this.link = context != null && StringUtils.isNotEmpty(builder.processInstanceId) ? context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.processInstanceId, "value", builder.name) : null;
        this.metadataTemplates = builder.metadataTemplates != null ? Collections.unmodifiableMap(builder.metadataTemplates) : null;
    }

    public String getFieldId() {
		return fieldId;
	}

    public String getLabel() {
        return label;
    }

    public String getHeader() {
        return header;
    }

    public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

    public boolean isRestricted() {
        return restricted;
    }

    public boolean isRequired() {
		return required;
	}

	public boolean isEditable() {
		return editable;
	}

    public boolean isReadonly() {
        return readonly;
    }

    public String getAccept() {
        return accept;
    }

    public String getMask() {
        return mask;
    }

    public String getPattern() {
        return pattern;
    }

    public String getCustomValidity() {
        return customValidity;
    }

    public List<Option> getOptions() {
		return options;
	}

    @JsonIgnore
    public Map<String, String> getOptionMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (options != null) {
            for (Option option : options) {
                if (StringUtils.isNotEmpty(option.getName()) && StringUtils.isNotEmpty(option.getValue()))
                    map.put(option.getName(), option.getValue());
            }
        }

        return map;
    }

	public String getDefaultValue() {
		return defaultValue;
	}

    public String getPlaceholder() {
        return placeholder;
    }

    public int getOrdinal() {
		return ordinal;
	}

	public int getDisplayValueLength() {
		return displayValueLength;
	}

	public int getMaxValueLength() {
		return maxValueLength;
	}

	public int getMinValueLength() {
		return minValueLength;
	}

	public int getMaxInputs() {
		return maxInputs;
	}

	public int getMinInputs() {
		return minInputs;
	}

    public List<Constraint> getConstraints() {
		return constraints;
	}

	public String getLink() {
		return link;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean equals(Object o) {
        Field other = Field.class.cast(o);
        return fieldId != null && other.fieldId != null ? other.fieldId.equals(fieldId) : false;
    }

    @Override
    public int hashCode() {
        return fieldId != null ? fieldId.hashCode() : 0;
    }

    public Map<String, String> getMetadataTemplates() {
        return metadataTemplates;
    }

    @Override
    public int compareTo(Field o) {
        int result = (ordinal < o.ordinal) ? -1 : ((ordinal == o.ordinal) ? 0 : 1);

        if (result == 0 && fieldId != null && o.fieldId != null)
            result = fieldId.compareTo(o.fieldId);

        if (result == 0 && name != null && o.name != null)
            result = name.compareTo(o.name);

        return result;
    }

    public final static class Builder {

    	private String fieldId;
    	private String processDefinitionKey;
        private String processInstanceId;
        private String label;
        private String header;
        private String name;
        private String type;
        private boolean editable;
        private boolean readonly;
        private boolean required;
        private boolean restricted;
        private boolean visible;
        private String accept;
        private String mask;
        private String pattern;
        private String customValidity;
        private String defaultValue;
        private String placeholder;
        private int displayValueLength;
        private int maxValueLength;
        private int minValueLength;
        private int maxInputs;
        private int minInputs;
        private List<Constraint> constraints;
        private List<Option> options;
        private Map<String, String> metadataTemplates;
        private int ordinal;
        private boolean isDeleted;

        public Builder() {
            super();
            this.fieldId = UUID.randomUUID().toString();
            this.displayValueLength = 40;
            this.maxInputs = 1;
            this.minInputs = 1;
            this.minValueLength = 0;
            this.maxValueLength = 255;
            this.ordinal = -1;
            this.editable = false;
            this.readonly = false;
            this.constraints = new ArrayList<Constraint>();
            this.options = new ArrayList<Option>();
            this.visible = true;
        }

        public Builder(Field field, Sanitizer sanitizer) {
            this.fieldId = field.fieldId != null ? sanitizer.sanitize(field.fieldId) : UUID.randomUUID().toString();
            this.label = sanitizer.sanitize(field.label);
            this.header = sanitizer.sanitize(field.header);
            this.name = sanitizer.sanitize(field.name);
            this.type = sanitizer.sanitize(field.type);
            this.editable = field.editable;
            this.readonly = field.readonly;
            this.required = field.required;
            this.restricted = field.restricted;
            this.accept = sanitizer.sanitize(field.accept);
            this.mask = field.mask;
            this.pattern = field.pattern;
            this.customValidity = field.customValidity;
            if (this.type != null && this.type.equals(piecework.Constants.FieldTypes.HTML))
                this.defaultValue = field.defaultValue;
            else
                this.defaultValue = sanitizer.sanitize(field.defaultValue);
            this.placeholder = sanitizer.sanitize(field.placeholder);
            this.displayValueLength = field.displayValueLength;
            this.maxValueLength = field.maxValueLength;
            this.minValueLength = field.minValueLength;
            this.maxInputs = field.maxInputs;
            this.minInputs = field.minInputs;
            this.ordinal = field.ordinal;
            this.isDeleted = field.isDeleted;
            this.visible = field.visible;

            if (field.constraints != null && !field.constraints.isEmpty()) {
            	this.constraints = new ArrayList<Constraint>(field.constraints.size());
            	for (Constraint constraint : field.constraints) {
            		this.constraints.add(new Constraint.Builder(constraint, sanitizer).build());
            	}
            } else {
                this.constraints = new ArrayList<Constraint>();
            }
            
            if (field.options != null && !field.options.isEmpty()) {
            	this.options = new ArrayList<Option>(field.options.size());
            	for (Option option : field.options) {
            		this.options.add(new Option.Builder(option, sanitizer).build());
            	}
            } else {
                this.options = new ArrayList<Option>();
            }

            if (field.metadataTemplates != null && !field.metadataTemplates.isEmpty())
                this.metadataTemplates = new HashMap<String, String>(field.metadataTemplates);
        }

        public Field build() {
            return new Field(this, null);
        }

        public Field build(ViewContext context) {
            return new Field(this, context);
        }

        public Builder fieldId(String fieldId) {
            this.fieldId = fieldId;
            return this;
        }
        
        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder header(String header) {
            this.header = header;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder editable() {
            this.editable = true;
            return this;
        }

        public Builder editable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public Builder readonly() {
            this.readonly = true;
            return this;
        }

        public Builder uneditable() {
            this.editable = false;
            return this;
        }
        
        public Builder required() {
            this.required = true;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder restricted() {
            this.restricted = true;
            return this;
        }

        public Builder accept(String accept) {
            this.accept = accept;
            return this;
        }

        public Builder mask(String mask) {
            this.mask = mask;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder customValidity(String customValidity) {
            this.customValidity = customValidity;
            return this;
        }
        
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder displayValueLength(int displayValueLength) {
            this.displayValueLength = displayValueLength;
            return this;
        }
        
        public Builder maxValueLength(int maxValueLength) {
            this.maxValueLength = maxValueLength;
            return this;
        }
        
        public Builder minValueLength(int minValueLength) {
            this.minValueLength = minValueLength;
            return this;
        }
        
        public Builder maxInputs(int maxInputs) {
            this.maxInputs = maxInputs;
            return this;
        }
        
        public Builder minInputs(int minInputs) {
            this.minInputs = minInputs;
            return this;
        }

        public Builder constraint(Constraint constraint) {
        	if (this.constraints == null)
        		this.constraints = new ArrayList<Constraint>();
        	this.constraints.add(constraint);
            return this;
        }

        public Builder option(Option option) {
            if (this.options == null)
                this.options = new ArrayList<Option>();
            this.options.add(option);
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

        public Builder visible() {
            this.visible = true;
            return this;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public Builder invisible() {
            this.visible = false;
            return this;
        }

        public Builder metadataTemplate(String name, String value) {
            if (this.metadataTemplates == null)
                this.metadataTemplates = new HashMap<String, String>();
            this.metadataTemplates.put(name, value);
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Field";
        public static final String ROOT_ELEMENT_NAME = "field";
        public static final String TYPE_NAME = "FieldType";
    }
	
}
