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
package piecework.form;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.Versions;
import piecework.enumeration.ActionType;
import piecework.exception.FormBuildingException;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.DataFilterService;

import javax.ws.rs.core.MediaType;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class FormFactoryTest {

    @InjectMocks
    FormFactory formFactory;

    @Mock
    DataFilterService dataFilterService;

    @Mock
    Process process;

    @Mock
    ProcessDeployment deployment;

    @Mock
    Versions versions;

    @Mock
    Entity user;


    @Test
    public void testFormInitial() throws FormBuildingException {
        FormRequest request = new FormRequest.Builder().build();

        Form form = formFactory.form(process, deployment, request, ActionType.CREATE, user, MediaType.TEXT_HTML_TYPE, null, null, false);

        Assert.assertNotNull(form);
//        Assert.assertEquals("First screen", form.getScreen().getTitle());
//        Assert.assertEquals(1, form.getScreen().getGroupings().size());
//        Assert.assertEquals(1, form.getScreen().getSections().size());
    }

}
