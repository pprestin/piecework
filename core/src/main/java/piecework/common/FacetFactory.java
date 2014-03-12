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

import org.apache.commons.lang.StringUtils;
import piecework.model.*;
import piecework.model.Process;

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
        facets.add(new SearchFacet("processInstanceLabel", "processInstanceLabel", "Label", true));
        facets.add(new DataFilterFacet("taskStatus", "Status", true) {
            @Override
            public boolean include(Task task, String value) {
                String lowercaseValue = value != null ?  value.toLowerCase() : "";
                String taskStatus = task != null && StringUtils.isNotEmpty(task.getTaskStatus()) ? task.getTaskStatus().toLowerCase() : null;
                return taskStatus != null && taskStatus.contains(lowercaseValue);
            }
        });
        facets.add(new DataFilterFacet("taskLabel", "Task", true) {
            @Override
            public boolean include(Task task, String value) {
                String lowercaseValue = value != null ?  value.toLowerCase() : "";
                String taskLabel = task != null && StringUtils.isNotEmpty(task.getTaskLabel()) ? task.getTaskLabel().toLowerCase() : null;
                return taskLabel != null && taskLabel.contains(lowercaseValue);
            }
        });
        facets.add(new DataFilterFacet("assignee", "Assignee", "user", true){
            @Override
            public boolean include(Task task, String value) {
                String lowercaseValue = value != null ?  value.toLowerCase() : "";
                String assigneeDisplayName = task != null && task.getAssignee() != null && StringUtils.isNotEmpty(task.getAssignee().getDisplayName()) ? task.getAssignee().getDisplayName().toLowerCase() : null;
                return assigneeDisplayName != null && assigneeDisplayName.contains(lowercaseValue);
            }
        });
        if (processes != null) {
            for (Process process : processes) {
                if (process.getFacets() != null && !process.getFacets().isEmpty())
                    facets.addAll(process.getFacets());
            }
        }

        facets.add(new SearchFacet("startTime", "startTime", "Created", "date", false));
        facets.add(new SearchFacet("endTime", "endTime", "Completed", "date", false));
        facets.add(new SearchFacet("lastModifiedTime", "lastModifiedTime", "Last Modified", "date", true));

        return facets;
    }

    public static SearchFacet defaultSearch() {
        return new SearchFacet("lastModifiedTime", "lastModifiedTime", "Last Modified", "date", true);
    }

}
