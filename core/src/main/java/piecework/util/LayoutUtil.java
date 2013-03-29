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
package piecework.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import piecework.Constants;


/**
 * @author James Renfro
 */
public class LayoutUtil {

	public enum Layout { WIZARD, PANELS, FLOW };
	
	public static Layout getLayout(String layoutStr) {
		Layout layout = Layout.PANELS;
		
		if (layoutStr != null) {
			if (layoutStr.equals("wizard"))
				layout = Layout.WIZARD;
			else if (layoutStr.equals("flow"))
				layout = Layout.FLOW;
		}
		return layout;
	}
	
	public static boolean isSelectedSection(String layout, String taskDefinitionKey, String sectionName) {
		return isSelectedSection(getLayout(layout), taskDefinitionKey, sectionName);
	}
	
	public static boolean isSelectedSection(Layout layout, String taskDefinitionKey, String sectionName) {
		if (layout == Layout.PANELS)
			return true;
		
		String[] sectionNames = sectionName != null ? sectionName.split(",") : null;
		@SuppressWarnings("unchecked")
		Set<String> sectionNameSet = (Set<String>) (sectionNames != null ? new HashSet<String>(Arrays.asList(sectionNames)) : Collections.emptySet());
		return (taskDefinitionKey == null && sectionNameSet.contains(Constants.START_TASK_DEFINITION_KEY) || (taskDefinitionKey != null && sectionNameSet.contains(taskDefinitionKey)));
	}
	
}
