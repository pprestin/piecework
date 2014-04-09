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
import piecework.command.*;
import piecework.common.SearchCriteria;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.enumeration.AlarmSeverity;
import piecework.exception.*;
import piecework.form.FormDisposition;
import piecework.form.FormFactory;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.SearchProvider;
import piecework.persistence.TaskProvider;
import piecework.repository.SubmissionRepository;
import piecework.repository.ValidationRepository;
import piecework.security.AccessTracker;
import piecework.security.Sanitizer;
import piecework.service.RequestService;
import piecework.service.TaskService;
import piecework.service.UserInterfaceService;
import piecework.settings.SecuritySettings;
import piecework.settings.UserInterfaceSettings;
import piecework.util.FormUtility;
import piecework.util.SecurityUtility;
import piecework.validation.Validation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author James Renfro
 */
public abstract class AbstractFormResource {

    private static final Logger LOG = Logger.getLogger(AbstractFormResource.class);
    private static final String VERSION = "v1";

    @Autowired
    protected AccessTracker accessTracker;

    @Autowired
    protected CommandFactory commandFactory;

    @Autowired
    private FormFactory formFactory;

    @Autowired
    protected IdentityHelper helper;

    @Autowired
    protected ModelProviderFactory modelProviderFactory;

    @Autowired
    private RequestService requestService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    protected Sanitizer sanitizer;

    @Autowired
    private SecuritySettings securitySettings;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserInterfaceService userInterfaceService;

    @Autowired
    private UserInterfaceSettings settings;

    @Autowired
    private ValidationRepository validationRepository;


    protected abstract boolean isAnonymous();

