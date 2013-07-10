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
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James Renfro
 */
@XmlRootElement(name = QueryParameter.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = QueryParameter.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryParameter {

    @XmlElement
    private final String name;

    @XmlElementWrapper(name="values")
    @XmlElement
    private final List<String> values;

    private QueryParameter() {
        this(new QueryParameter.Builder(), new ViewContext());
    }

    private QueryParameter(QueryParameter.Builder builder, ViewContext context) {
        this.name = builder.name;
        this.values = builder.values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }

    public final static class Builder {

        private String name;
        private List<String> values;

        public Builder() {
            super();
        }

        public Builder(QueryParameter parameter, Sanitizer sanitizer) {
            this.name = sanitizer.sanitize(parameter.name);

            if (parameter.values != null && !parameter.values.isEmpty()) {
                this.values = new ArrayList<String>(parameter.values.size());
                for (String value : parameter.values) {
                    this.values.add(sanitizer.sanitize(value));
                }
            }
        }

        public QueryParameter build() {
            return new QueryParameter(this, null);
        }

        public QueryParameter build(ViewContext context) {
            return new QueryParameter(this, context);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            if (this.values == null)
                this.values = new ArrayList<String>();
            this.values.add(value);
            return this;
        }

        public Builder values(String ... values) {
            if (this.values == null)
                this.values = new ArrayList<String>();
            if (values.length > 0)
                this.values.addAll(Arrays.asList(values));
            return this;
        }

        public Builder values(List<String> values) {
            if (this.values == null)
                this.values = new ArrayList<String>();
            if (values != null)
                this.values.addAll(values);
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Query Parameter";
        public static final String ROOT_ELEMENT_NAME = "parameter";
        public static final String TYPE_NAME = "QueryParameterType";
    }
}
