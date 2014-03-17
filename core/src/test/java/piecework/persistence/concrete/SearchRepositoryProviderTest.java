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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.common.SearchCriteria;
import piecework.content.ContentResource;
import piecework.model.Entity;
import piecework.model.Facet;
import piecework.model.Process;
import piecework.model.SearchResponse;
import piecework.model.SearchResults;
import piecework.repository.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.security.data.DataFilterService;
import piecework.service.CacheService;
import piecework.service.IdentityService;
import piecework.test.ProcessFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchRepositoryProviderTest {

    @Mock
    BucketListRepository bucketListRepository;

    @Mock
    CacheService cacheService;

    @Mock
    DataFilterService dataFilterService;

    @Mock
    IdentityService identityService;

    @Mock
    ProcessRepository processRepository;

    @Mock
    ProcessInstanceRepository processInstanceRepository;

    @Mock
    AttachmentRepository attachmentRepository;

    @Mock
    ContentRepository contentRepository;

    @Mock
    ContentResource contentResource;

    @Mock
    DeploymentRepository deploymentRepository;

    @Mock
    Entity principal;

    private SearchRepositoryProvider searchProvider;

    @Before
    public void setup() {
        this.searchProvider = new SearchRepositoryProvider(processRepository, processInstanceRepository,
                bucketListRepository, cacheService, dataFilterService, identityService, new PassthroughSanitizer(), principal);
    }

    @Test
    public void verifyBaseFacets() throws Exception {
        SearchResults results = searchProvider.facets("Test", ProcessFactory.viewContext());
        Assert.assertEquals(Long.valueOf(7l), results.getTotal());
        List<Object> list = results.getList();
        Facet processInstanceLabel = Facet.class.cast(list.get(0));
        Assert.assertEquals("processInstanceLabel", processInstanceLabel.getName());
    }

    @Test
    public void verifyProcesses() throws Exception {
        Mockito.doReturn(Collections.singleton("TEST-1"))
                .when(principal).getProcessDefinitionKeys(eq(AuthorizationRole.USER));

        Process process = new Process.Builder()
                .processDefinitionKey("TEST-1")
                .build();

        Mockito.doReturn(Collections.singletonList(process))
                .when(processRepository).findAllBasic(any(Iterable.class));

        Set<Process> processes = searchProvider.processes(Collections.singleton("TEST-1"));
        Assert.assertEquals(1, processes.size());
    }

    @Test
    public void verifyFormsExcludeData() throws Exception {
        Mockito.doReturn(Collections.singleton("TEST-1"))
               .when(principal).getProcessDefinitionKeys(eq(AuthorizationRole.USER));

        Process process = new Process.Builder()
                .processDefinitionKey("TEST-1")
                .processDefinitionLabel("A Test Example Process")
                .build();

        Mockito.doReturn(Collections.singletonList(process))
                .when(processRepository).findAllBasic(any(Iterable.class));

        SearchCriteria criteria = new SearchCriteria.Builder()
                .processDefinitionKey("TEST-1")
                .processDefinitionKey("TEST-2")
                .build();

        SearchResponse response = searchProvider.forms(criteria, ProcessFactory.viewContext(), true);
        List<Map<String, String>> metadata = response.getMetadata();
        Assert.assertEquals(1, metadata.size());
        Map<String, String> map = metadata.get(0);
        Assert.assertEquals(3, map.size());
        String processDefinitionKey = map.get("processDefinitionKey");
        String processDefinitionLabel = map.get("processDefinitionLabel");
        String link = map.get("link");
        Assert.assertEquals("TEST-1", processDefinitionKey);
        Assert.assertEquals("A Test Example Process", processDefinitionLabel);
        Assert.assertEquals("https://somehost.org/piecework/ui/form/TEST-1", link);
        Assert.assertEquals(0, response.getData().size());
        Assert.assertEquals(0, response.getTotal());
        Assert.assertEquals(0, response.getPageNumber());
        Assert.assertEquals(0, response.getPageSize());
    }

}
