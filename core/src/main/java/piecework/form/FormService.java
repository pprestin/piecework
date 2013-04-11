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
package piecework.form;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.exception.StatusCodeError;
import piecework.exception.ValidationException;
import piecework.form.model.Form;
import piecework.form.model.Message;
import piecework.form.model.builder.FormFieldBuilder;
import piecework.form.model.builder.FormFieldElementBuilder;
import piecework.form.model.builder.OptionBuilder;
import piecework.form.model.builder.SectionBuilder;
import piecework.form.model.record.FormRecord;
import piecework.form.validation.AttributeValidation;
import piecework.form.validation.FormValidator;
import piecework.process.ProcessService;
import piecework.process.exception.ProcessNotFoundException;
import piecework.util.ManyMap;
import piecework.util.PropertyValueReader;
import piecework.util.UserReference;

/**
 * @author James Renfro
 */
@Service
public class FormService {

	private static final Logger LOG = Logger.getLogger(FormService.class);
	
	private static Set<String> FREEFORM_INPUT_TYPES;
	
	static {
		FREEFORM_INPUT_TYPES = new HashSet<String>(Arrays.asList("text", "textarea", "person-lookup", "current-date", "current-user"));
	}
	
	@Autowired
	private ProcessService processService;
	
	@Autowired 
	private FormRepository repository;
	
	@Autowired
	private FormValidator validator;
	
	public List<String> findProcessInstanceIds(String processDefinitionKey) throws StatusCodeError {
		return null;
	}
	
	public  Set<String> findProcessInstanceIdsByParameterMap(Collection<String> processDefinitionKeys, Map<String, List<String>> parameterMap) throws StatusCodeError {
		return null;
	}
	
	public Map<String, String> getColumnMap(String processDefinitionKey, String taskDefinitionKey) throws StatusCodeError {
		return null;
	}
	
//	public List<Form> getForms(String processDefinitionKey, String taskDefinitionKey) throws StatusCodeError {
//		return null;
//	}
	
	public Form getForm(String processDefinitionKey, FormPosition position, String taskDefinitionKey) throws ProcessNotFoundException {
		FormRecord form = getFormRecord(processDefinitionKey, position, taskDefinitionKey);
		
		return form;
	}
	
	public Form getForm(String processDefinitionKey, FormPosition position, String taskDefinitionKey, List<AttributeValidation> validations) throws ProcessNotFoundException {
		FormRecord form = getFormRecord(processDefinitionKey, position, taskDefinitionKey);
//		ManyMap<String, String> valuesMap = new ManyMap<String, String>(formData);
		
		return includeValues(form, null, null, null, false, false, validations);
	}
	
	
//	public Form getForm(String processDefinitionKey, String taskDefinitionKey, List<ValidationResult> results, Map<String, List<String>> formData, List<Attachment> attachments) throws StatusCodeError {
//		return null;
//	}
//	
//	public Form getForm(String processDefinitionKey, String taskDefinitionKey, String processInstanceId, boolean includeRestricted, boolean readOnly) throws StatusCodeError {
//		return null;
//	}
	
	public Form storeForm(String processDefinitionKey, FormPosition position, String taskDefinitionKey, Form form) throws ProcessNotFoundException {
		
		FormRecord.Builder builder = new FormRecord.Builder(form);
		if (processDefinitionKey != null)
			builder.processDefinitionKey(processDefinitionKey);
		if (taskDefinitionKey != null)
			builder.taskDefinitionKey(taskDefinitionKey);
		
		FormRecord record = builder.build();
		record = repository.save(record);
		
		processService.addForm(position, record);
		
		return record;
	}
	
	public ManyMap<String, String> getValues(String processDefinitionKey, String processInstanceId) throws StatusCodeError {
		return null;
	}
	
	public void validate(String processDefinitionKey, String taskDefinitionKey, List<String> sectionNames, PropertyValueReader reader) throws ValidationException, ProcessNotFoundException {
		piecework.process.model.Process process = processService.getProcess(processDefinitionKey);
		
		FormPosition position = FormPosition.TASK_REQUEST;
		
		if (taskDefinitionKey == null)
			position = FormPosition.START_REQUEST;
		
		FormRecord form = getFormRecord(processDefinitionKey, position, taskDefinitionKey);
		PropertyValueReader previousReader = null;
		validator.validate(process, form, previousReader, sectionNames, reader, true, true, true);
	}
	
