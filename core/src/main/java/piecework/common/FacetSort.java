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
import org.springframework.data.domain.Sort;
import piecework.model.Facet;
import piecework.util.SearchUtility;

/**
 * @author James Renfro
 */
public class FacetSort {

    private final Facet facet;
    private final Sort.Direction direction;

    public FacetSort(Facet facet, String direction) {
        this.facet = facet;
        if (StringUtils.isNotEmpty(direction) && (direction.equalsIgnoreCase("asc") ||
                        direction.equalsIgnoreCase("desc")))
            this.direction = Sort.Direction.fromString(direction);
        else
            this.direction = Sort.Direction.DESC;
    }

    public Facet getFacet() {
        return facet;
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    public String toString() {
        return facet.getName() + ":" + direction.toString().toLowerCase();
    }
}
