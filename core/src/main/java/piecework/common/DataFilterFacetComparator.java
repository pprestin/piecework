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
import piecework.model.Facet;
import piecework.model.User;

import java.util.Comparator;
import java.util.Map;

/**
 * @author James Renfro
 */
public class DataFilterFacetComparator implements Comparator<Map<String, Object>> {

    private final String name;
    private final String type;

    public DataFilterFacetComparator(Facet facet) {
        this.name = facet.getName();
        this.type = facet.getType();
    }

    @Override
    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        Object f1 = o1.get(name);
        Object f2 = o2.get(name);

        if (f1 == null && f2 == null)
            return 0;
        if (f1 == null)
            return 1;
        if (f2 == null)
            return -1;

        if (type != null && type.equals("user")) {
            if (f1 instanceof User && f2 instanceof User) {
                User u1 = User.class.cast(f1);
                User u2 = User.class.cast(f2);

                if (StringUtils.isNotEmpty(u1.getDisplayName()) && StringUtils.isNotEmpty(u2.getDisplayName()))
                    return u1.getDisplayName().compareTo(u2.getDisplayName());
            }
        }

        if (f1 instanceof Comparable && f2 instanceof Comparable)
            return Comparable.class.cast(f1).compareTo(f2);

        return 0;
    }

}
