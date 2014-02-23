/*
 * Copyright 2012 University of Washington
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

import javax.ws.rs.core.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import piecework.persistence.ProcessProvider;
import piecework.exception.*;
import piecework.resource.AnonymousFormResource;

import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class AnonymousFormResourceVersion1 extends AbstractFormResource implements AnonymousFormResource {

    private static final Logger LOG = Logger.getLogger(FormResourceVersion1.class);


    @Override
    public Response read(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        return startForm(context, rawProcessDefinitionKey, null);
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultivaluedMap<String, String> formData) throws PieceworkException {
        return submitForm(context, rawProcessDefinitionKey, rawRequestId, formData, Map.class, null);
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final MessageContext context, final MultivaluedMap<String, String> formData) throws PieceworkException {
        return validateForm(context, rawProcessDefinitionKey, formData, rawRequestId, rawValidationId, null);
    }

    @Override
    protected boolean isAnonymous() {
        return true;
    }

}
