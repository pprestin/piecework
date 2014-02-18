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
package piecework.submission;

import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.model.Entity;
import piecework.model.FormRequest;
import piecework.model.ProcessInstance;
import piecework.model.Submission;

/**
 * @author James Renfro
 */
public interface SubmissionHandler<T> {

    Submission handle(ProcessInstance instance, T submission, SubmissionTemplate template, Entity principal) throws PieceworkException;

    Class<?> getType();

}
