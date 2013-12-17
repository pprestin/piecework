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
package piecework.content;

import piecework.enumeration.ContentHandlerPriority;

/**
 * Implement this class if you wish to make decisions about which providers to make use of in the
 * ContentHandlerRepository -- the primary use case for this is when you've implemented a
 * ContentProvider that connects to an external content repository and want to disable
 * the default GridFSContentProvider or make it into a backup for your external repo.
 *
 * @author James Renfro
 */
public interface ContentProviderVoter {

    <P extends ContentProvider> ContentHandlerPriority vote(P provider);

}
