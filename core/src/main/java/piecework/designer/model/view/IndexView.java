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
package piecework.designer.model.view;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author James Renfro
 */
@XmlRootElement(name = IndexView.Constants.ROOT_ELEMENT_NAME)
public class IndexView {

	public String temp;
	
	public static class Constants {
		public static final String RESOURCE_LABEL = "Index";
		public static final String ROOT_ELEMENT_NAME = "index";
	}
	
}
