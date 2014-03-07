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
package piecework.command;

import piecework.model.FormRequest;
import piecework.model.Submission;
import piecework.persistence.ProcessDeploymentProvider;

/**
 * @author James Renfro
 */
public class SubmissionCommandResponse<P extends ProcessDeploymentProvider> {

    private final P modelProvider;
    private final Submission submission;
    private final FormRequest nextRequest;

    public SubmissionCommandResponse(P modelProvider, Submission submission, FormRequest nextRequest) {
        this.modelProvider = modelProvider;
        this.submission = submission;
        this.nextRequest = nextRequest;
    }

    public Submission getSubmission() {
        return submission;
    }

    public FormRequest getNextRequest() {
        return nextRequest;
    }

    public P getModelProvider() {
        return modelProvider;
    }
}
