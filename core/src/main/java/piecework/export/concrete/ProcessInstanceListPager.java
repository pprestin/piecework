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
package piecework.export.concrete;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import piecework.export.Pager;
import piecework.model.ProcessInstance;

import java.util.List;

/**
 * @author James Renfro
 */
public class ProcessInstanceListPager implements Pager<ProcessInstance> {

    private final List<ProcessInstance> instances;
    private boolean hasNext = true;

    public ProcessInstanceListPager(List<ProcessInstance> instances) {
        this.instances = instances;
    }

    public Page<ProcessInstance> nextPage() {
        return new PageImpl<ProcessInstance>(instances);
    }

    public boolean hasNext() {
        if (hasNext) {
            hasNext = false;
            return true;
        }
        return false;
    }

    public void reset() {
        // No op
    }

}
