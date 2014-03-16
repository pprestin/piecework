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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import piecework.export.Pager;
import piecework.model.ProcessInstance;
import piecework.repository.ProcessInstanceRepository;

/**
 * @author James Renfro
 */
public class ProcessInstanceQueryPager implements Pager<ProcessInstance> {

    private static final int PAGE_SIZE = 200;

    private final Query query;
    private final ProcessInstanceRepository repository;
    private final Sort sort;

    private Page<ProcessInstance> page;
    private Pageable request;

    public ProcessInstanceQueryPager(Query query, ProcessInstanceRepository repository, Sort sort) {
        this.query = query;
        this.repository = repository;
        this.request = new PageRequest(0, PAGE_SIZE, sort);
        this.sort = sort;
    }

    public Page<ProcessInstance> nextPage() {
        Query query = this.query.with(this.request);
        this.page = repository.findByQuery(query, this.request, true);
        this.request = page.nextPageable();
        return page;
    }

    public boolean hasNext() {
        return this.page == null || !this.page.isLastPage();
    }

    public void reset() {
        this.request = new PageRequest(0, PAGE_SIZE, sort);
    }

}
