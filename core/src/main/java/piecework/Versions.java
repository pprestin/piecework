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
package piecework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.common.ViewContext;

import javax.annotation.PostConstruct;

/**
 * @author James Renfro
 */
@Service
public class Versions {

    @Autowired
    Environment environment;

    private String hostUri;
    private String baseApplicationUri;
    private String basePublicUri;
    private String baseServiceUri;

    @PostConstruct
    public void initialize() {
        this.hostUri = environment.getProperty("host.uri");
        this.baseApplicationUri = environment.getProperty("base.application.uri");
        this.baseServiceUri = environment.getProperty("base.service.uri");
        this.basePublicUri = environment.getProperty("base.public.uri");
    }

    public ViewContext getVersion1() {
        return new ViewContext(hostUri, baseApplicationUri, baseServiceUri, basePublicUri, "v1");
    }

}
