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
package piecework.validation;

import org.junit.Test;
import piecework.Constants;
import piecework.common.ManyMap;
import piecework.exception.ValidationRuleException;
import piecework.model.Constraint;
import piecework.model.Option;
import piecework.model.User;
import piecework.model.Value;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ValidationRuleTest {

    private final String fieldName = "testfield1";

    /*
     * Success condition here is just not throwing an exception
     */
    @Test
    public void testConstrainedRequiredNotRequired() throws ValidationRuleException {
        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name("testfield2")
                .value("yes")
                .build();

        ValidationRule rule = new ValidationRule.Builder(ValidationRule.ValidationRuleType.CONSTRAINED_REQUIRED)
                .name(fieldName)
                .constraint(constraint)
                .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne("testfield2", new Value("no"));
        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testConstrainedRequiredIsRequiredFailure() throws ValidationRuleException {
        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name("testfield2")
                .value("yes")
                .build();

        ValidationRule rule = new ValidationRule.Builder(ValidationRule.ValidationRuleType.CONSTRAINED_REQUIRED)
                .name(fieldName)
                .constraint(constraint)
                .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne("testfield2", new Value("yes"));
        rule.evaluate(submissionData, null);
    }

    @Test
    public void testConstrainedRequiredIsRequiredSuccess() throws ValidationRuleException {
        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name("testfield2")
                .value("yes")
                .build();

        ValidationRule rule = new ValidationRule.Builder(ValidationRule.ValidationRuleType.CONSTRAINED_REQUIRED)
                .name(fieldName)
                .constraint(constraint)
                .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("something"));
        submissionData.putOne("testfield2", new Value("yes"));
        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testEmailFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.EMAIL)
                .name(fieldName)
                .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("invalid"));
        rule.evaluate(submissionData, null);
    }

    @Test
    public void testEmailSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.EMAIL)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("valid@valid.com"));
        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testLimitedOptionsFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.LIMITED_OPTIONS)
                        .name(fieldName)
                        .options(Collections.<Option>singletonList(new Option.Builder().value("Okay").build()))
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("notOkay"));
        rule.evaluate(submissionData, null);
    }

    @Test
    public void testLimitedOptionsSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.LIMITED_OPTIONS)
                        .name(fieldName)
                        .options(Collections.<Option>singletonList(new Option.Builder().value("Okay").build()))
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("Okay"));
        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testNumberOfInputsTooFew() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMBER_OF_INPUTS)
                        .name(fieldName)
                        .numberOfInputs(3, 2)
                        .required(true)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("first"));
        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testNumberOfInputsTooMany() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMBER_OF_INPUTS)
                        .name(fieldName)
                        .numberOfInputs(3, 1)
                        .required(true)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("first"));
        submissionData.putOne(fieldName, new Value("second"));
        submissionData.putOne(fieldName, new Value("third"));
        submissionData.putOne(fieldName, new Value("fourth"));

        rule.evaluate(submissionData, null);
    }

    @Test
    public void testNumberOfInputsJustRight() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMBER_OF_INPUTS)
                        .name(fieldName)
                        .numberOfInputs(3, 1)
                        .required(true)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("first"));
        submissionData.putOne(fieldName, new Value("second"));
        submissionData.putOne(fieldName, new Value("third"));

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testNumericFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMERIC)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("nonnumeric"));

        rule.evaluate(submissionData, null);
    }

    @Test
    public void testNumericSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMERIC)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("123"));

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testPatternFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.PATTERN)
                        .name(fieldName)
                        .pattern(Pattern.compile("\\d+:\\w"))
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("df:e"));

        rule.evaluate(submissionData, null);
    }

    @Test
    public void testPatternSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.PATTERN)
                        .name(fieldName)
                        .pattern(Pattern.compile("\\d+:\\w"))
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("12:e"));

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testRequiredFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value(""));

        rule.evaluate(submissionData, null);
    }

    @Test
    public void testRequiredSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("something"));

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testRequiredIfNoPreviousNullFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED_IF_NO_PREVIOUS)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value(""));

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testRequiredIfNoPreviousFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED_IF_NO_PREVIOUS)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> previousData = new ManyMap<String, Value>();
        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value(""));

        rule.evaluate(submissionData, previousData);
    }

    @Test
    public void testRequiredIfNoPreviousSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED_IF_NO_PREVIOUS)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> previousData = new ManyMap<String, Value>();
        previousData.putOne(fieldName, new Value("something"));
        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value(""));

        rule.evaluate(submissionData, previousData);
    }

    @Test(expected = ValidationRuleException.class)
    public void testValidUserFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALID_USER)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("sdf"));

        rule.evaluate(submissionData, null);
    }

    @Test
    public void testValidUserSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALID_USER)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new User.Builder().build());

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testValueLengthTooLong() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUE_LENGTH)
                        .name(fieldName)
                        .valueLength(5, 3)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("dfgdfg"));

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testValueLengthTooShort() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUE_LENGTH)
                        .name(fieldName)
                        .valueLength(5, 3)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("df"));

        rule.evaluate(submissionData, null);
    }

    @Test
    public void testValueLengthSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUE_LENGTH)
                        .name(fieldName)
                        .valueLength(5, 3)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("dfdf"));

        rule.evaluate(submissionData, null);
    }

    @Test(expected = ValidationRuleException.class)
    public void testValuesMatchFailure() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUES_MATCH)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("df"));
        submissionData.putOne(fieldName, new Value("dfs"));

        rule.evaluate(submissionData, null);
    }

    @Test
    public void testValuesMatchSuccess() throws ValidationRuleException {
        ValidationRule rule =
                new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUES_MATCH)
                        .name(fieldName)
                        .build();

        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("dfdf"));
        submissionData.putOne(fieldName, new Value("dfdf"));

        rule.evaluate(submissionData, null);
    }

}
