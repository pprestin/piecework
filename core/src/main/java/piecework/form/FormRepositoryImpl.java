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

import piecework.form.model.record.FormRecord;

import com.sun.xml.bind.v2.model.core.ID;

/**
 * @author James Renfro
 */
public class FormRepositoryImpl extends SimpleMongoRepostiory<FormRecord, String> implements FormRepository {

	@Override
	public T collectionFindOne(ID id, String collectionName) {
		return null;
	}

}
