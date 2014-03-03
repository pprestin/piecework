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
package piecework.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import piecework.ui.CustomJaxbJsonProvider;
import piecework.ui.visitor.HtmlProvider;

/**
 * @author James Renfro
 */
@Configuration
public class ProviderConfiguration {

    @Bean
    public CustomJaxbJsonProvider customJaxbJsonProvider() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return new CustomJaxbJsonProvider(new JacksonJaxbJsonProvider(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS));
    }

    @Bean
    public HtmlProvider htmlProvider() {
        return new HtmlProvider();
    }

}
