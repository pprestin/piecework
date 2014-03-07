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
package piecework.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import piecework.model.*;
import piecework.model.Process;

import java.util.Set;

/**
 * @author James Renfro
 */
public interface PageHandler<T> {

    SearchResults handle(Page<T> page, Pageable pageable, Set<Process> allowedProcesses);

}
