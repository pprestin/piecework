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
package piecework.exception;

import piecework.common.MultipleChoices;

import java.util.ArrayList;
import java.util.List;


/**
 * This is an exception class to wrap multiple choices exceptions.
 * 
 * @author James Renfro
 */
public class MultipleChoicesError extends StatusCodeError {

	private static final long serialVersionUID = -919256383256902410L;
	
	private List<Object> entities;
	
	public MultipleChoicesError() {
		this(new ArrayList<Object>());
	}
	
	public MultipleChoicesError(List<Object> entities) {
		super(300);
		this.entities = entities;
	}

	/**
	 * @return the entities
	 */
	public List<Object> getEntities() {
		return this.entities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(List<Object> entities) {
		this.entities = entities;
	}
	
	public Object getEntity() {
		MultipleChoices multipleChoices = new MultipleChoices();
		multipleChoices.setEntities(this.getEntities());
		
		return multipleChoices;
	}

}
