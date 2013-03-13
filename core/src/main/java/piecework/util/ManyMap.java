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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ManyMap<K, V> extends Hashtable<K, List<V>> {
    
	private static final long serialVersionUID = -5990287399286512607L;

	public ManyMap() {
		super();
	}
	
	public ManyMap(Map<K, List<V>> map) {
		super(map);
	}
	
	public ManyMap(int initialCapacity) {
		super(initialCapacity);
	}
	
	public synchronized V getOne(K key) {
		List<V> list = get(key);
		
		return list != null && !list.isEmpty() ? list.iterator().next() : null;
	}
	
	public synchronized int putOne(K key, V value) {
		
		List<V> list = get(key);
		if (list == null) {
			list = new Vector<V>();
			put(key, list);
		}
		list.add(value);
		
		return list.size();
	}
	
}
