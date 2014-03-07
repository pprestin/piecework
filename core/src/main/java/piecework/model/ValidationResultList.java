/*
 * Copyright 2010 University of Washington
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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ValidationResultList.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ValidationResultList.Constants.TYPE_NAME)
@XmlSeeAlso(ValidationResult.class)
public class ValidationResultList {

	@XmlElementWrapper(name="items")
	private final List<ValidationResult> items;
	
	@XmlTransient
	private final Map<String, List<String>> formData;
	
	public ValidationResultList() {
		this((List<ValidationResult>)null);
	}
	
	public ValidationResultList(List<ValidationResult> items) {
		this(items, null);
	}
	
	public ValidationResultList(List<ValidationResult> items, Map<String, List<String>> formData) {
		this.items = items;
		this.formData = formData;
	}

    public ValidationResultList(Map<String, List<Message>> results) {
        this.items = new ArrayList<ValidationResult>();
        if (results != null && !results.isEmpty()) {
            for (Map.Entry<String, List<Message>> entry : results.entrySet()) {
                List<Message> messages = entry.getValue();

                if (messages != null && !messages.isEmpty()) {
                    for (Message message : messages) {
                        this.items.add(new ValidationResult(message.getType(), entry.getKey(), message.getText()));
                    }
                }
            }
        }
        this.formData = null;
    }

	public List<ValidationResult> getItems() {
		return items;
	}

	public Map<String, List<String>> getFormData() {
		return formData;
	}
	
	static class Constants {
		public static final String ROOT_ELEMENT_NAME = "validationResults";
		public static final String TYPE_NAME = "ValidationResultsType";
	}

}
