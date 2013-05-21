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
package piecework;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.map.MultiKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author James Renfro
 */
@Service
public class Registry {

	private MultiKeyMap map = new MultiKeyMap();
	
	@Autowired
	Registrant<?>[] registrants;
	
	@SuppressWarnings("unchecked")
	public <V extends Registrant<C>, C> V retrieve(Class<C> type, String key) {
		return (V) map.get(type, key);
	}
	
	@PostConstruct
	public void register() {
		for (Registrant<?> registrant : registrants) {
			map.put(registrant.getType(), registrant.getKey(), registrant);
		}
	}
	
}
