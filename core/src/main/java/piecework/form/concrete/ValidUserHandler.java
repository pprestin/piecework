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
package piecework.form.concrete;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.form.handler.ValueHandler;
import piecework.identity.InternalUserDetailsService;
import piecework.model.FormValue;
import piecework.model.FormValueDetail;
import piecework.model.User;

import java.util.Arrays;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class ValidUserHandler implements ValueHandler {

    @Autowired
    InternalUserDetailsService userDetailsService;

    @Override
    public List<FormValue> handle(String name, String value, FormValueDetail detail) {
        User user = userDetailsService.getUserByAnyId(value);

        if (user != null) {
            FormValue internalId = new FormValue.Builder().name(name).value(user.getUserId()).build();
            FormValue displayName = new FormValue.Builder().name(name + "__displayName").value(user.getDisplayName()).build();
            FormValue visibleId = new FormValue.Builder().name(name + "__visibleId").value(user.getVisibleId()).build();

            return Arrays.asList(internalId, displayName, visibleId);
        }

        return null;
    }

    @Override
    public List<String> getAcceptableFieldNames(String name) {
        return Arrays.asList(name, name + "__displayName", name + "__visibleId");
    }

}
