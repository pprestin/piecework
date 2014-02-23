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
package piecework.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import piecework.model.*;
import piecework.repository.custom.ProcessInstanceRepositoryCustom;

import java.util.List;

/**
 * @author James Renfro
 */
public interface ProcessInstanceRepository extends MongoRepository<ProcessInstance, String>, ProcessInstanceRepositoryCustom {

    @Query(value="{ 'processDefinitionKey' : ?0 }")
    List<ProcessInstance> findByProcessDefinitionKey(String processDefinitionKey);

    @Query(value="{ 'processDefinitionKey' : ?0, 'engineProcessInstanceId' : ?1 }")
    ProcessInstance findByProcessDefinitionKeyAndEngineProcessInstanceId(String processDefinitionKey, String engineProcessInstanceId);

    List<ProcessInstance> findByKeywordsRegex(String keyword);

    @Query(value="{ 'processInstanceId' : { $in: ?0 } }")
    List<ProcessInstance> findByProcessInstanceIdIn(Iterable<String> processInstanceIds);

    @Query(value="{ 'processInstanceId' : { $in: ?0 }, 'keywords' : { $regex: ?1 } }")
    List<ProcessInstance> findByProcessInstanceIdInAndKeywordsRegex(Iterable<String> processInstanceIds, String keyword);

    @Query(value="{ 'processDefinitionKey' : { $in: ?0 }, 'engineProcessInstanceId' : { $in: ?1 } }", fields="{ 'processDefinitionKey': 1, 'processInstanceId':1, 'engineProcessInstanceId': 1, 'alias':1, 'processInstanceLabel':1 }")
    List<ProcessInstance> findByProcessDefinitionKeyInAndEngineProcessInstanceIdIn(Iterable<String> processDefinitionKeys, Iterable<String> engineProcessInstanceIds);

    @Query(value="{ 'processDefinitionKey' : { $in: ?0 }, 'engineProcessInstanceId' : { $in: ?1 }, 'keywords' : { $regex: ?2 }  }", fields="{ 'processDefinitionKey': 1, 'processInstanceId':1, 'engineProcessInstanceId': 1, 'alias':1, 'processInstanceLabel':1 }")
    List<ProcessInstance> findByProcessDefinitionKeyInAndEngineProcessInstanceIdInAndKeyword(Iterable<String> processDefinitionKeys, Iterable<String> engineProcessInstanceIds, String keyword);

}
