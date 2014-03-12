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
package piecework.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import piecework.model.Process;

import java.util.List;

/**
 * @author James Renfro
 */
public interface ProcessRepository extends MongoRepository<Process, String> {

    @Query(value="{isDeleted : false}", fields="{_id:1, processDefinitionLabel:1, deploymentId: 1, deploymentLabel:1, deploymentVersion:1, deploymentDate:1, isDeleted :1}")
    List<Process> findAllBasic();

    @Query(value="{_id : { $in: ?0 }, isDeleted : false}", fields="{_id:1, processDefinitionLabel:1, deploymentId: 1, deploymentLabel:1, deploymentVersion:1, deploymentDate:1, facets: 1, isDeleted :1}")
    List<Process> findAllBasic(Iterable<String> processDefinitionKeys);

}
