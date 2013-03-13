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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public class ManyMapSet<K, V> extends HashMap<K, Set<V>> {

	private static final long serialVersionUID = -535417510730554623L;

	public ManyMapSet() {
		super();
	}
	
	public ManyMapSet(Map<K, Set<V>> map) {
		super(map);
	}
	
	public ManyMapSet(int initialCapacity) {
		super(initialCapacity);
	}
	
	public synchronized V getOne(K key) {
		Set<V> list = get(key);
		
		return list != null && !list.isEmpty() ? list.iterator().next() : null;
	}
	
	public synchronized int putOne(K key, V value) {
		
		Set<V> list = get(key);
		if (list == null) {
			list = new HashSet<V>();
			put(key, list);
		}
		list.add(value);
		
		return list.size();
	}
	
	public synchronized V removeOne(K key, V value) {
		Set<V> list = get(key);
		
		if (list != null && !list.isEmpty()) {
			if (list.remove(value))
				return value;
		}
		return null;
	}
	
}
