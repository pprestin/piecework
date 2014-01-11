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

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import piecework.Constants;
import piecework.Versions;
import piecework.command.CommandFactory;
import piecework.form.FormDisposition;
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.form.FormFactory;
import piecework.persistence.ProcessInstanceRepository;
import piecework.persistence.SubmissionRepository;
import piecework.security.AccessTracker;
import piecework.service.*;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;
import piecework.security.SecuritySettings;
import piecework.util.TaskUtility;
import piecework.validation.Validation;
import piecework.validation.ValidationFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;


/**
 * @author James Renfro
 */
public abstract class AbstractFormResource {

    private static final Logger LOG = Logger.getLogger(AbstractFormResource.class);

    @Autowired
    AccessTracker accessTracker;

    @Autowired
    CommandFactory commandFactory;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    FormFactory formFactory;

    @Autowired
    FormService formService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    RequestService requestService;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    protected Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    UserInterfaceService userInterfaceService;

    @Autowired
    Versions versions;

    protected abstract boolean isAnonymous();

    protected Response startForm(MessageContext context, Process process) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, isAnonymous());
        FormRequest request = requestService.create(requestDetails, process);
        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;
        return response(process, request, ActionType.CREATE, mediaType);
    }

//    protected Response receiptForm(MessageContext context, Process process, String rawRequestId) throws PieceworkException {
//        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
//        accessTracker.track(requestDetails, false, isAnonymous());
//        String requestId = sanitizer.sanitize(rawRequestId);
//        FormRequest request = requestService.read(requestDetails, requestId);
//
//        ActionType actionType = request.getAction();
//
//        if (actionType == null || (isAnonymous() && (actionType != ActionType.COMPLETE && actionType != ActionType.REJECT)))
//            throw new ForbiddenError();
//
//        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
//        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;
//        return response(process, request, actionType, mediaType);
//    }

    protected Response submissionForm(MessageContext context, Process process, String rawSubmissionId) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, isAnonymous());
        String submissionId = sanitizer.sanitize(rawSubmissionId);

        Submission submission = submissionRepository.findOne(submissionId);

        if (submission == null)
            throw new NotFoundError();

        String requestId = submission.getRequestId();

        if (StringUtils.isEmpty(requestId))
            throw new NotFoundError();

        FormRequest request = requestService.read(requestDetails, requestId);

        if (request == null)
            throw new NotFoundError();

        ActionType actionType = request.getAction();

        if (actionType == null || (isAnonymous() && (actionType != ActionType.COMPLETE && actionType != ActionType.REJECT)))
            throw new ForbiddenError();


        // When returning the JSON for a submission it's necessary to rerun the validation
        Validation validation = commandFactory.validation(process, process.getDeployment(), request, submission, helper.getPrincipal()).execute();

        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;
        return response(process, request, actionType, mediaType, validation, null, true);
    }

    protected Response taskForm(MessageContext context, Process process, String rawTaskId) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, isAnonymous());
        String taskId = sanitizer.sanitize(rawTaskId);
        Entity principal = helper.getPrincipal();
        FormRequest request = requestService.create(principal, requestDetails, process, taskId, null);

        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;

        return response(process, request, ActionType.CREATE, mediaType);
    }

    protected SearchResults search(MessageContext context, MultivaluedMap<String, String> rawQueryParameters) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, isAnonymous());
        Entity principal = helper.getPrincipal();
        return formService.search(rawQueryParameters, principal);
    }

