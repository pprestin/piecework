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
package piecework.process.concrete;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author James Renfro
 */
public class MongoRepositoryStub<T> implements MongoRepository<T, String> {

	private final Map<String, T> map;
	
	public MongoRepositoryStub() {
		this.map = new HashMap<String, T>();
	}
	
	@Override
	public <S extends T> List<S> save(Iterable<S> records) {
		Iterator<S> iterator = records.iterator();
		List<S> list = new ArrayList<S>();
		
		while (iterator.hasNext()) {
			S record = iterator.next();
			
			String id = verifyId(record);
			map.put(id, record);
			list.add(record);
		}
		
		return list;
	}

	@Override
	public List<T> findAll() {
		return new ArrayList<T>(map.values());
	}

	@Override
	public List<T> findAll(Sort sort) {
		throw new NotImplementedException();
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		throw new NotImplementedException();
	}

	@Override
	public <S extends T> S save(S record) {
		String id = verifyId(record);
		map.put(id, record);
		return record;
	}

	@Override
	public T findOne(String id) {
		return map.get(id);
	}

	@Override
	public boolean exists(String id) {
		return map.containsKey(id);
	}
	
	@Override
	public Iterable<T> findAll(Iterable<String> ids) {
		List<T> list = new ArrayList<T>();
		Iterator<String> iterator = ids.iterator();
		
		while (iterator.hasNext()) {
			String id = iterator.next();
			T record = map.get(id);
			if (record != null)
				list.add(record);
		}
		
		return list;
	}

	@Override
	public long count() {
		return map.size();
	}

	@Override
	public void delete(String id) {
		map.put(id, null);
	}

	@Override
	public void delete(T record) {
		map.remove(record);
	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		Iterator<? extends T> iterator = entities.iterator();
		
		while (iterator.hasNext()) {
			T record = iterator.next();
			map.remove(record);
		}
	}

	@Override
	public void deleteAll() {
		map.clear();
	}
	
	private String verifyId(Object record) {
		Field[] fields = record.getClass().getDeclaredFields();
		
		Field idField = null;
		for (Field field : fields) {
			if (field.getAnnotation(Id.class) != null) {
				idField = field;
				break;
			}
		}
		
		Assert.assertNotNull(idField);
		
		String id = null;
		
		try {
			idField.setAccessible(true);
			id = (String) idField.get(record);
				
			if (id == null) {
				id = UUID.randomUUID().toString();
				idField.set(record, id);
			}
		} catch (IllegalArgumentException e) {
			Assert.fail();
		} catch (IllegalAccessException e) {
			Assert.fail();
		}
		return id;
	}

}
