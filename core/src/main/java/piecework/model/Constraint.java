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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.common.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Constraint.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Constraint.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Constraint.Constants.ROOT_ELEMENT_NAME)
public class Constraint implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
    @XmlID
    @Id
	private final String constraintId;

	@XmlElement
	private final String type;
	
	@XmlElement
    private final String name;
	
	@XmlElement
    private final String value;

    @XmlElementWrapper(name="and")
    @XmlElementRef
    private final List<Constraint> and;

    @XmlElementWrapper(name="or")
    @XmlElementRef
    private final List<Constraint> or;

//    @XmlElementWrapper(name="subconstraints")
//    @XmlElementRef
//    private final List<Constraint> subconstraints;
	
	@XmlAttribute
    private final int ordinal;
	
	@XmlElement
    private final String link;
	
	@XmlTransient
    @JsonIgnore
    private final boolean isDeleted;
	
	private Constraint() {
        this(new Constraint.Builder(), new ViewContext());
    }

    private Constraint(Constraint.Builder builder, ViewContext context) {
    	this.constraintId = builder.constraintId;
        this.name = builder.name;
        this.type = builder.type;
        this.value = builder.value;
        this.ordinal = builder.ordinal;
        this.isDeleted = builder.isDeleted;
        this.and = builder.and != null ? Collections.unmodifiableList(builder.and) : null;
        this.or = builder.or != null ? Collections.unmodifiableList(builder.or) : null;
//        this.subconstraints = builder.subconstraints != null ? Collections.unmodifiableList(builder.subconstraints) : null;
        this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.constraintId) : null;
    }
	
	public String getConstraintId() {
		return constraintId;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public String getLink() {
		return link;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

    public List<Constraint> getAnd() {
        return and;
    }

    public List<Constraint> getOr() {
        return or;
    }
//    public List<Constraint> getSubconstraints() {
//        return subconstraints;
//    }

    public final static class Builder {

    	private String constraintId;
    	private String processDefinitionKey;
    	private String type;
        private String name;
        private String value;
        private List<Constraint> and;
        private List<Constraint> or;
//        private List<Constraint> subconstraints;
        private int ordinal;
        private boolean isDeleted;

        public Builder() {
            super();
            this.constraintId = UUID.randomUUID().toString();
        }

        public Builder(Constraint constraint, Sanitizer sanitizer) {
            this.constraintId = constraint.constraintId != null ? sanitizer.sanitize(constraint.constraintId) : UUID.randomUUID().toString();
            this.name = sanitizer.sanitize(constraint.name);
            this.type = sanitizer.sanitize(constraint.type);
            this.value = sanitizer.sanitize(constraint.value);
            this.ordinal = constraint.ordinal;
            this.isDeleted = constraint.isDeleted;
            if (constraint.and != null && !constraint.and.isEmpty()) {
                this.and = new ArrayList<Constraint>(constraint.and.size());
                for (Constraint subconstraint : constraint.and) {
                    this.and.add(new Constraint.Builder(subconstraint, sanitizer).build());
                }
            }

            if (constraint.or != null && !constraint.or.isEmpty()) {
                this.or = new ArrayList<Constraint>(constraint.or.size());
                for (Constraint subconstraint : constraint.or) {
                    this.or.add(new Constraint.Builder(subconstraint, sanitizer).build());
                }
            }

//            if (constraint.subconstraints != null && !constraint.subconstraints.isEmpty()) {
//                this.subconstraints = new ArrayList<Constraint>(constraint.subconstraints.size());
//                for (Constraint subconstraint : constraint.subconstraints) {
//                    this.subconstraints.add(new Constraint.Builder(subconstraint, sanitizer).build());
//                }
//            }
        }

        public Constraint build() {
            return new Constraint(this, null);
        }

        public Constraint build(ViewContext context) {
            return new Constraint(this, context);
        }
        
        public Builder constraintId(String constraintId) {
            this.constraintId = constraintId;
            return this;
        }
        
        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
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
        
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder and(Constraint constraint) {
            if (this.and == null)
                this.and = new ArrayList<Constraint>();
            this.and.add(constraint);
            return this;
        }

        public Builder or(Constraint constraint) {
            if (this.or == null)
                this.or = new ArrayList<Constraint>();
            this.or.add(constraint);
            return this;
        }

//        public Builder subconstraint(Constraint subconstraint) {
//            if (this.subconstraints == null)
//                this.subconstraints = new ArrayList<Constraint>();
//            this.subconstraints.add(subconstraint);
//            return this;
//        }
        
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
        public static final String RESOURCE_LABEL = "Constraint";
        public static final String ROOT_ELEMENT_NAME = "constraint";
        public static final String TYPE_NAME = "ConstraintType";
    }
	
}
