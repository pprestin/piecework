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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;
import org.apache.cxf.common.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import piecework.process.ProcessRepository;
import piecework.process.model.record.ProcessRecord;

/**
 * @author James Renfro
 */
public class ProcessRepositoryStub implements ProcessRepository {

	private final Map<String, ProcessRecord> map;
	
	public ProcessRepositoryStub() {
		this.map = new HashMap<String, ProcessRecord>();
	}
	
	@Override
	public <S extends ProcessRecord> List<S> save(Iterable<S> records) {
		Iterator<S> iterator = records.iterator();
		List<S> list = new ArrayList<S>();
		
		while (iterator.hasNext()) {
			S record = iterator.next();
			
			if (StringUtils.isEmpty(record.getId()))
				record.setId(UUID.randomUUID().toString());
			
			map.put(record.getId(), record);
			list.add(record);
		}
		
		return list;
	}

	@Override
	public List<ProcessRecord> findAll() {
		return new ArrayList<ProcessRecord>(map.values());
	}

	@Override
	public List<ProcessRecord> findAll(Sort sort) {
		throw new NotImplementedException();
	}

	@Override
	public Page<ProcessRecord> findAll(Pageable pageable) {
		throw new NotImplementedException();
	}

	@Override
	public <S extends ProcessRecord> S save(S record) {
		if (StringUtils.isEmpty(record.getId()))
			record.setId(UUID.randomUUID().toString());
		
		map.put(record.getId(), record);
		
		return record;
	}

	@Override
	public ProcessRecord findOne(String id) {
		return map.get(id);
	}

	@Override
	public boolean exists(String id) {
		return map.containsKey(id);
	}
	
	@Override
	public Iterable<ProcessRecord> findAll(Iterable<String> ids) {
		List<ProcessRecord> list = new ArrayList<ProcessRecord>();
		Iterator<String> iterator = ids.iterator();
		
		while (iterator.hasNext()) {
			String id = iterator.next();
			ProcessRecord record = map.get(id);
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
	public void delete(ProcessRecord record) {
		map.remove(record);
	}

	@Override
	public void delete(Iterable<? extends ProcessRecord> entities) {
		Iterator<? extends ProcessRecord> iterator = entities.iterator();
		
		while (iterator.hasNext()) {
			ProcessRecord record = iterator.next();
			map.remove(record);
		}
	}

	@Override
	public void deleteAll() {
		map.clear();
	}

}
