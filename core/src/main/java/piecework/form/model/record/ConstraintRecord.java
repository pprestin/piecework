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

import java.util.List;

import piecework.form.model.Constraint;

/**
 * @author James Renfro
 */
public class ConstraintRecord implements Constraint {

	private String constraintTypeCode;
	private List<String> referencedPropertyNames;
	private String operator;
	private String value;
	private String effect;

	public ConstraintRecord() {
		
	}
	
	public ConstraintRecord(Constraint contract) {
		this.constraintTypeCode = contract.getConstraintTypeCode();
		this.referencedPropertyNames = contract.getReferencedPropertyNames();
		this.operator = contract.getOperator();
		this.value = contract.getValue();
		this.effect = contract.getEffect();
	}

	public String getConstraintTypeCode() {
		return constraintTypeCode;
	}

	public void setConstraintTypeCode(String constraintTypeCode) {
		this.constraintTypeCode = constraintTypeCode;
	}

	public List<String> getReferencedPropertyNames() {
		return referencedPropertyNames;
	}

	public void setReferencedPropertyNames(List<String> referencedPropertyNames) {
		this.referencedPropertyNames = referencedPropertyNames;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getEffect() {
		return effect;
	}

	public void setEffect(String effect) {
		this.effect = effect;
	}
	
}