	public void storeValues(String processDefinitionKey, String taskDefinitionKey, String processBusinessKey, List<String> sectionNames, PropertyValueReader reader, boolean isRequiredNecessary) throws ValidationException, ProcessNotFoundException {
		long validationStart = System.currentTimeMillis();
		
		piecework.process.model.Process process = processService.getProcess(processDefinitionKey);
		
		FormPosition position = FormPosition.TASK_REQUEST;
		
		if (taskDefinitionKey == null)
			position = FormPosition.START_REQUEST;
		
		FormRecord form = getFormRecord(processDefinitionKey, position, taskDefinitionKey);
		
//		if (form == null)
//			throw new InternalServerError();
		
		PropertyValueReader previousReader = null;
		List<AttributeValidation> validations = validator.validate(process, form, previousReader, sectionNames, reader, true, isRequiredNecessary, true);
				
//				validator.validate(process, form, previousInstanceDocument, sectionNames, reader, null, !doForceStore, isRequiredNecessary, true);
//		
//		if (LOG.isInfoEnabled())
//			LOG.info("Validation for mongo took " + (System.currentTimeMillis() - validationStart) + " ms");
//				
//		if (validations != null) 
//			doStoreValues(namespace, processDefinitionKey, processInstanceId, reader.getTitle(), validations, doForceStore, context);

	}

	public void storeMessages(String processDefinitionKey, String taskDefinitionKey, String processInstanceId, List<Message> messages) throws StatusCodeError {

	}

	private FormRecord getFormRecord(String processDefinitionKey, FormPosition position, String taskDefinitionKey) throws ProcessNotFoundException {
		
		piecework.process.model.Process process = processService.getProcess(processDefinitionKey);
		
		String formId = getFormId(process, position, taskDefinitionKey);
		
		FormRecord form = null;
		
		if (formId != null) {
			form = repository.findOne(formId);
	
			if (form != null) {
				String layout = form.getLayout();
				// If the layout is wizard then only the start task form is going to
				// be returned, and the only section that will be editable is the
				// one that
				if (layout != null && layout.equalsIgnoreCase("wizard")) {
					form = repository.findOne(process.getStartRequestFormIdentifier());
				}
			}
		}
		
		return form;
	}

