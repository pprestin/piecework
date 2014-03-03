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

import org.springframework.cache.Cache;
import piecework.common.ViewContext;
import piecework.enumeration.CacheName;
import piecework.exception.GoneError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.persistence.ProcessProvider;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.CacheService;

/**
 * Implementation of ProcessProvider that adds caching.
 *
 * @author James Renfro
 */
public class CachingProcessProvider implements ProcessProvider {

    private final CacheService cacheService;
    private final ProcessProvider processProvider;

    CachingProcessProvider(CacheService cacheService, ProcessProvider processProvider) {
        this.cacheService = cacheService;
        this.processProvider = processProvider;
    }

    @Override
    public piecework.model.Process process() throws PieceworkException {
        String processDefinitionKey = processProvider.processDefinitionKey();

        Process process;
        Cache.ValueWrapper value = cacheService.get(CacheName.PROCESS, processDefinitionKey);
        if (value != null) {
            process = Process.class.cast(value.get());

            if (process == null)
                throw new NotFoundError();
            if (process.isDeleted())
                throw new GoneError();

        } else {
            process = processProvider.process();
            cacheService.put(CacheName.PROCESS, processDefinitionKey, process);
        }

        return process;
    }

    @Override
    public Process process(ViewContext context) throws PieceworkException {
        return new Process.Builder(process(), new PassthroughSanitizer()).build(context);
    }

    @Override
    public Entity principal() {
        return processProvider.principal();
    }

    @Override
    public String processDefinitionKey() {
        return processProvider.processDefinitionKey();
    }

}
