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
package piecework.util;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.common.ManyMap;
import piecework.model.Field;
import piecework.model.Message;
import piecework.model.Value;
import piecework.validation.Validation;
import piecework.validation.ValidationRule;

import java.util.*;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationUtilityTest {

    @Test
    public void testValidateValidRequiredField() {
        String fieldName = "TestField";

        Field field = new Field.Builder()
                .name(fieldName)
                .editable()
                .required()
                .build();
        Validation.Builder builder = new Validation.Builder();
        List<ValidationRule> rules = new ArrayList<ValidationRule>(ValidationUtility.validationRules(field, null));
        Set<String> fieldNames = new HashSet<String>();
        fieldNames.add(fieldName);
        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        ManyMap<String, Value> instanceData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("TestValue"));

        ValidationUtility.validateField(builder, field, rules, fieldNames,
                submissionData, instanceData, true);

        Validation validation = builder.build();
        Assert.assertEquals(submissionData, validation.getData());
        Assert.assertEquals(0, validation.getResults().size());
    }

    @Test
    public void testValidateMissingRequiredField() {
        String fieldName = "TestField";

        Field field = new Field.Builder()
                .name(fieldName)
                .editable()
                .required()
                .build();
        Validation.Builder builder = new Validation.Builder();
        List<ValidationRule> rules = new ArrayList<ValidationRule>(ValidationUtility.validationRules(field, null));
        Set<String> fieldNames = new HashSet<String>();
        fieldNames.add(fieldName);
        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        ManyMap<String, Value> instanceData = new ManyMap<String, Value>();

        ValidationUtility.validateField(builder, field, rules, fieldNames,
                submissionData, instanceData, true);

        Validation validation = builder.build();
        List<Message> messages = validation.getResults().get(fieldName);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("Field is required", messages.get(0).getText());
        Assert.assertTrue(validation.getData().isEmpty());
    }



}
