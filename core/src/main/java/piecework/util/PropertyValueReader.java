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
package piecework.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public interface PropertyValueReader {

	String getFirstValue(String propertyName);
	
	List<String> getValuesAsStrings(String propertyName);
	
	Set<String> keySet();
	
	Set<Map.Entry<String, List<String>>> entrySet();
	
	boolean hasPreviousValue(String propertyName);
	
	List<String> getPreviousValues(String propertyName);
	
	String getTitle();
	
}
