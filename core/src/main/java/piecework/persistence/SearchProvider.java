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

import piecework.common.SearchCriteria;
import piecework.common.SearchQueryParameters;
import piecework.common.ViewContext;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;

import javax.swing.text.View;
import java.util.Set;

/**
 * @author James Renfro
 */
public interface SearchProvider extends ModelProvider {

    SearchResults facets(String label, ViewContext context) throws PieceworkException;

    SearchResponse forms(SearchCriteria criteria, ViewContext context) throws PieceworkException;

    Set<Process> processes(String ... allowedRoles);

    Set<Process> processes(Set<String> processDefinitionKeys);

    SearchResults tasks(SearchCriteria criteria, ViewContext context) throws PieceworkException;

}
