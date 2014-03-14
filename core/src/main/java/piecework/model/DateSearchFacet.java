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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.data.mongodb.core.query.Criteria;
import piecework.common.DateRange;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class DateSearchFacet extends SearchFacet<DateRange> {
    private static final Logger LOG = Logger.getLogger(DateSearchFacet.class);

    public DateSearchFacet() {
        this(null, null, null, null, false);
    }

    public DateSearchFacet(String query, String name, String label, String type, boolean required) {
        super(query, name, label, type, required);
    }

    public Criteria criteria(DateRange value) {
        Criteria criteria = where(query);
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeParser();
        try {
            if (StringUtils.isNotEmpty(value.getAfter())) {
                DateTime dateTime = dateTimeFormatter.parseDateTime(value.getAfter());
                criteria.gt(dateTime.toDate());
            }
            if (StringUtils.isNotEmpty(value.getBefore())) {
                DateTime dateTime = dateTimeFormatter.parseDateTime(value.getBefore());
                criteria.lt(dateTime.toDate());
            }
        } catch (Exception e) {
            LOG.warn("Unable to parse " + value + " as a datetime object", e);
        }

        return criteria;
    }

}