//    protected Response saveForm(MessageContext context, Process process, String rawRequestId, MultipartBody body) throws PieceworkException {
//        String requestId = sanitizer.sanitize(rawRequestId);
//
//        if (StringUtils.isEmpty(requestId))
//            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);
//
//        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
//        accessTracker.track(requestDetails, true, isAnonymous());
//        FormRequest formRequest = requestService.read(requestDetails, requestId);
//        if (formRequest == null) {
//            LOG.error("Forbidden: Attempting to save a form for a request id that doesn't exist");
//            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
//        }
//        try {
//            return redirect(formRequest, formService.save(process, requestDetails, requestId, body, MultipartBody.class, helper.getPrincipal()));
//        } catch (MisconfiguredProcessException mpe) {
//            LOG.error("Unable to create new instance because process is misconfigured", mpe);
//            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
//        }
//    }

    protected Response submitForm(MessageContext context, Process process, String rawRequestId, MultivaluedMap<String, String> formData) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, isAnonymous());
        FormRequest formRequest = requestService.read(requestDetails, requestId);
        if (formRequest == null) {
            LOG.error("Forbidden: Attempting to submit a form for a request id that doesn't exist");
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        try {
            Submission submission = formService.submit(process, requestDetails, requestId, formData, Map.class, helper.getPrincipal());
//            FormRequest receiptRequest = formService.submit(process, requestDetails, requestId, formData, Map.class, helper.getPrincipal());

            // Anonymous form submissions are not redirected, since it's not possible to authorize access to the receipt form
            // so for security reasons the response is forced to be a COMPLETE action and no restricted data can be returned
            if (isAnonymous())
                return response(process, formRequest, ActionType.COMPLETE, MediaType.TEXT_HTML_TYPE, null, null, false);

            return redirect(formRequest, submission);
        } catch (Exception e) {
            Validation validation = null;
            Explanation explanation = null;

            if (e instanceof BadRequestError)
                validation = ((BadRequestError)e).getValidation();
            else {
                String detail = e.getMessage() != null ? e.getMessage() : "";
                explanation = new Explanation();
                explanation.setMessage("Unable to complete task");
                explanation.setMessageDetail("Caught an unexpected exception trying to process form submission " + detail);
            }

            Map<String, List<Message>> results = validation != null ? validation.getResults() : null;

            if (results != null && !results.isEmpty()) {
                for (Map.Entry<String, List<Message>> result : results.entrySet()) {
                    LOG.warn("Validation error " + result.getKey() + " : " + result.getValue().iterator().next().getText());
                }
            }

            List<MediaType> acceptableMediaTypes = requestDetails.getAcceptableMediaTypes();
            boolean isJSON = acceptableMediaTypes.size() == 1 && acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE);

            if (isJSON && e instanceof BadRequestError)
                throw (BadRequestError)e;

            try {
                FormRequest invalidRequest = requestService.create(requestDetails, process, formRequest.getInstance(), formRequest.getTask(), ActionType.CREATE, validation, explanation);
                if (isAnonymous())
                    return response(process, invalidRequest, ActionType.CREATE, MediaType.TEXT_HTML_TYPE, validation, explanation, true);

                Submission submission = validation != null ? validation.getSubmission() : null;
                return redirect(invalidRequest, submission);
            } catch (MisconfiguredProcessException mpe) {
                LOG.error("Unable to create new instance because process is misconfigured", mpe);
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
        }
    }

    protected Response submitForm(MessageContext context, Process process, String rawRequestId, MultipartBody body) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, isAnonymous());
        FormRequest formRequest = requestService.read(requestDetails, requestId);
        try {
            return redirect(formRequest, formService.submit(process, requestDetails, requestId, body, MultipartBody.class, helper.getPrincipal()));
        } catch (Exception e) {
            Validation validation = null;
            Explanation explanation = null;

            if (e instanceof BadRequestError)
                validation = ((BadRequestError)e).getValidation();
            else {
                String detail = e.getMessage() != null ? e.getMessage() : "";
                explanation = new Explanation();
                explanation.setMessage("Unable to complete task");
                explanation.setMessageDetail("Caught an unexpected exception trying to process form submission " + detail);
            }

            Map<String, List<Message>> results = validation != null ? validation.getResults() : null;

            if (results != null && !results.isEmpty()) {
                for (Map.Entry<String, List<Message>> result : results.entrySet()) {
                    LOG.warn("Validation error " + result.getKey() + " : " + result.getValue().iterator().next().getText());
                }
            }

            List<MediaType> acceptableMediaTypes = requestDetails.getAcceptableMediaTypes();
            boolean isJSON = acceptableMediaTypes.size() == 1 && acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE);

            if (isJSON && e instanceof BadRequestError)
                throw (BadRequestError)e;

            try {
//                FormRequest formRequest = requestService.read(requestDetails, requestId);
                FormRequest invalidRequest = requestService.create(requestDetails, process, formRequest.getInstance(), formRequest.getTask(), ActionType.CREATE, validation, explanation);
//                return response(process, invalidRequest, ActionType.CREATE, MediaType.TEXT_HTML_TYPE, validation, explanation, submission, true);
                if (isAnonymous())
                    return response(process, invalidRequest, ActionType.CREATE, MediaType.TEXT_HTML_TYPE, validation, explanation, true);

                Submission submission = validation != null ? validation.getSubmission() : null;
                return redirect(invalidRequest, submission);
            } catch (MisconfiguredProcessException mpe) {
                LOG.error("Unable to create new instance because process is misconfigured", mpe);
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
        }
    }

    protected Response validateForm(MessageContext context, Process process, MultivaluedMap<String, String> formData, String rawRequestId, String rawValidationId) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        String requestId = sanitizer.sanitize(rawRequestId);
        String validationId = sanitizer.sanitize(rawValidationId);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, isAnonymous());
        formService.validate(process, requestDetails, requestId, formData, Map.class, validationId, principal);
        return Response.noContent().build();
    }

    protected Response validateForm(MessageContext context, Process process, MultipartBody body, String rawRequestId, String rawValidationId) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        String requestId = sanitizer.sanitize(rawRequestId);
        String validationId = sanitizer.sanitize(rawValidationId);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, isAnonymous());
        formService.validate(process, requestDetails, requestId, body, MultipartBody.class, validationId, principal);
        return Response.noContent().build();
    }

    private Response redirect(FormRequest formRequest, Submission submission) throws StatusCodeError {
        if (formRequest == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        String location;

        if (isAnonymous())
            throw new ConflictError(Constants.ExceptionCodes.process_is_misconfigured);
        else if (submission != null)
            location = versions.getVersion1().getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME, formRequest.getProcessDefinitionKey()) + "?submissionId=" + submission.getSubmissionId();
        else
            location = versions.getVersion1().getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME, formRequest.getProcessDefinitionKey()) + "?taskId=" + formRequest.getTaskId();

        return Response.status(Response.Status.SEE_OTHER).header(HttpHeaders.LOCATION, location).build();
    }

    private Response response(Process process, FormRequest request, ActionType actionType, MediaType mediaType) throws StatusCodeError {
        return response(process, request, actionType, mediaType, null, null, true);
    }

    private Response response(Process process, FormRequest request, ActionType actionType, MediaType mediaType, Validation validation, Explanation explanation, boolean includeRestrictedData) throws StatusCodeError {
        if (!request.validate(process))
            throw new BadRequestError();

        Entity principal = helper.getPrincipal();
        try {
            ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
            Form form = formFactory.form(process, deployment, request, actionType, principal, mediaType, validation, explanation, includeRestrictedData, isAnonymous());
            FormDisposition formDisposition = form.getDisposition();

            switch (formDisposition.getType()) {
                case REMOTE:
                    String taskId = request.getTaskId();
                    String query = null;
                    if (explanation == null && StringUtils.isNotEmpty(taskId))
                        query = "taskId=" + taskId;
                    else if (request != null && StringUtils.isNotEmpty(request.getRequestId()))
                        query = "requestId=" + request.getRequestId();

                    URI uri = formDisposition.getUri();
                    uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
                    return Response.seeOther(uri).build();
                case CUSTOM:
                    return Response.ok(userInterfaceService.getCustomPageAsStreaming(process, form), MediaType.TEXT_HTML_TYPE).build();
            }

            return Response.ok(form).build();
        } catch (URISyntaxException use) {
            LOG.error("URISyntaxException serving page", use);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        } catch (IOException ioe) {
            LOG.error("IOException serving page", ioe);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        } catch (MisconfiguredProcessException mpe) {
            LOG.error("Process is misconfigured", mpe);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        } catch (FormBuildingException fbe) {
            LOG.error("Unable to build form", fbe);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        }
    }


}
