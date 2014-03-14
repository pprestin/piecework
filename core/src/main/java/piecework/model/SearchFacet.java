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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class SearchFacet<T> extends Facet {
    private static final Logger LOG = Logger.getLogger(SearchFacet.class);
    protected final String query;

    public SearchFacet(String query, String name, String label, boolean required) {
        this(query, name, label, "string", required);
    }

    public SearchFacet(String query, String name, String label, String type, boolean required) {
        super(name, label, type, required);
        this.query = query;
    }

    public Criteria criteria(T object) {
        String value = object.toString();

        Criteria criteria = where(query);
        List<String> tokens = new ArrayList<String>();
        String resultString = value.replaceAll("[^\\p{L}\\p{Nd}\\-]", ",");
        String[] components = resultString.split(",");
        if (components != null && components.length > 0)
            tokens.addAll(Arrays.asList(components));

        int count = 0;
        List<Pattern> dbObjects = new ArrayList<Pattern>();
        for (String token : tokens) {
            dbObjects.add(Pattern.compile(token, Pattern.CASE_INSENSITIVE));
            count++;
        }
        if (count > 0)
            criteria.all(dbObjects);
        return criteria;
    }

    public String getQuery() {
        return query;
    }

}
