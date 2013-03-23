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
package piecework.persistence;

import java.io.Serializable;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.stereotype.Service;

/**
 * @author James Renfro
 */
public class MongoCollectionAwareRepositoryImpl<T, ID extends Serializable> extends SimpleMongoRepository<T,ID> implements MongoCollectionAwareRepository<T,ID> {

	private final MongoEntityInformation<T, ID> metadata;
	private final MongoOperations mongoOperations;
	
	public MongoCollectionAwareRepositoryImpl(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
		super(metadata, mongoOperations);
		this.metadata = metadata;
		this.mongoOperations = mongoOperations;
	}

	@Override
	public T collectionFindOne(ID id, String collectionName) {
		return null;
	}

}
