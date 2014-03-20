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

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.persistence.ProcessProvider;
import piecework.repository.ProcessRepository;
import piecework.security.concrete.PassthroughSanitizer;

/**
 * Retrieves a process by its process definition key.
 *
 * @author James Renfro
 */
public class ProcessRepositoryProvider implements ProcessProvider {
    private static final Logger LOG = Logger.getLogger(ProcessRepositoryProvider.class);

    private final ProcessRepository processRepository;
    private final String processDefinitionKey;
    private final Entity principal;

    private Process process;

    ProcessRepositoryProvider(ProcessRepository processRepository, String processDefinitionKey, Entity principal) {
        this.processRepository = processRepository;
        this.processDefinitionKey = processDefinitionKey;
        this.principal = principal;
    }

    @Override
    public synchronized Process process() throws StatusCodeError {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        if (process == null) {
            if (StringUtils.isEmpty(processDefinitionKey))
                throw new BadRequestError(Constants.ExceptionCodes.process_key_required);
            process = processRepository.findOne(processDefinitionKey);
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved process in " + (System.currentTimeMillis() - time) + " ms");

        if (process == null)
            throw new NotFoundError();
        if (process.isDeleted())
            throw new GoneError();

        return process;
    }

    @Override
    public Process process(ViewContext context) throws PieceworkException {
        return new Process.Builder(process(), new PassthroughSanitizer()).build(context);
    }

    @Override
    public String processDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public Entity principal() {
        return principal;
    }
}
