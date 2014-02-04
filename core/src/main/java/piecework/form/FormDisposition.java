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

import org.apache.commons.lang.StringUtils;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.model.Process;
import piecework.validation.Validation;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author James Renfro
 */
public class FormDisposition {

    public enum FormDispositionType { DEFAULT, CUSTOM, REMOTE };

    private final FormDispositionType type;
    private final URI hostUri;
    private final URI pageUri;
    private final URI resourceUri;
    private final String base;
    private final String path;
    private final Action action;

    private FormDisposition() {
        this(new Builder());
    }

    private FormDisposition(Builder builder) {
        this.type = builder.type;
        this.hostUri = builder.hostUri;
        this.pageUri = builder.pageUri;
        this.resourceUri = builder.resourceUri;
        this.base = builder.base;
        this.path = builder.path;
        this.action = builder.action;
    }

    public FormDispositionType getType() {
        return type;
    }

    public URI getHostUri() {
        return hostUri;
    }

    public URI getPageUri() {
        return pageUri;
    }

    public URI getResourceUri() {
        return resourceUri;
    }

    public URI getInvalidPageUri(Submission submission) throws URISyntaxException {
        String query = null;
        if (submission != null && StringUtils.isNotEmpty(submission.getSubmissionId()))
            query = "submissionId=" + submission.getSubmissionId();

        return new URI(pageUri.getScheme(), pageUri.getUserInfo(), pageUri.getHost(), pageUri.getPort(), pageUri.getPath(), query, null);
    }

    public URI getResponsePageUri(FormRequest formRequest) throws URISyntaxException {
        String query = null;
        if (formRequest != null && StringUtils.isNotEmpty(formRequest.getRequestId()))
            query = "requestId=" + formRequest.getRequestId();
        return new URI(pageUri.getScheme(), pageUri.getUserInfo(), pageUri.getHost(), pageUri.getPort(), pageUri.getPath(), query, null);
    }

    public URI getPageUri(FormRequest request, Validation validation, Explanation explanation, int count) throws URISyntaxException {
        String taskId = request != null ? request.getTaskId() : null;
        String query = null;
        if (explanation == null && StringUtils.isNotEmpty(taskId))
            query = "taskId=" + taskId;
        else if (request != null && StringUtils.isNotEmpty(request.getRequestId()))
            query = "requestId=" + request.getRequestId();
        else if (validation != null && validation.getSubmission() != null && StringUtils.isNotEmpty(validation.getSubmission().getSubmissionId()))
            query = "submissionId=" + validation.getSubmission().getSubmissionId();

        query += "&redirectCount=" + count;

        if (pageUri == null || count >= 3)
            return null;

        return new URI(pageUri.getScheme(), pageUri.getUserInfo(), pageUri.getHost(), pageUri.getPort(), pageUri.getPath(), query, null);
    }

    public String getBase() {
        return base;
    }

    public String getPath() {
        return path;
    }

    public Action getAction() {
        return action;
    }

    public static final class Builder {

        private FormDispositionType type;
        private URI hostUri;
        private URI pageUri;
        private URI resourceUri;
        private String base;
        private String path;
        private Action action;

        private Builder() {

        }

        private Builder(Process process, ProcessDeployment deployment, Action action, ViewContext context) {
            String hostUrl = context.getHostUri();
            String pageUrl = context.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME, process.getProcessDefinitionKey());
            String resourceUrl = pageUrl;

            this.action = action;
            this.base = deployment.getBase();
            this.path = action.getLocation();

            switch (action.getStrategy()) {
                case DECORATE_HTML:
                    this.type = FormDispositionType.CUSTOM;
                    break;
                case INCLUDE_SCRIPT:
                case INCLUDE_DIRECTIVES:
                case REMOTE:
                    this.type = FormDispositionType.REMOTE;
                    hostUrl = deployment.getRemoteHost();
                    pageUrl = StringUtils.isNotEmpty(hostUrl) && StringUtils.isNotEmpty(action.getLocation()) ? hostUrl + action.getLocation() : null;
                    break;
                default:
                    this.type = FormDispositionType.DEFAULT;
                    break;
            }

            this.hostUri = StringUtils.isNotEmpty(hostUrl) ? URI.create(hostUrl) : null;
            this.pageUri = StringUtils.isNotEmpty(pageUrl) ? URI.create(pageUrl) : null;
            this.resourceUri = StringUtils.isNotEmpty(resourceUrl) ? URI.create(resourceUrl) : null;
        }

        public static FormDisposition build(Process process, ProcessDeployment deployment, Action action, ViewContext context) {
            return new FormDisposition(new Builder(process, deployment, action, context));
        }

    }

}
