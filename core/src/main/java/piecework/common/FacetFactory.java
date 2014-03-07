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
package piecework.common;

import piecework.model.Facet;
import piecework.model.Process;
import piecework.model.SearchFacet;
import piecework.model.User;

import java.util.*;

/**
 * @author James Renfro
 */
public class FacetFactory {

    public static Map<String, Facet> facetMap(Set<Process> processes) {
        Map<String, Facet> facetMap = new HashMap<String, Facet>();
        for (Facet facet : facets(processes)) {
            facetMap.put(facet.getName(), facet);
        }
        return facetMap;
    }

    public static List<Facet> facets(Set<Process> processes) {
        // The system has a default set of facets that is not process-dependent
        List<Facet> facets = new ArrayList<Facet>();
        facets.add(new SearchFacet("processInstanceLabel", "InstanceLabel", "Label"));
        facets.add(new SearchFacet("processStatus", "ProcessStatus", "Process Status"));
//        facets.add(new Facet("tasks..taskStatus", "TaskStatus", "Task Status"));
//        facets.add(new Facet("tasks..taskLabel", "TaskLabel", "Task"));
//        facets.add(new Facet("tasks..assignee", "Assignee", "Assignee", User.class));
//        facets.add(new Facet("tasks..startTime", "TaskStart", "Start Time", Date.class));
//        facets.add(new Facet("tasks..endTime", "TaskEnd", "End Time", Date.class));
//        facets.add(new Facet("tasks..dueDate", "TaskDue", "Task Due", Date.class));
        facets.add(new SearchFacet("startTime", "InstanceStartTime", "Created", Date.class));
        facets.add(new SearchFacet("lastModifiedTime", "InstanceLastModified", "Last Modified", Date.class));
        facets.add(new SearchFacet("endTime", "InstanceEndTime", "Completed", Date.class));

        return facets;
    }

}
