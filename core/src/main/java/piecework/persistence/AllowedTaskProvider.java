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
package piecework.persistence;

import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.exception.PieceworkException;
import piecework.model.SearchResults;
import piecework.model.Task;
import piecework.process.AttachmentQueryParameters;

/**
 * @author James Renfro
 */
public interface AllowedTaskProvider extends ProcessInstanceProvider {

    Task allowedTask(boolean limitToActive) throws PieceworkException;

    Task allowedTask(ViewContext context, boolean limitToActive) throws PieceworkException;

    ContentResource attachment(String attachmentId) throws PieceworkException;

    SearchResults attachments(AttachmentQueryParameters queryParameters, ViewContext context) throws PieceworkException;

    ContentResource value(String fieldName, String fileId) throws PieceworkException;

    SearchResults values(String fieldName, ViewContext context) throws PieceworkException;

}
