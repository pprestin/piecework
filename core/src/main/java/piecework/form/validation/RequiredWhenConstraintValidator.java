/*
 * Copyright 2011 University of Washington
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
package piecework.form.validation;

import java.util.List;

import org.apache.cxf.common.util.StringUtils;
import org.springframework.stereotype.Service;

import piecework.form.legacy.AttributeValidation;
import piecework.form.model.Constraint;
import piecework.form.legacy.AttributeValidation.Status;
import piecework.util.PropertyValueReader;

/**
 * @author James Renfro
 *
 */
@Service
public class RequiredWhenConstraintValidator implements ConstraintValidator<Constraint> {

	@Override
	public AttributeValidation validate(String propertyName, Constraint constraint,
			PropertyValueReader propertyValueReader,
			boolean isFieldSpecificUpdate, boolean isRestricted,
			boolean isText, boolean isUnchanged) {
		
		List<String> currentValues = propertyValueReader.getValuesAsStrings(propertyName);
		List<String> referencedPropertyNames = constraint.getReferencedPropertyNames();

		if (referencedPropertyNames != null && referencedPropertyNames.size() > 0) {
			String referencedPropertyName = referencedPropertyNames.iterator().next();
			List<String> values = propertyValueReader.getValuesAsStrings(referencedPropertyName);
			
			if (StringUtils.isEmpty(currentValues)) {
				String value = values != null && !values.isEmpty() ? values.iterator().next() : null;
				// If no value is provided on the constraint then it just means that the property value must be non-null
				// Otherwise, if a value is provided then the property value can be null, but if it's not and it matches, then that's an error
				if ((StringUtils.isEmpty(constraint.getValue()) && StringUtils.isEmpty(value)) || 
						(!StringUtils.isEmpty(constraint.getValue()) && 
						 (!StringUtils.isEmpty(value) && value.equals(constraint.getValue())))) {
					
					if (propertyValueReader.hasPreviousValue(propertyName))
						return new AttributeValidation(Status.SUCCESS, propertyName, null, null, isRestricted, isText, isUnchanged);
					
					if (isFieldSpecificUpdate)
						return new AttributeValidation(Status.WARNING, propertyName, currentValues, "Field is required", isRestricted, isText, isUnchanged);
					
					return new AttributeValidation(Status.ERROR, propertyName, currentValues, "Field is required", isRestricted, isText, isUnchanged);
				}
			}
		}
		
		return new AttributeValidation(Status.SUCCESS, propertyName, currentValues, null, isRestricted, isText, isUnchanged);
	}
	

	@Override
	public Class<Constraint> getType() {
		return Constraint.class;
	}

	@Override
	public String getKey() {
		return "required";
	}

}
