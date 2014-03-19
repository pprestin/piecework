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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.util.Date;

/**
 * @author James Renfro
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DateValue extends Value {

    @XmlAttribute
    private final long ms;

    @XmlValue
    private final DateTime dateTime;

    public DateValue() {
        this(null);
    }

    public DateValue(DateTime value) {
        super(value != null ? value.toString() : null);
        this.ms = value != null ? value.getMillis() : 0l;
        this.dateTime = value;
    }

    public static enum DateValueConverter implements Converter<Date, DateValue> {

        INSTANCE;

        public DateValue convert(Date source) {
            return source == null ? null : new DateValue(new DateTime(source));
        }
    }

}
