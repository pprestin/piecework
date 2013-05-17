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
package piecework.form.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.exception.ValidationException;
import piecework.form.model.Constraint;
import piecework.form.model.Form;
import piecework.form.model.FormField;
import piecework.form.model.FormFieldElement;
import piecework.form.model.Option;
import piecework.form.model.OptionProvider;
import piecework.form.model.OptionResolver;
import piecework.form.model.Section;
import piecework.form.validation.AttributeValidation.Status;
import piecework.util.LayoutUtil;
import piecework.util.PropertyValueReader;

/**
 * @author James Renfro
 */
@Service
public class FormValidator {
	private static Set<String> FREEFORM_INPUT_TYPES;
	
	static {
		FREEFORM_INPUT_TYPES = new HashSet<String>(Arrays.asList("text", "textarea", "person-lookup", "current-date", "current-user"));
	}
	
	private static final Logger LOG = Logger.getLogger(FormValidator.class);
	
	private OptionResolver optionResolver;
	
	@Autowired
	private ConstraintValidatorRegistry registry;
	
	public List<AttributeValidation> validate(piecework.model.Process process, Form form, PropertyValueReader previousReader, List<String> sectionIds,
			PropertyValueReader reader, boolean doExceptionOnError, boolean isRequiredNecessary, boolean isForStorage) throws ValidationException {
		
		long start = System.currentTimeMillis();
		
		PropertyValueReader mergedPropertyValueReader = new PropertyValueReader(reader, previousReader);
		
		Set<String> restrictedTo = StringUtils.isEmpty(sectionIds) ? null : new HashSet<String>(sectionIds);
		
		List<AttributeValidation> results = new ArrayList<AttributeValidation>();
		boolean hasErrorResult = false;
		
		String layout = form.getLayout();

		String taskDefinitionKey = form.getName();
		List<Section> sections = form.getSections();
		
		if (sections != null) {
			for (Section section : sections) {
				if (section == null)
					continue;
				
				String sectionId = section.getId();
				String sectionName = section.getName();
				
				if (!isForStorage && !LayoutUtil.isSelectedSection(layout, taskDefinitionKey, sectionName))
					continue;
				
				if (restrictedTo != null && !restrictedTo.contains(sectionId) && !restrictedTo.contains(sectionName))
					continue;
				
				List<FormField> fields = section.getFields();
				if (fields == null || fields.isEmpty())
					continue;
				
				for (FormField field : fields) {
					String propertyName = field.getPropertyName();
					List<String> values = mergedPropertyValueReader.getValuesAsStrings(propertyName);
					String inputType = field.getTypeAttr();
					
					boolean isEditable = Boolean.valueOf(field.getEditable());
					boolean isPersonLookup = inputType != null && inputType.equals("person-lookup");
					
//					AttributeRecord attribute = process != null ? process.getAttribute(propertyName) : null;					
					boolean isRestricted = false;
//							attribute != null && attribute.getRestricted() != null && attribute.getRestricted().booleanValue();
					
					if (Boolean.valueOf(field.getRestricted()).booleanValue())
						isRestricted = true;
				
					boolean isText = inputType == null || FREEFORM_INPUT_TYPES.contains(inputType);
					
					boolean isFieldSpecificUpdate = restrictedTo != null && restrictedTo.contains(propertyName);
					
					// Do basic property validation
					boolean hasValues = !isFullyEmpty(values);
					boolean hasAtLeastEmptyValue = values != null && !values.isEmpty();
					
					boolean isUnchanged = false;
					
					if (hasValues) {
						
						if (!isEditable) {
							hasErrorResult = true;
							results.add(new AttributeValidation(Status.ERROR, propertyName, values, "Not permitted to set a value for this property", isRestricted, isText, isUnchanged));
							continue;
						}
						
						// Check to make sure that we're not resubmitting a value that already exists in the database for this property
						List<String> previousValues = mergedPropertyValueReader.getPreviousValues(propertyName);
						if (previousValues != null && !previousValues.isEmpty()) {
							isUnchanged = true;
							for (int i=0;i<values.size();i++) {
								String previousValue = previousValues.size() > i ? previousValues.get(i) : null;
								String currentValue = values.get(i);
								
								if (previousValue == null || !previousValue.equals(currentValue)) {
									isUnchanged = false;
									break;
								}
							}
//							This is now being done by passing a boolean isUnchanged through to later code
//							// Don't bother to rewrite values that are unchanged
//							if (isUnchanged) {
//								results.add(new AttributeValidation(Status.SUCCESS, propertyName, values, null, isRestricted, isText));
//								continue;
//							}
						}
						
						// Ensure that values for non-text properties all match valid options
						if (!isText) {
							List<Option> options = field.getOptions();
							OptionProvider<?> optionProvider = field.getOptionProvider();
							
							if (optionResolver != null)
								options = optionResolver.getOptions(inputType, optionProvider);
							
							// If no options are stored, then assume that any option is valid
							if (options != null && !options.isEmpty()) {
								
								// FIXME: This is not efficient when there is only a single value (the majority of the time
								Set<String> optionSet = new HashSet<String>();
								for (Option option : options) {
									optionSet.add(option.getValue());
								}
								
								if (values != null) {
									for (String value : values) {
										if (value != null) {
											if (!optionSet.contains(value)) {
												hasErrorResult = true;
												results.add(new AttributeValidation(Status.ERROR, propertyName, Collections.singletonList(value), "Not a valid option for this field", isRestricted, isText, isUnchanged));
											}
										}
									}
								}						
							}
						}
						
					} else {

						// No value was passed and this is a property that cannot be created or edited at this time,
						// so just bail out so we don't erroneously create a SUCCESS validation for it
						if (!isEditable) 
							continue;
						
						boolean isRequired = Boolean.valueOf(field.getRequired());
						
						if (isRequiredNecessary && isRequired) {	
							if (isFieldSpecificUpdate) {
								results.add(new AttributeValidation(Status.WARNING, propertyName, values, "Field is required", isRestricted, isText, isUnchanged));
								continue;
							} else {
								if (mergedPropertyValueReader.hasPreviousValue(propertyName))
									continue;
								
								hasErrorResult = true;
								results.add(new AttributeValidation(Status.ERROR, propertyName, values, "Field is required", isRestricted, isText, isUnchanged));
								continue;
							}
						}
						
						// Handle special case for checkboxes that have a single option -- they're either on or off, and when they're off nothing
						// is passed in... this is valid generally, unless the checkbox is 'required', in which case it has to be checked and the
						// code directly above this will send back an error
						if (inputType != null && inputType.equalsIgnoreCase("checkbox")) {
							
							// So basically what this means is that checkboxes that send nothing at all, not even an empty string, will be treated
							// exactly as if they had sent an empty string, assuming they have only one option
							if (field.getOptions() != null && field.getOptions().size() == 1) {
								hasAtLeastEmptyValue = true;
							}
							
						}
						
					}
					
					List<FormFieldElement> elements = field.getElements();
					
					if (elements != null && !elements.isEmpty()) {
						int i = 0;
						int numberOfValues = hasValues ? values.size() : 0;
						for (FormFieldElement element : elements) {
						
							String value = numberOfValues > i ? values.get(i) : null;
							
							if (value == null)
								continue;
							
							int maxSize = element.getMaxSize() != null ? Integer.parseInt(element.getMaxSize()) : -1;
							int minSize = element.getMinSize() != null ? Integer.parseInt(element.getMinSize()) : -1;
							
							if (minSize > -1 || maxSize > -1) {
								if (values != null) {
									if (minSize > -1 && value.length() < minSize) {
										hasErrorResult = true;
										results.add(new AttributeValidation(Status.ERROR, propertyName, Collections.singletonList(value), "Must be more than " + minSize + " characters", isRestricted, isText, isUnchanged));
									}
									if (maxSize > -1 && value.length() > maxSize) {
										hasErrorResult = true;
										results.add(new AttributeValidation(Status.ERROR, propertyName, Collections.singletonList(value), "Must be less than " + maxSize + " characters", isRestricted, isText, isUnchanged));
									}
								}
							}
							i++;
						}
					}

					// Do constraint validation
					List<Constraint> constraints = field.getConstraints();
					if (constraints == null) {
						if (hasAtLeastEmptyValue) {
							if (isPersonLookup) {
								String displayPropertyName = "_" + propertyName;
								List<String> displayValues = mergedPropertyValueReader.getValuesAsStrings(displayPropertyName);
								if (displayValues != null && !displayValues.isEmpty()) 
									results.add(new AttributeValidation(Status.SUCCESS, displayPropertyName, displayValues, null, isRestricted, isText, isUnchanged));
								results.add(new AttributeValidation(Status.SUCCESS, propertyName, values, null, isRestricted, isText, isUnchanged));
							}
							results.add(new AttributeValidation(Status.SUCCESS, propertyName, values, null, isRestricted, isText, isUnchanged));
						}
						continue;
					}

					if (isRequiredNecessary) {
						for (Constraint constraint : constraints) {
							ConstraintValidator<Constraint> validator = registry.retrieve(Constraint.class, constraint.getConstraintTypeCode());
	
							if (validator == null) {
								LOG.fatal("Constraint type does not have a configured validator: " + constraint.getConstraintTypeCode());
								continue;
							}
	
							AttributeValidation result = validator.validate(propertyName, constraint, mergedPropertyValueReader, isFieldSpecificUpdate, isRestricted, isText, isUnchanged);
	
							if (result.getStatus() == Status.ERROR)
								hasErrorResult = true;
	
							if (hasAtLeastEmptyValue) {
								results.add(result);
							}
						}	
					}
				}
			}			
		}
		
		if (LOG.isDebugEnabled()) {
			long end = System.currentTimeMillis();
			LOG.debug("Validation took " + (end-start) + " milliseconds");
		}
		

		if (doExceptionOnError && hasErrorResult) {	
			if (results != null) {
				for (AttributeValidation result : results) {
					LOG.warn(result.toString());
				}
			}
			throw new ValidationException(results);
		}
		
		return results;
	}
		
	private static boolean isFullyEmpty(List<String> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        for (String item : list) {
        	if (!StringUtils.isEmpty(item))
        		return false;
        }
        return true;
    }
	
	public OptionResolver getOptionResolver() {
		return optionResolver;
	}

	public void setOptionResolver(OptionResolver optionResolver) {
		this.optionResolver = optionResolver;
	}
}
