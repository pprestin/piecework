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

import org.junit.Assert;
import org.junit.Test;
import piecework.Constants;
import piecework.model.Constraint;
import piecework.model.Field;
import piecework.model.FormValue;
import piecework.test.ExampleFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ConstraintUtilTest {

    @Test
    public void testEvaluateDefaultValueTrue() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN)
                .name(budgetNumber.getName())
                .value("^100000$")
                .build();

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, null, constraint);
        Assert.assertTrue(isSatisfied);
    }

    @Test
    public void testEvaluateDefaultValueFalse() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .build();

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, null, constraint);
        Assert.assertFalse(isSatisfied);
    }

    @Test
    public void testEvaluateFormValueTrue() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN)
                .name(budgetNumber.getName())
                .value("^200000$")
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("200000").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertTrue(isSatisfied);
    }

    @Test
    public void testEvaluateFormValueFalse() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN)
                .name(budgetNumber.getName())
                .value("^200000$")
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("200001").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertFalse(isSatisfied);
    }

    @Test
    public void testAndConstraintFormValueOnlyFirstSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .and(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("200001").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("demote").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertFalse(isSatisfied);
    }

    @Test
    public void testAndConstraintFormValueOnlySecondSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .and(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("200001$").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("promote").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertFalse(isSatisfied);
    }

    @Test
    public void testAndConstraintFormValueBothSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .and(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();


        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("100001").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("reprimand").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertTrue(isSatisfied);
    }

    @Test
    public void testAndConstraintFormValueNoneSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .and(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("200001").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("demote").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertFalse(isSatisfied);
    }

    @Test
    public void testOrConstraintFormValueFirstSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .or(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("100001").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("demote").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertTrue(isSatisfied);
    }

    @Test
    public void testOrConstraintFormValueSecondSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .or(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("200001$").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("promote").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertTrue(isSatisfied);
    }

    @Test
    public void testOrConstraintFormValueBothSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .or(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("100001").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("reprimand").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertTrue(isSatisfied);
    }

    @Test
    public void testOrConstraintFormValueNoneSatisfied() throws Exception {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field actionType = ExampleFactory.actionTypeField();
        fieldMap.put(actionType.getName(), actionType);

        Field budgetNumber = ExampleFactory.budgetNumberField();
        fieldMap.put(budgetNumber.getName(), budgetNumber);

        Constraint constraint = new Constraint.Builder()
                .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                .name(budgetNumber.getName())
                .value("^100001$")
                .or(new Constraint.Builder()
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                        .name(actionType.getName())
                        .value("^(reprimand|promote)$")
                        .build())
                .build();

        Map<String, FormValue> formValueMap = new HashMap<String, FormValue>();
        formValueMap.put(budgetNumber.getName(), new FormValue.Builder().name(budgetNumber.getName()).value("200001").build());
        formValueMap.put(actionType.getName(), new FormValue.Builder().name(actionType.getName()).value("demote").build());

        boolean isSatisfied = ConstraintUtil.evaluate(fieldMap, formValueMap, constraint);
        Assert.assertFalse(isSatisfied);
    }

    @Test
    public void testCheckAny() throws Exception {

    }

}
