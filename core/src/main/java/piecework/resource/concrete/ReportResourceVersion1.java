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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.exception.InternalServerError;
import piecework.identity.IdentityHelper;
import piecework.model.Process;
import piecework.exception.PieceworkException;
import piecework.model.Report;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessProvider;
import piecework.resource.ReportResource;
import piecework.security.Sanitizer;
import piecework.service.ProcessService;
import piecework.service.ReportService;
import piecework.service.UserInterfaceService;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author James Renfro
 */
@Service
public class ReportResourceVersion1 implements ReportResource {

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ReportService reportService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    UserInterfaceService userInterfaceService;

    @Override
    public Response read() throws PieceworkException {
        try {
            Report report = new Report(null, null);
            return Response.ok(userInterfaceService.getDefaultPageAsStreaming(Report.class, report), MediaType.TEXT_HTML_TYPE).build();
        } catch (IOException ioe) {
            throw new InternalServerError();
        }
    }

    @Override
    public Response read(MessageContext context, String rawProcessDefinitionKey, String rawReportName) throws PieceworkException {
        String reportName = sanitizer.sanitize(rawReportName);
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        return Response.ok(reportService.getReport(processProvider, reportName)).build();
    }
}
