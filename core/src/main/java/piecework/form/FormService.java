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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.EntityConverter;
import org.springframework.stereotype.Service;

import piecework.authorization.AuthorizationContext;
import piecework.common.view.ValidationResult;
import piecework.exception.StatusCodeError;
import piecework.form.model.Attachment;
import piecework.form.model.Form;
import piecework.form.model.Message;
import piecework.util.ManyMap;
import piecework.util.PropertyValueReader;

/**
 * @author James Renfro
 */
@Service
public class FormService {

	@Autowired 
	private FormRepository repository;
	
	public List<String> findProcessInstanceIds(String namespace, String processDefinitionKey) throws StatusCodeError {
		return null;
	}
	
	public  Set<String> findProcessInstanceIdsByParameterMap(String namespace, Collection<String> processDefinitionKeys, Map<String, List<String>> parameterMap) throws StatusCodeError {
		return null;
	}
	
	public Map<String, String> getColumnMap(String namespace, String processDefinitionKey, String taskDefinitionKey, EntityConverter entityConverter, AuthorizationContext context) throws StatusCodeError {
		return null;
	}
	
	public List<Form> getForms(String namespace, String processDefinitionKey, String taskDefinitionKey, AuthorizationContext context) throws StatusCodeError {
		return null;
	}
	
	public Form getForm(String namespace, String processDefinitionKey, String taskDefinitionKey, AuthorizationContext context) throws StatusCodeError {
		return null;
	}
	
	public Form getForm(String namespace, String processDefinitionKey, String taskDefinitionKey, AuthorizationContext context, List<ValidationResult> results, Map<String, List<String>> formData, List<Attachment> attachments) throws StatusCodeError {
		return null;
	}
	
	public Form getForm(String namespace, String processDefinitionKey, String taskDefinitionKey, String processInstanceId, boolean includeRestricted, boolean readOnly, AuthorizationContext context) throws StatusCodeError {
		return null;
	}
	
	public Form storeForm(String namespace, String processDefinitionKey, String taskDefinitionKey, Form formReference, String userId) throws StatusCodeError {
		return null;
	}
	
	public ManyMap<String, String> getValues(String namespace, String processDefinitionKey, String processInstanceId, AuthorizationContext context) throws StatusCodeError {
		return null;
	}
	
	public void validate(String namespace, String processDefinitionKey, String taskDefinitionKey, List<String> sectionNames, PropertyValueReader reader, EntityConverter entityConverter, AuthorizationContext context) throws StatusCodeError {

	}
	
	public void storeValues(String namespace, String processDefinitionKey, String taskDefinitionKey, String processInstanceId, List<String> sectionNames, PropertyValueReader reader, AuthorizationContext context, boolean isRequiredNecessary) throws StatusCodeError {

	}

	public void storeMessages(String namespace, String processDefinitionKey, String taskDefinitionKey, String processInstanceId, List<Message> messages) throws StatusCodeError {

	}

		
}
