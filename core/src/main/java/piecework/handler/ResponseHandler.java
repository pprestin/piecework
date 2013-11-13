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
package piecework.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.common.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.form.FormFactory;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityHelper;
import piecework.validation.FormValidation;
import piecework.model.*;
import piecework.model.Process;
import piecework.ui.StreamingPageContent;
import piecework.persistence.ContentRepository;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;


/**
 * @author James Renfro
 */
@Service
public class ResponseHandler {

    private static final Logger LOG = Logger.getLogger(ResponseHandler.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    FormFactory formFactory;

    @Autowired
    IdentityHelper helper;

    @Autowired
    Versions versions;

    public Response handle(RequestDetails requestDetails, FormRequest formRequest, Process process) throws StatusCodeError {
        return handle(requestDetails, formRequest, process, formRequest.getInstance(), formRequest.getTask(), null);
    }

    public Response handle(RequestDetails requestDetails, FormRequest formRequest, Process process, ProcessInstance instance, Task task, FormValidation validation) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        ActionType actionType = formRequest.getAction() != null ? formRequest.getAction() : ActionType.CREATE;
        Activity activity = formRequest.getActivity();
        Action action = activity.action(actionType);

        Form form = formFactory.form(formRequest, process, instance, task, validation, actionType, principal);

        if (form != null && action != null && form.getContainer() != null && !form.getContainer().isReadonly()) {

            List<MediaType> acceptableMediaTypes = requestDetails.getAcceptableMediaTypes();
            boolean isJavascript = acceptableMediaTypes.size() == 1 && acceptableMediaTypes.contains(new MediaType("text", "javascript"));

            if (StringUtils.isNotEmpty(action.getLocation()) && !isJavascript) {
                String location = action.getLocation();

                if (location.startsWith("https://")) {
                    if (location.contains("{formRequestId}") && task != null)
                        location = location.replace("{formRequestId}", task.getTaskInstanceId());
                    return Response.seeOther(URI.create(location)).build();
                }

                ProcessDeployment deployment = process.getDeployment();
                if (deployment == null)
                    throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

                location = deployment.getBase() + "/" + location;

                Content content = content(location, acceptableMediaTypes);
                return Response.ok(new StreamingPageContent(process, form, content, action.getStrategy()), content.getContentType()).build();
            }
        }

        return Response.ok(form).build();
    }

    public Response redirect(FormRequest formRequest) throws StatusCodeError {
        return Response.status(Response.Status.SEE_OTHER).header(HttpHeaders.LOCATION, versions.getVersion1().getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME, formRequest.getProcessDefinitionKey(), formRequest.getRequestId())).build();
    }

    public Content content(String location, List<MediaType> acceptableMediaTypes) throws StatusCodeError {
        // If the location is not blank then retrieve from that location
        Content content;
        if (location.startsWith("classpath:")) {
            String classpathLocation = location.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(classpathLocation);
            try {
                String contentType = null;
                if (!resource.exists() && acceptableMediaTypes.size() == 1) {
                    MediaType mediaType = acceptableMediaTypes.get(0);
                    contentType = mediaType.toString();
                    if (contentType.equals("text/javascript"))
                        resource = new ClassPathResource(classpathLocation + ".js");
                    else if (contentType.equals("text/css"))
                        resource = new ClassPathResource(classpathLocation + ".css");
                }

                BufferedInputStream inputStream = new BufferedInputStream(resource.getInputStream());
                if (contentType == null)
                    contentType = URLConnection.guessContentTypeFromStream(inputStream);
                if (contentType == null) {
                    if (location.endsWith(".css"))
                        contentType = "text/css";
                    else if (location.endsWith(".js"))
                        contentType = "application/json";
                    else
                        contentType = "text/html";
                }
                content = new Content.Builder().inputStream(inputStream).contentType(contentType).build();
            } catch (IOException e) {
                LOG.error("Could not retrieve content", e);
                throw new InternalServerError();
            }
        } else if (location.startsWith("file:")) {
            FileSystemResource resource = new FileSystemResource(location.substring("file:".length()));
            try {
                BufferedInputStream inputStream = new BufferedInputStream(resource.getInputStream());
                String contentType = URLConnection.guessContentTypeFromStream(inputStream);
                if (contentType == null) {
                    if (location.endsWith(".css"))
                        contentType = "text/css";
                    else if (location.endsWith(".js"))
                        contentType = "application/json";
                    else
                        contentType = "text/html";
                }
                content = new Content.Builder().inputStream(inputStream).contentType(contentType).build();
            } catch (IOException e) {
                LOG.error("Unable to retrieve content input stream", e);
                throw new InternalServerError();
            }
        } else {
            content = contentRepository.findByLocation(location);
        }
        return content;
    }

}
