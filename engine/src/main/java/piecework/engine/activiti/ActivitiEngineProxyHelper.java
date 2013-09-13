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
package piecework.engine.activiti;

import com.google.common.collect.Sets;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ManyMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class ActivitiEngineProxyHelper {

    @Autowired
    ProcessEngine processEngine;

    @Cacheable("processDefinitionIds")
    public Set<String> getProcessDefinitionIds(String ... keys) {
        Set<String> keySet = Sets.newHashSet(keys);
        Set<String> set = new HashSet<String>();
        for (ProcessDefinition processDefinition : processEngine.getRepositoryService().createProcessDefinitionQuery().list()) {
            if (keySet.contains(processDefinition.getKey()))
                set.add(processDefinition.getId());
        }

        return Collections.unmodifiableSet(set);
    }

//    @Cacheable("processDefinitionKeys")
//    private Set<String> getProcessDefinitionKeys(Set<Process> processes) {
//        Set<String> set = Sets.newHashSet();
//        for (Process process : processes) {
//            if (process.getEngine() != null && getKey() != null && process.getEngine().equals(getKey()) && StringUtils.isNotEmpty(process.getProcessDefinitionKey()))
//                set.add(process.getProcessDefinitionKey());
//        }
//        return set;
//    }

    @Cacheable("processDefinitionIdMap")
    public ManyMap<String, Process> getProcessDefinitionIdMap(Set<Process> processes) {
        ManyMap<String, Process> processDefinitionKeyMap = new ManyMap<String, Process>();
        for (Process process : processes) {
            if (process.getEngine() != null && getKey() != null && process.getEngine().equals(getKey()))
                processDefinitionKeyMap.putOne(process.getEngineProcessDefinitionKey(), process);
        }

        ManyMap<String, Process> map = new ManyMap<String, Process>();
        for (ProcessDefinition processDefinition : processEngine.getRepositoryService().createProcessDefinitionQuery().list()) {
            List<Process> matchingProcesses = processDefinitionKeyMap.get(processDefinition.getKey());
            if (matchingProcesses != null && !matchingProcesses.isEmpty())
                map.put(processDefinition.getId(), matchingProcesses);
        }

        return map;
    }

    public String getKey() {
        return "activiti";
    }
}
