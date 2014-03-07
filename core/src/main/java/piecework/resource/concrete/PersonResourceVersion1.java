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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.exception.StatusCodeError;
import piecework.identity.PersonSearchCriteria;
import piecework.model.ProcessInstance;
import piecework.model.SearchResults;
import piecework.model.User;
import piecework.resource.PersonResource;
import piecework.service.IdentityService;

import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class PersonResourceVersion1 implements PersonResource {

    @Autowired
    IdentityService userDetailsService;

    @Autowired
    Versions versions;

    @Override
    public SearchResults search(PersonSearchCriteria criteria) throws StatusCodeError {

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("People")
                .resourceName(ProcessInstance.Constants.ROOT_ELEMENT_NAME)
                .link(versions.getVersion1().getApplicationUri(User.Constants.ROOT_ELEMENT_NAME));

        List<User> users = userDetailsService.findUsersByDisplayName(criteria.getDisplayNameLike(), criteria.getMaxResults());
        resultsBuilder.items(users);
        return resultsBuilder.build();
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
