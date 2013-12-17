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
package piecework.validation;

import piecework.Registrant;
import piecework.form.legacy.AttributeValidation;
import piecework.util.PropertyValueReader;

/**
 * @author James Renfro
 */
@Deprecated
public interface ConstraintValidator<C> extends Registrant<C> {

	AttributeValidation validate(String propertyName, C constraint, PropertyValueReader propertyValueReader, boolean isFieldSpecificUpdate, boolean isRestricted, boolean isText, boolean isUnchanged);

}