    protected Response startForm(final MessageContext context, final String rawProcessDefinitionKey, Entity principal) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, principal);
        if (isAnonymous())
            SecurityUtility.verifyProcessAllowsAnonymousSubmission(deploymentProvider);

        FormRequest request = requestService.create(requestDetails, deploymentProvider);
        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;
        return response(deploymentProvider, request, ActionType.CREATE, mediaType, 1);
    }

    protected <P extends ProcessDeploymentProvider> Response requestForm(final MessageContext context, final String rawProcessDefinitionKey, final String rawRequestId, Entity principal) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new NotFoundError();

        FormRequest request = requestService.read(processDefinitionKey, requestDetails, requestId);

        if (request == null)
            throw new NotFoundError();

        P provider = modelProviderFactory.provider(request, principal);

        if (isAnonymous())
            SecurityUtility.verifyProcessAllowsAnonymousSubmission(provider);

        ActionType actionType = request.getAction();

        if (actionType == null || (isAnonymous() && (actionType != ActionType.COMPLETE && actionType != ActionType.REJECT)))
            throw new ForbiddenError();

        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;
        return response(provider, request, actionType, mediaType, 1);
    }

    protected <P extends ProcessDeploymentProvider> Response submissionForm(final MessageContext context, final String rawProcessDefinitionKey, final String rawSubmissionId, Entity principal) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String submissionId = sanitizer.sanitize(rawSubmissionId);

        Submission submission = submissionRepository.findOne(submissionId);

        if (submission == null)
            throw new NotFoundError();

        String requestId = submission.getRequestId();

        if (StringUtils.isEmpty(requestId))
            throw new NotFoundError();

        FormRequest request = requestService.read(processDefinitionKey, requestDetails, requestId);

        if (request == null)
            throw new NotFoundError();

        P provider = modelProviderFactory.provider(request, principal);

        if (isAnonymous())
            SecurityUtility.verifyProcessAllowsAnonymousSubmission(provider);

        ActionType actionType = request.getAction();

        if (actionType == null || (isAnonymous() && (actionType != ActionType.COMPLETE && actionType != ActionType.REJECT)))
            throw new ForbiddenError();

        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;
        boolean isJSON = mediaType != null && mediaType.equals(MediaType.APPLICATION_JSON_TYPE);

        try {
            // When returning the JSON for a submission it's necessary to rerun the validation
            SubmissionValidationCommand<P> validationCommand = commandFactory.submissionValidation(provider, request, actionType, submission, VERSION, isJSON);
            Validation validation = validationCommand.execute();

            return response(provider, request, actionType, mediaType, validation, null, true, 1);
        } catch (Exception e) {
            return handleSubmissionException(provider, requestDetails, mediaType, e, false);
        }
    }

    protected Response taskForm(final MessageContext context, final String rawProcessDefinitionKey, final String rawTaskId, final int count, Entity principal) throws PieceworkException {
        if (isAnonymous()) {
            String message = "Someone is attempting to view a task form through an anonymous resource. This is never allowed.";
            LOG.error(message);
            accessTracker.alarm(AlarmSeverity.URGENT, message);
            throw new ForbiddenError();
        }

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        TaskProvider taskProvider = modelProviderFactory.taskProvider(rawProcessDefinitionKey, rawTaskId, principal);

        Task task = taskProvider.task();
        if (task == null)
            throw new ForbiddenError(Constants.ExceptionCodes.task_does_not_exist);

        SecurityUtility.verifyEntityIsAuthorized(taskProvider.process(), task, principal);

        FormRequest request = requestService.create(requestDetails, taskProvider, ActionType.CREATE);

        List<MediaType> mediaTypes = context != null ? context.getHttpHeaders().getAcceptableMediaTypes() : Collections.<MediaType>emptyList();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;

        return response(taskProvider, request, ActionType.CREATE, mediaType, count);
    }

    protected SearchResponse search(final MessageContext context, final SearchCriteria criteria, Entity principal) throws PieceworkException {
        if (isAnonymous() || principal == null) {
            String message = "Someone is attempting to view a list of forms through an anonymous resource. This is never allowed.";
            LOG.error(message);
            accessTracker.alarm(AlarmSeverity.URGENT, message);
            throw new ForbiddenError();
        }

        List<MediaType> mediaTypes = context != null ? context.getHttpHeaders().getAcceptableMediaTypes() : Collections.<MediaType>emptyList();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;

        SearchProvider searchProvider = modelProviderFactory.searchProvider(principal);
        return searchProvider.forms(criteria, new ViewContext(settings, VERSION), mediaType.equals(MediaType.TEXT_HTML_TYPE));
    }

    protected <T, P extends ProcessDeploymentProvider> Response submitForm(final MessageContext context, final String rawProcessDefinitionKey, final String rawRequestId, final T data, final Class<T> type, Entity principal) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest request = requestService.read(processDefinitionKey, requestDetails, requestId);

        if (request == null)
            throw new NotFoundError();

        P provider = modelProviderFactory.provider(request, principal);

        if (isAnonymous() || principal == null)
            SecurityUtility.verifyProcessAllowsAnonymousSubmission(provider);

        List<MediaType> mediaTypes = context != null ? context.getHttpHeaders().getAcceptableMediaTypes() : Collections.<MediaType>emptyList();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;

        try {
            ValidationCommand<P> validationCommand = commandFactory.validation(provider, request, ActionType.UNDEFINED, data, type, VERSION);
            Validation validation = validationCommand.execute();
            SubmitFormCommand<P> submitFormCommand = commandFactory.submitForm(provider, validation, request.getAction(), requestDetails, request);
            SubmissionCommandResponse submissionCommandResponse = submitFormCommand.execute();

            if (isAnonymous())
                return response(submissionCommandResponse.getModelProvider(), submissionCommandResponse.getNextRequest(), ActionType.COMPLETE, MediaType.TEXT_HTML_TYPE, null, null, true, 1);

            if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE))
                return response(submissionCommandResponse.getModelProvider(), submissionCommandResponse.getNextRequest(), submissionCommandResponse.getNextRequest().getAction(), mediaType, 1);

            return redirect(submissionCommandResponse.getModelProvider(), submissionCommandResponse, validation, false);
        } catch (Exception e) {
            return handleSubmissionException(provider, requestDetails, mediaType, e, true);
        }
    }

    protected <P extends ProcessDeploymentProvider> Response validationForm(final MessageContext context, final String rawProcessDefinitionKey, final String rawValidationId, Entity principal) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String validationId = sanitizer.sanitize(rawValidationId);

        Validation validation = validationRepository.findOne(validationId);

        if (validation == null)
            throw new NotFoundError();

        Submission submission = validation.getSubmission();
        String requestId = submission.getRequestId();

        if (StringUtils.isEmpty(requestId))
            throw new NotFoundError();

        FormRequest request = requestService.read(processDefinitionKey, requestDetails, requestId);

        if (request == null)
            throw new NotFoundError();

        P provider = modelProviderFactory.provider(request, principal);

        if (isAnonymous())
            SecurityUtility.verifyProcessAllowsAnonymousSubmission(provider);

        ActionType actionType = request.getAction();

        if (actionType == null || (isAnonymous() && (actionType != ActionType.COMPLETE && actionType != ActionType.REJECT)))
            throw new ForbiddenError();

        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;
        return response(provider, request, actionType, mediaType, validation, null, true, 1);
    }

    private <P extends ProcessDeploymentProvider> Response handleSubmissionException(P provider, RequestDetails requestDetails, MediaType mediaType, Exception e, boolean allowRedirect) throws PieceworkException {
        Validation validation = null;
        Explanation explanation = null;

        if (e instanceof BadRequestError) {
            BadRequestError badRequestError = BadRequestError.class.cast(e);
            validation = badRequestError.getValidation();
            badRequestError.setModelProvider(provider);
        } else if (e instanceof StatusCodeError) {
            StatusCodeError error = StatusCodeError.class.cast(e);
            error.setModelProvider(provider);
            int statusCode = error.getStatusCode();
            explanation = ErrorResponseBuilder.buildExplanation(statusCode, error.getLocalizedMessage(), error.getMessageDetail());
            LOG.warn("Caught a status code error", e);
        } else {
            String detail = e.getMessage() != null ? e.getMessage() : "";
            explanation = new Explanation();
            explanation.setMessage("Unable to complete task");
            explanation.setMessageDetail("Caught an unexpected exception trying to process form submission " + detail);
            LOG.warn("Caught an unexpected exception trying to process form submission", e);
        }

        Map<String, List<Message>> results = validation != null ? validation.getResults() : null;

        if (results != null && !results.isEmpty()) {
            for (Map.Entry<String, List<Message>> result : results.entrySet()) {
                LOG.warn("Validation error " + result.getKey() + " : " + result.getValue().iterator().next().getText());
            }
        }


        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE) && e instanceof BadRequestError)
            throw (BadRequestError)e;

        try {
            FormRequest invalidRequest = requestService.create(requestDetails, provider, ActionType.CREATE, validation, explanation);
            if (isAnonymous())
                return response(provider, invalidRequest, ActionType.CREATE, MediaType.TEXT_HTML_TYPE, validation, explanation, true, 1);

            if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE) || !allowRedirect || explanation != null)
                return response(provider, invalidRequest, invalidRequest.getAction(), mediaType, validation, explanation, true, 1);

            if (!allowRedirect)
                return response(provider, invalidRequest, invalidRequest.getAction(), mediaType, validation, explanation, true, 1);

            Submission submission = validation != null ? validation.getSubmission() : null;
            return redirect(provider, new SubmissionCommandResponse(provider, submission, invalidRequest), validation, true);
        } catch (MisconfiguredProcessException mpe) {
            LOG.error("Unable to create new instance because process is misconfigured", mpe);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        }
    }

    protected <P extends ProcessDeploymentProvider> Response validateForm(final MessageContext context, final String rawProcessDefinitionKey, final MultivaluedMap<String, String> formData, final String rawRequestId, final String rawValidationId, final Entity principal) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);
        String validationId = sanitizer.sanitize(rawValidationId);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest request = requestService.read(processDefinitionKey, requestDetails, requestId);

        if (request == null)
            throw new NotFoundError();

        P provider = modelProviderFactory.provider(request, principal);

        if (isAnonymous() || principal == null)
            SecurityUtility.verifyProcessAllowsAnonymousSubmission(provider);

        commandFactory.containerValidation(provider, request, ActionType.VALIDATE, formData, Map.class, validationId, VERSION).execute();

        return FormUtility.noContentResponse(settings, provider, isAnonymous());
    }

    protected <P extends ProcessDeploymentProvider> Response validateForm(final MessageContext context, final String rawProcessDefinitionKey, final MultipartBody body, final String rawRequestId, final String rawValidationId, final Entity principal) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);
        String validationId = sanitizer.sanitize(rawValidationId);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest request = requestService.read(processDefinitionKey, requestDetails, requestId);

        if (request == null)
            throw new NotFoundError();

        P provider = modelProviderFactory.provider(request, principal);

        if (isAnonymous() || principal == null)
            SecurityUtility.verifyProcessAllowsAnonymousSubmission(provider);

        commandFactory.containerValidation(provider, request, ActionType.VALIDATE, body, MultipartBody.class, validationId, VERSION).execute();
        return FormUtility.noContentResponse(settings, provider, isAnonymous());
    }

    private <P extends ProcessDeploymentProvider> Response redirect(final P provider, final SubmissionCommandResponse submissionCommandResponse, Validation validation, final boolean isInvalidSubmission) throws PieceworkException {
        if (submissionCommandResponse.getNextRequest() == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        FormRequest formRequest = submissionCommandResponse.getNextRequest();
        Submission submission = submissionCommandResponse.getSubmission();
        if (isAnonymous())
            throw new ConflictError(Constants.ExceptionCodes.process_is_misconfigured);

        try {
            FormDisposition formDisposition = FormUtility.disposition(provider, provider.activity(), formRequest.getAction(), new ViewContext(settings, VERSION), null);

            if (isInvalidSubmission) {
                if (validation == null)
                    return Response.seeOther(formDisposition.getInvalidPageUri(submission)).build();
                return Response.seeOther(formDisposition.getInvalidPageUri(validationRepository.save(validation))).build();
            }

            return Response.seeOther(formDisposition.getResponsePageUri(formRequest)).build();
        } catch (URISyntaxException use) {
            LOG.error("URISyntaxException serving page", use);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        } catch (MisconfiguredProcessException mpe) {
            LOG.error("Misconfigured process ", mpe);
            throw new ConflictError(Constants.ExceptionCodes.process_is_misconfigured);
        }

    }

    private <P extends ProcessDeploymentProvider> Response response(final P modelProvider, final FormRequest request, final ActionType actionType, final MediaType mediaType, final int count) throws PieceworkException {
        return response(modelProvider, request, actionType, mediaType, null, null, true, count);
    }

    private <P extends ProcessDeploymentProvider> Response response(final P modelProvider, final FormRequest request, final ActionType actionType, final MediaType mediaType, final Validation validation, final Explanation explanation, final boolean includeRestrictedData, int count) throws PieceworkException {
        Process process = modelProvider.process();
        if (!request.validate(process))
            throw new BadRequestError();

        try {
            Form form = formFactory.form(modelProvider, request, actionType, validation, explanation, includeRestrictedData, isAnonymous(), VERSION);
            FormDisposition formDisposition = form.getDisposition();

            if (mediaType == null || mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                switch (formDisposition.getType()) {
                    case REMOTE:
                        URI pageUri = formDisposition.getPageUri(request, validation, explanation, count);
                        if (pageUri == null && formDisposition.getHostUri() != null)
                            return Response.temporaryRedirect(formDisposition.getHostUri()).build();
                        if (pageUri != null)
                            return Response.seeOther(pageUri).build();
                    case CUSTOM:
                        return FormUtility.okResponse(settings, modelProvider, userInterfaceService.getCustomPageAsStreaming(modelProvider, form), MediaType.TEXT_HTML_TYPE.toString(), isAnonymous());
                }
            }

            return FormUtility.okResponse(settings, modelProvider, form, mediaType.toString(), isAnonymous());

        } catch (URISyntaxException use) {
            LOG.error("URISyntaxException serving page", use);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        } catch (IOException ioe) {
            LOG.error("IOException serving page", ioe);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        }
    }

}
