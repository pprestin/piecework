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
package piecework.content.stubs;

import piecework.content.ContentProvider;
import piecework.content.ContentProviderVoter;
import piecework.content.concrete.InMemoryContentProviderReceiver;
import piecework.enumeration.ContentHandlerPriority;

/**
 * @author James Renfro
 */
public class TestContentProviderVoter implements ContentProviderVoter {

    @Override
    public <P extends ContentProvider> ContentHandlerPriority vote(P provider) {
        if (provider instanceof TestExternalContentProvider)
            return ContentHandlerPriority.PRIMARY;
        if (provider instanceof InMemoryContentProviderReceiver)
            return ContentHandlerPriority.BACKUP;
        return ContentHandlerPriority.IGNORE;
    }

}
