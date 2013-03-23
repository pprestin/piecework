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
package piecework.form.model.builder;

import java.util.ArrayList;
import java.util.List;

import piecework.form.model.Constraint;

/**
 * @author James Renfro
 */
public abstract class ConstraintBuilder<C extends Constraint> extends Builder {
	private String constraintTypeCode;
	private List<String> referencedPropertyNames;
	private String operator;
	private String value;
	private String effect;
	
	public ConstraintBuilder() {
		
	}
	
	public ConstraintBuilder(Constraint constraint) {
		this.constraintTypeCode = constraint.getConstraintTypeCode();
		this.referencedPropertyNames = constraint.getReferencedPropertyNames();
		this.operator = constraint.getOperator();
		this.value = constraint.getValue();
		this.effect = constraint.getEffect();
	}
	
	public abstract C build();
	
	public ConstraintBuilder<C> constraintTypeCode(String constraintTypeCode) {
		this.constraintTypeCode = constraintTypeCode;
		return this;
	}
	
	public ConstraintBuilder<C> referencedPropertyName(String referencedPropertyName) {
		if (this.referencedPropertyNames == null)
			this.referencedPropertyNames = new ArrayList<String>();
		this.referencedPropertyNames.add(referencedPropertyName);
		return this;
	}
	
	public ConstraintBuilder<C> referencedPropertyNames(List<String> referencedPropertyNames) {
		if (this.referencedPropertyNames == null)
			this.referencedPropertyNames = new ArrayList<String>();
		this.referencedPropertyNames.addAll(referencedPropertyNames);
		return this;
	}
	
	public ConstraintBuilder<C> operator(String operator) {
		this.operator = operator;
		return this;
	}
	
	public ConstraintBuilder<C> value(String value) {
		this.value = value;
		return this;
	}
	
	public ConstraintBuilder<C> effect(String effect) {
		this.effect = effect;
		return this;
	}

	public String getConstraintTypeCode() {
		return constraintTypeCode;
	}

	public List<String> getReferencedPropertyNames() {
		return referencedPropertyNames;
	}

	public String getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}

	public String getEffect() {
		return effect;
	}
}
