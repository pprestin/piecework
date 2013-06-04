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
package piecework.ui;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import piecework.common.model.User;
import piecework.model.ProcessInstance;

/**
 * @author James Renfro
 */
public class PageContextTest {

    @Test
    public void testSerializeToJson() throws Exception {
        ClassPathResource expectedResource = new ClassPathResource("piecework/ui/PageContext.json");

        ProcessInstance processInstance = new ProcessInstance.Builder()
                .processInstanceId("1")
                .processDefinitionKey("Test")
                .processDefinitionLabel("Testing Process")
                .processInstanceLabel("A Simple Test")
                .build();

        PageContext context = new PageContext.Builder()
                .applicationTitle("Test application")
                .pageTitle("Test page")
                .assetsUrl("http://localhost/resources")
                .resource(processInstance)
                .user(new User.Builder().userId("123").visibleId("jtest").displayName("Jill Test").build())
                .build();

        ObjectMapper mapper = new ObjectMapper();

        String expected = IOUtils.toString(expectedResource.getInputStream());
        String actual = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        Assert.assertEquals(expected, actual);
    }


}
