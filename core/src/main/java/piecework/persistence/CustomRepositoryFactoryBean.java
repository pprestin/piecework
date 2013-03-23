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
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.stereotype.Service;

/**
 * @author James Renfro
 */
@Service
public class CustomRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>  extends MongoRepositoryFactoryBean<T, S, ID> {

	protected RepositoryFactorySupport createRepositoryFactory(MongoOperations mongoOperations) {
		return new CustomRepositoryFactory<S, ID>(mongoOperations);
	}

	private static class CustomRepositoryFactory<T, ID extends Serializable> extends MongoRepositoryFactory {

		private MongoOperations mongoOperations;
		private Class<?> repositoryInterface;
		
		public CustomRepositoryFactory(MongoOperations mongoOperations) {
			super(mongoOperations);
			this.mongoOperations = mongoOperations;
			this.repositoryInterface = CustomRepositoryFactory.class;
		}

		@SuppressWarnings("unchecked")
		protected Object getTargetRepository(RepositoryMetadata metadata) {
			return new MongoCollectionAwareRepositoryImpl<T, ID>((MongoEntityInformation<T, ID>)metadata, mongoOperations);
		}

		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {

			return CustomRepositoryFactory.class;
		}

		public Class<?> getRepositoryInterface() {
			return repositoryInterface;
		}
	}
	
}
