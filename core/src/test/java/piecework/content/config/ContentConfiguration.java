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
package piecework.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import piecework.content.ContentHandlerRepository;
import piecework.content.concrete.InMemoryContentProviderReceiver;
import piecework.content.stubs.*;

/**
 * @author James Renfro
 */
@Configuration
public class ContentConfiguration {

    @Bean
    public ContentHandlerRepository contentHandlerRepository() {
        return new ContentHandlerRepository();
    }

    @Bean
    public InMemoryContentProviderReceiver inMemoryContentProviderReceiver() {
        return new InMemoryContentProviderReceiver();
    }

    @Bean
    public TestContentProviderVoter testContentProviderVoter() {
        return new TestContentProviderVoter();
    }

    @Bean
    public TestContentReceiverVoter testContentReceiverVoter() {
        return new TestContentReceiverVoter();
    }

    @Bean
    public TestExternalContentProvider testExternalContentProvider() {
        return new TestExternalContentProvider();
    }

    @Bean
    public TestExternalContentReceiver testExternalContentReceiver() {
        return new TestExternalContentReceiver();
    }

    @Bean
    public TestKeyContentProvider testKeyContentProvider() {
        return new TestKeyContentProvider();
    }

    @Bean
    public TestKeyContentReceiver testKeyContentReceiver() {
        return new TestKeyContentReceiver();
    }

}
