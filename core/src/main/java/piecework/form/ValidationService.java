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
package piecework.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.Registry;
import piecework.model.Constraint;
import piecework.model.Field;
import piecework.model.FormSubmission;
import piecework.model.Option;
import piecework.model.ProcessInstance;
import piecework.model.Screen;
import piecework.model.Section;
import piecework.util.ManyMap;
import piecework.util.OptionResolver;

import com.google.common.collect.Sets;

/**
 * @author James Renfro
 */
@Service
public class ValidationService {
	
	private static final Set<String> FREEFORM_INPUT_TYPES = Sets.newHashSet("text", "textarea", "person-lookup", "current-date", "current-user");
	private static final Logger LOG = Logger.getLogger(ValidationService.class);

	@Autowired(required=false)
	Registry registry;
	
	
	public FormValidation validate(FormSubmission submission, ProcessInstance instance, Screen screen, String validationId) {
		
		long start = System.currentTimeMillis();
		
		FormValidation.Builder validationBuilder = new FormValidation.Builder();
		
		// Only include attachments in the validation result if attachments are allowed
		if (screen.isAttachmentAllowed()) 
			validationBuilder.attachments(submission.getAttachments());
		
		boolean hasErrorResult = false;

		List<Section> sections = screen.getSections();
		
		ManyMap<String, String> submissionValueMap = submission.getFormValueMap();
		ManyMap<String, String> instanceValueMap = instance != null ? instance.getFormValueMap() : new ManyMap<String, String>();
		
		if (sections != null) {
			for (Section section : sections) {
				if (section == null)
					continue;
				
				// If a validation id is passed, then limit the validation to the section that matches
				if (validationId != null && !validationId.equals(section.getTagId()))
					continue;
				
				List<Field> fields = section.getFields();
				if (fields == null || fields.isEmpty())
					continue;
				
				for (Field field : fields) {
					String fieldName = field.getName();
					List<String> values = submissionValueMap.get(fieldName);
					List<String> previousValues = instanceValueMap.get(fieldName);
					String inputType = field.getType();
					
					boolean isEmailAddress = false;
					boolean isNumeric = false;
					boolean isPersonLookup = false;
					boolean isAllValuesMatch = false;
					boolean isOnlyRequiredWhen = false;
					boolean isText = inputType == null || FREEFORM_INPUT_TYPES.contains(inputType);
					boolean isFieldSpecificUpdate = validationId != null && validationId.equals(fieldName);
					boolean hasValues = !isFullyEmpty(values);
					boolean hasPreviousValues = !isFullyEmpty(previousValues);
					boolean hasAtLeastEmptyValue = values != null && !values.isEmpty();
					
					OptionResolver optionResolver = null;
					
					List<Constraint> onlyRequiredWhenConstraints = null;
					
					List<Constraint> constraints = field.getConstraints();
					if (constraints != null && !constraints.isEmpty()) {
						for (Constraint constraint : constraints) {
							String type = constraint.getType();
							if (type == null)
								continue;
							
							isOnlyRequiredWhen = type.equals(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN);
							
							if (isOnlyRequiredWhen) {
								if (onlyRequiredWhenConstraints == null)
									onlyRequiredWhenConstraints = new ArrayList<Constraint>();
								onlyRequiredWhenConstraints.add(constraint);
							}
							
							if (type.equals(Constants.ConstraintTypes.IS_VALID_USER))
								isPersonLookup = true;
							else if (type.equals(Constants.ConstraintTypes.IS_NUMERIC))
								isNumeric = true;
							else if (type.equals(Constants.ConstraintTypes.IS_EMAIL_ADDRESS))
								isEmailAddress = true;
							else if (type.equals(Constants.ConstraintTypes.IS_ALL_VALUES_MATCH))
								isAllValuesMatch = true;
							else if (type.equals(Constants.ConstraintTypes.IS_LIMITED_TO)) {
								optionResolver = registry.retrieve(Option.class, constraint.getName());
							}	
						}
					}

					boolean isUnchanged = false;
					
					if (hasValues) {

						if (!field.isEditable()) {
							hasErrorResult = true;
							validationBuilder.error(fieldName, "Not permitted to set a value for this property");
							continue;
						}

						// Check to make sure that we're not resubmitting a value that already exists in the database for this property
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
							validationBuilder.unchangedField(fieldName);
						}
						
						// Ensure that values for non-text properties all match valid options
						if (!isText) {
							List<Option> options = field.getOptions();

							if (optionResolver != null)
								options = optionResolver.getOptions();
							
							// If no options are stored, then assume that any option is valid
							if (options != null && !options.isEmpty()) {
								
								// FIXME: This is not efficient when there is only a single value (the majority of the time)
								Set<String> optionSet = new HashSet<String>();
								for (Option option : options) {
									optionSet.add(option.getValue());
								}
								
								if (values != null) {
									for (String value : values) {
										if (value != null) {
											if (!optionSet.contains(value)) {
												hasErrorResult = true;
												validationBuilder.error(fieldName, "Not a valid option for this field");
											}
										}
									}
								}						
							}
						} else {
														
							int counter = 0;
							
							String lastValue = null;
							
							for (String value : values) {
							
								if (value.length() > field.getMaxValueLength()) {
									hasErrorResult = true;
									validationBuilder.error(fieldName, "Cannot be more than " + field.getMaxValueLength() + " characters");
								} else if (value.length() < field.getMinValueLength()) {
									hasErrorResult = true;
									validationBuilder.error(fieldName, "Cannot be less than " + field.getMinValueLength() + " characters");
								} 
								
								if (isNumeric && !value.matches("^[0-9]+$")) {
									hasErrorResult = true;
									validationBuilder.error(fieldName, "Must be a number");
								}
								
								if (isEmailAddress && !EmailValidator.getInstance().isValid(value)) {
									hasErrorResult = true;
									validationBuilder.error(fieldName, "Is not a valid email address");
								}
								
								if (isAllValuesMatch) {
									if (lastValue != null && !lastValue.equals(value)) {
										hasErrorResult = true;
										validationBuilder.error(fieldName, "Values do not match");
										break;
									}
									
									lastValue = value;
								}

								counter++;
							}
							
							if (field.getMaxInputs() < counter) {
								hasErrorResult = true;
								validationBuilder.error(fieldName, "No more than " + field.getMaxInputs() + " values are allowed");
							} else if (field.getMinInputs() > counter) {
								hasErrorResult = true;
								validationBuilder.error(fieldName, "At least " + field.getMinInputs() + " values are required");
							} 
						} 

					} else {

						// No value was passed and this is a property that cannot be created or edited at this time,
						// so just bail out so we don't erroneously create a SUCCESS validation for it
						if (!field.isEditable()) 
							continue;
						
						if (field.isRequired()) {	
							if (isFieldSpecificUpdate) {
								validationBuilder.warning(fieldName, "Field is required");
								continue;
							} else {
								if (hasPreviousValues)
									continue;
								
								hasErrorResult = true;
								validationBuilder.error(fieldName, "Field is required");
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
					

					
//					if (elements != null && !elements.isEmpty()) {
//						int i = 0;
//						int numberOfValues = hasValues ? values.size() : 0;
//						for (FormFieldElement element : elements) {
//						
//							String value = numberOfValues > i ? values.get(i) : null;
//							
//							if (value == null)
//								continue;
//							
//							int maxSize = element.getMaxSize() != null ? Integer.parseInt(element.getMaxSize()) : -1;
//							int minSize = element.getMinSize() != null ? Integer.parseInt(element.getMinSize()) : -1;
//							
//							if (minSize > -1 || maxSize > -1) {
//								if (values != null) {
//									if (minSize > -1 && value.length() < minSize) {
//										hasErrorResult = true;
//										results.add(new AttributeValidation(Status.ERROR, propertyName, Collections.singletonList(value), "Must be more than " + minSize + " characters", isRestricted, isText, isUnchanged));
//									}
//									if (maxSize > -1 && value.length() > maxSize) {
//										hasErrorResult = true;
//										results.add(new AttributeValidation(Status.ERROR, propertyName, Collections.singletonList(value), "Must be less than " + maxSize + " characters", isRestricted, isText, isUnchanged));
//									}
//								}
//							}
//							i++;
//						}
//					}

					// FIXME: Handle special constraint validation
//					if (registry != null) {
//						for (Constraint constraint : constraints) {
//							ConstraintValidator<Constraint> validator = registry.retrieve(Constraint.class, constraint.getType());
//	
//							if (validator == null) {
//								LOG.info("Constraint type does not have a configured validator: " + constraint.getType());
//								continue;
//							}
//	
//							AttributeValidation result = validator.validate(fieldName, constraint, mergedPropertyValueReader, isFieldSpecificUpdate, isRestricted, isText, isUnchanged);
//	
//							if (result.getStatus() == Status.ERROR)
//								hasErrorResult = true;
//	
//							if (hasAtLeastEmptyValue) {
//								results.add(result);
//							}
//						}	
//					}					
					

					if (!hasErrorResult && hasAtLeastEmptyValue) {
						if (isPersonLookup) {
							String displayPropertyName = "_" + fieldName;
							List<String> displayValues = submissionValueMap.get(displayPropertyName);
							if (displayValues != null && !displayValues.isEmpty()) 
								validationBuilder.formValue(displayPropertyName, displayValues.toArray(new String[displayValues.size()]));	
						}  
						
						// Ensure that we save the form value
						validationBuilder.formValue(fieldName, values.toArray(new String[values.size()]));
					}
				}
			}			
		}
		
		if (LOG.isDebugEnabled()) {
			long end = System.currentTimeMillis();
			LOG.debug("Validation took " + (end-start) + " milliseconds");
		}

		return validationBuilder.build();
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
	
}
