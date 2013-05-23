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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import piecework.model.Process;
import piecework.process.ProcessRepository;

import javax.servlet.http.HttpServletRequest;

/**
 * @author James Renfro
 */
public class RequestHandlerTest {

    private static final String EXAMPLE_PROCESS_DEFINITION_KEY = "example";

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    ProcessRepository processRepository;

    HttpServletRequest request;



    @Before
    public void setUp() throws Exception {
        this.request = Mockito.mock(HttpServletRequest.class);



//        Process process = new Process.Builder()
//                .interaction()
//                .build();

    }

    @Test
    public void testCreate() throws Exception {

//        requestHandler.create(request, TEST)

    }

    @Test
    public void testHandle() throws Exception {

    }

}
