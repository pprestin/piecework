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
package piecework.authorization;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import piecework.model.Process;
import piecework.persistence.ProcessRepository;
import piecework.service.ProcessService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
public class SuperUserAccessAuthority extends AccessAuthority {

    private final List<Process> processes;

    public SuperUserAccessAuthority(ProcessService processService) {
        super();
        if (processService != null)
            this.processes = new ArrayList<Process>(processService.findAllProcesses());
        else
            this.processes = new ArrayList<Process>();
    }

    @Override
    public boolean hasGroup(Set<String> allowedGroupIds) {
        return true;
    }

    @Override
    public boolean hasRole(Process process, Set<String> allowedRoleSet) {
        return true;
    }

    @Override
    public boolean isAuthorized(String roleAllowed, String processDefinitionKeyAllowed) {
        return true;
    }

    @Override
    public Set<String> getProcessDefinitionKeys(Set<String> allowedRoleSet) {
        Set<String> processDefinitionKeys = new HashSet<String>();
        for (Process process : processes) {
            processDefinitionKeys.add(process.getProcessDefinitionKey());
        }
        return processDefinitionKeys;
    }

    @Override
    public String toString() {
        return "";
    }
}
