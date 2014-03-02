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
package piecework.persistence.concrete;

import piecework.common.ViewContext;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;
import piecework.settings.ContentSettings;
import piecework.settings.UserInterfaceSettings;

/**
 * @author James Renfro
 */
public class SystemContentProfileProvider implements ContentProfileProvider {

    private final ContentSettings settings;
    private final Entity principal;

    public SystemContentProfileProvider(ContentSettings settings, Entity principal) {
        this.settings = settings;
        this.principal = principal;
    }

    @Override
    public ContentProfile contentProfile() throws PieceworkException {
        return new ContentProfile.Builder()
                .baseDirectory(settings.getApplicationFilesystemRoot())
                .build();
    }

    @Override
    public Process process() throws PieceworkException {
        // Does not provide a specific process
        throw new NotFoundError();
    }

    @Override
    public Process process(ViewContext context) throws PieceworkException {
        // Does not provide a specific process
        throw new NotFoundError();
    }

    @Override
    public String processDefinitionKey() {
        return null;
    }

    @Override
    public Entity principal() {
        return principal;
    }
}
