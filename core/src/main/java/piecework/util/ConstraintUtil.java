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

import piecework.Constants;
import piecework.model.Constraint;
import piecework.model.Field;
import piecework.model.FormValue;
import piecework.model.Value;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ConstraintUtil {

    public static boolean hasConstraint(String type, List<Constraint> constraints) {
        return getConstraint(type, constraints) != null;
    }

    public static Constraint getConstraint(String type, List<Constraint> constraints) {
        if (constraints != null && !constraints.isEmpty()) {
            for (Constraint constraint : constraints) {
                if (constraint.getType() != null && constraint.getType().equals(type))
                    return constraint;
            }
        }
        return null;
    }

    public static boolean evaluate(Map<String, Field> fieldMap, ManyMap<String, Value> submissionData, Constraint constraint) {
        if (constraint == null)
            return true;

        String constraintName = constraint.getName();
        String constraintValue = constraint.getValue();
        Pattern pattern = Pattern.compile(constraintValue);

        boolean isSatisfied = false;

        Field constraintField = fieldMap != null ? fieldMap.get(constraintName) : null;
        List<? extends Value> values = submissionData != null ? submissionData.get(constraintName) : null;

        // Evaluate whether this particular item is satisfied
        if (constraintField != null && (values == null || values.isEmpty())) {
            String defaultFieldValue = constraintField.getDefaultValue();
            isSatisfied = defaultFieldValue != null && pattern.matcher(defaultFieldValue).matches();
        } else {
            if (values != null) {
                for (Value value : values) {
                    isSatisfied = values != null && pattern.matcher(value.getValue()).matches();
                    if (!isSatisfied)
                        break;
                }
            }
        }

        // If it is satisfied, then evaluate each of the 'and' constraints
        if (isSatisfied) {
            return checkAll(null, fieldMap, submissionData, constraint.getAnd());
        } else {
            if (constraint.getOr() != null && !constraint.getOr().isEmpty())
                return checkAny(null, fieldMap, submissionData, constraint.getOr());
        }


        return isSatisfied;
    }

    public static boolean checkAll(String type, Map<String, Field> fieldMap, ManyMap<String, Value> submissionData, List<Constraint> constraints) {
        if (constraints != null && !constraints.isEmpty()) {
            for (Constraint constraint : constraints) {
                if (type == null || constraint.getType() == null || constraint.getType().equals(type)) {
                    if (! evaluate(fieldMap, submissionData, constraint))
                        return false;
                }
            }
        }
        return true;
    }

    public static boolean checkAny(String type, Map<String, Field> fieldMap, ManyMap<String, Value> submissionData, List<Constraint> constraints) {
        if (constraints != null && !constraints.isEmpty()) {
            for (Constraint constraint : constraints) {
                if (type == null || constraint.getType() == null || constraint.getType().equals(type)) {
                    if (evaluate(fieldMap, submissionData, constraint))
                        return true;
                }
            }
            return false;
        }
        return true;
    }


}
