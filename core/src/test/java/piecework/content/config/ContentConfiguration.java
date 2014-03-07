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

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import piecework.content.ContentHandlerRepository;
import piecework.content.concrete.InMemoryContentProviderReceiver;
import piecework.content.stubs.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Configuration
public class ContentConfiguration {

    @Bean
    public TestExternalResource mockVendorResource() {
        return new TestExternalResource();
    }

    @Bean
    public Server mockRemoteServer(TestExternalResource testExternalResource) throws Exception {
        Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
        extensionMappings.put("json", "application/json");
        extensionMappings.put("xml", "application/xml");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setServiceBean(testExternalResource);
        sf.setAddress("http://localhost:10001/external");
        sf.setExtensionMappings(extensionMappings);

        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(sf.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
        return sf.create();
    }

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