	private String getFormId(piecework.process.model.Process process, FormPosition position, String taskDefinitionKey) {
		String formId = null;
		Map<String, String> map = null;
		switch (position) {
		case START_REQUEST:
			formId = process.getStartRequestFormIdentifier();
			break;
		case START_RESPONSE:
			formId = process.getStartResponseFormIdentifier();
			break;
		case TASK_REQUEST:
			map = process.getTaskRequestFormIdentifiers();
			break;
		case TASK_RESPONSE:
			map = process.getTaskRequestFormIdentifiers();
			break;
		}
		
		if (map != null && !map.isEmpty()) {
			String potentialFormId = map.get(taskDefinitionKey);
			if (potentialFormId != null)
				formId = potentialFormId;
		}
		return formId;
	}
	
	
	private Form includeValues(Form form, ManyMap<String, String> valuesMap, Map<String, Message> messagesMap, Process process, boolean includeRestricted, boolean readOnly, List<AttributeValidation> results) {
		
		FormRecord.Builder builder = new FormRecord.Builder(form);
		Map<String, AttributeValidation> validationMap = results != null && !results.isEmpty() ? new HashMap<String, AttributeValidation>(results.size()) : null;
		if (validationMap != null) {
			for (AttributeValidation result : results) {
				validationMap.put(result.getAttributeName(), result);
			}
		}
		
		boolean doResetSelectedSection = messagesMap != null && !messagesMap.isEmpty();
		
		List<SectionBuilder<?>> sections = builder.getSections();
		if (sections != null) {
			for (SectionBuilder<?> section : sections) {
				List<FormFieldBuilder<?>> fields = section.getFields();
			
				if (fields != null) {
					for (FormFieldBuilder<?> field : fields) {
						String propertyName = field.getPropertyName();
						String inputType = field.getTypeAttr();
						
						boolean isFreeformInput = inputType != null && FREEFORM_INPUT_TYPES.contains(inputType);
						boolean isPersonLookup = inputType != null && inputType.equals("person-lookup");
												
//						AttributeRecord attributeRecord = processDocument != null ? processDocument.getAttribute(propertyName) : null;
						
						// Values can be restricted at the process level, the field level, or as a result of having been restricted in an earlier task
						boolean isRestricted = (field.getRestricted() != null && field.getRestricted().booleanValue()); // || (attributeRecord != null && attributeRecord.getRestricted() != null && attributeRecord.getRestricted().booleanValue());

						boolean isValueRestricted = false;
						
						List<String> values = valuesMap != null ? valuesMap.get(propertyName) : null;
						
						if (validationMap != null) {
							AttributeValidation validation = validationMap.get(propertyName);
						
							if (validation != null) {
								field.message(validation.getMessage());
								field.messageType(validation.getStatus().toString());
							
								values = validation.getValues();
							}
							
						} else if (messagesMap != null) {
							Message messageReference = messagesMap.get(propertyName);
							
							if (messageReference != null) {
								String messageText = messageReference.getMessage();
								
								if (isRestricted && readOnly && values != null && !values.isEmpty()) {
									for (String value : values) {
										if (value == null)
											continue;
										
										String parsed = value.replaceAll("-", "");
										
										int size = value.length();
										int start = messageText.indexOf(parsed);
										
										if (start != -1) {
											String masked = String.format("%" + size + "s", "").replace(' ', '*');
											messageText = messageText.replaceAll(parsed, masked);
										}
										
										start = messageText.indexOf(value);
										if (start != -1) {
											String masked = String.format("%" + size + "s", "").replace(' ', '*');
											messageText = messageText.replaceAll(value, masked);
										}	
									}
								}
								
								field.message(messageText);
								field.messageType(messageReference.getMessageType());
							
								if (doResetSelectedSection) {
									section.selected(true);
									section.editable(true);
									doResetSelectedSection = false;
								}
							}
							
						}
						
						if (values == null || values.isEmpty())
							continue;

						if (isValueRestricted && !isRestricted) {
							field.restricted(Boolean.TRUE);
							isRestricted = true;
						}
						
						// If this call to getForm is not supposed to include restricted values then we can simply escape out
//						if (isRestricted && !includeRestricted) 
//							continue;
						
						
						String selectedOptionLabel = null;
						
						// Don't pick a specific option if we're dealing with a restricted field unless the user is allowed to see restricted fields
						if (!isRestricted || includeRestricted) {
						
							List<OptionBuilder<?>> options = field.getOptions();
							// TODO: See if it's worth implementing a more efficient method
							if (options != null) {
								for (OptionBuilder<?> option : options) {
									String optionValue = option.getValue();
								
									if (optionValue != null && values.contains(optionValue)) {
										option.selected(true);
										if (selectedOptionLabel == null)
											selectedOptionLabel = option.getLabel();
									}
								}
							}
						
						}
						
						List<FormFieldElementBuilder<?>> elements = field.getElements();
						if (elements != null && !elements.isEmpty()) {
							int i = 0;
							int numberOfValues = values.size();
							
							for (FormFieldElementBuilder<?> element : elements) {
								if (i >= numberOfValues)
									break;

								if (isRestricted && !includeRestricted) {									
									String masked = null;
									try {
										String maxSize = element.getMaxSize();
										String minSize = element.getMinSize();
										if (maxSize != null) {
											int size = Integer.parseInt(maxSize);
											masked = String.format("%" + size + "s", "").replace(' ', '*');
										}
										
									} catch (NumberFormatException nfe) {
										LOG.warn(nfe);
									}
										
									if (masked == null) 
										masked = "****";
										
									element.text(masked);
								} else if (isPersonLookup) {
									
									String personId = values.get(i);
									element.valueAttr(personId);
									
									// FIXME: Wire up spring ldap provider here
									UserReference userReference = null; //userReferenceManager.getUserByPrimaryId(personId);
									
//									if (userReference == null)
//										userReference = userReferenceManager.getUserBySecondaryId(personId);
									
									if (userReference != null) {
										element.valueAttr(userReference.getId());
										element.text(userReference.getDisplayName());
									}
									
								} else if (isFreeformInput) {
									element.text(values.get(i));
								} else {
									String value = values.get(i);
									element.valueAttr(value);
									if (selectedOptionLabel != null)
										element.text(selectedOptionLabel);
								}
								
								i++;
							}
						}	
					}
				}
			}
		}
		return builder.build();
	}
}
