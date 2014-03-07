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
package piecework.model;

import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class SearchFacet extends Facet {

    private final String query;

    public SearchFacet(String query, String name, String label) {
        this(query, name, label, "string");
    }

    public SearchFacet(String query, String name, String label, String type) {
        super(name, label, type);
        this.query = query;
    }

    public Criteria criteria(String value) {
        return where(query).is(value);
    }

    public String getQuery() {
        return query;
    }

}
