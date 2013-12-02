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
package piecework.resource.concrete;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.exception.StatusCodeError;
import piecework.form.AnonymousScriptResource;
import piecework.service.FormTemplateService;

import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Service
public class AnonymousScriptResourceVersion1 extends AbstractScriptResource implements AnonymousScriptResource {

    private static final Logger LOG = Logger.getLogger(AnonymousScriptResourceVersion1.class);

    @Autowired
    private FormTemplateService formTemplateService;

    @Override
    public Response readScript(String scriptId) throws StatusCodeError {
        String templateName = formTemplateService.getTemplateName(scriptId, isAnonymous());

        Resource scriptResource = userInterfaceService.getScriptResource(templateName);
        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(String stylesheetId) throws StatusCodeError {
        String templateName = formTemplateService.getTemplateName(stylesheetId, isAnonymous());

        Resource stylesheetResource = userInterfaceService.getStylesheetResource(templateName);
        return response(stylesheetResource, "text/css");
    }

    @Override
    protected boolean isAnonymous() {
        return true;
    }
}
