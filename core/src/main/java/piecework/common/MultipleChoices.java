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
package piecework.common;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a representation of the multiple choices exception entity. It is returned
 * if a given GET request resolves to multiple entities. 
 * 
 * @author James Renfro
 */
@XmlRootElement
public class MultipleChoices {

	private List<Object> options;

	public MultipleChoices() {
		this.options = new ArrayList<Object>();
	}
	
	/**
	 * @return the options
	 */
	public List<Object> getEntities() {
		return this.options;
	}

	/**
	 * @param options the options to set
	 */
	public void setEntities(List<Object> options) {
		this.options = options;
	}

}
