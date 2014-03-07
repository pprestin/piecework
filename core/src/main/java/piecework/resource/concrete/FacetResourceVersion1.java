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
package piecework.resource.concrete;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.common.ViewContext;
import piecework.enumeration.AlarmSeverity;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.Entity;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.SearchProvider;
import piecework.resource.FacetResource;
import piecework.security.AccessTracker;
import piecework.settings.UserInterfaceSettings;

import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Service
public class FacetResourceVersion1 implements FacetResource {
    private static final Logger LOG = Logger.getLogger(FacetResourceVersion1.class);
    private static final String VERSION = "v1";

    @Autowired
    AccessTracker accessTracker;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ModelProviderFactory providerFactory;

    @Autowired
    UserInterfaceSettings settings;

    @Override
    public Response search(MessageContext context, String label) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        if (principal == null) {
            String message = "Someone is attempting to view a list of forms through an anonymous resource. This is never allowed.";
            LOG.error(message);
            accessTracker.alarm(AlarmSeverity.URGENT, message);
            throw new ForbiddenError();
        }

        SearchProvider searchProvider = providerFactory.searchProvider(principal);
        return Response.ok(searchProvider.facets(label, new ViewContext(settings, VERSION))).build();
    }

}
