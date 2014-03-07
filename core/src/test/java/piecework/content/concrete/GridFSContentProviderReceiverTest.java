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
package piecework.content.concrete;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import piecework.common.UuidGenerator;
import piecework.enumeration.AlarmSeverity;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.content.ContentResource;
import piecework.model.Entity;
import piecework.persistence.ContentProfileProvider;
import piecework.security.AccessTracker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class GridFSContentProviderReceiverTest {

    @InjectMocks
    GridFSContentProviderReceiver contentProviderReceiver;

    @Mock
    GridFsOperations gridFsOperations;

    @Mock
    GridFSDBFile gridFSDBFile;

    @Mock
    GridFSFile gridFSFile;

    @Mock
    UuidGenerator uuidGenerator;

    @Mock
    ContentProfileProvider modelProvider;

    @Before
    public void setup() throws PieceworkException {
        contentProviderReceiver.accessTracker = Mockito.mock(AccessTracker.class);
        Mockito.doReturn("TEST")
               .when(modelProvider).processDefinitionKey();
        Mockito.doReturn("50000000001")
               .when(uuidGenerator).getNextId();
        Mockito.doReturn(Collections.singletonList(gridFSDBFile))
               .when(gridFsOperations).find(any(Query.class));
        Mockito.doReturn(gridFSDBFile)
                .when(gridFsOperations).findOne(any(Query.class));
        Mockito.doReturn("60000000001")
               .when(gridFSDBFile).getId();
        Mockito.doReturn(new ByteArrayInputStream("Some sample data".getBytes()))
               .when(gridFSDBFile).getInputStream();
        Mockito.doReturn(gridFSFile)
               .when(gridFsOperations).store(any(InputStream.class), anyString(), anyString(), any(DBObject.class));
        Mockito.doReturn("70000000001")
               .when(gridFSFile).getId();
        Mockito.doReturn("application/json")
               .when(gridFSFile).getContentType();
    }

    @Test
    public void verifyExpire() throws Exception {
        Assert.assertFalse(contentProviderReceiver.expire(modelProvider, "/TEST/50000000001"));
    }

    @Test(expected = ForbiddenError.class)
    public void verifyFindByInvalidLocation() throws Exception {
        String location = "/OTHER/50000000001";
        try {
            contentProviderReceiver.findByLocation(modelProvider, location);
        } finally {
            Mockito.verify(contentProviderReceiver.accessTracker)
                    .alarm(eq(AlarmSeverity.MINOR), anyString(), any(Entity.class));
        }
    }

    @Test
    public void verifyFindByValidLocation() throws Exception {
        String location = "/TEST/50000000001";
        ContentResource contentResource = contentProviderReceiver.findByLocation(modelProvider, location);
        Assert.assertNotNull(contentResource);
        Assert.assertEquals("60000000001", contentResource.getContentId());
        String actual = IOUtils.toString(contentResource.getInputStream());
        Assert.assertEquals("Some sample data", actual);
    }

    @Test
    public void verifySave() throws Exception {
        ContentResource contentResource = new BasicContentResource.Builder()
                .contentType("application/json")
                .build();
        ContentResource stored = contentProviderReceiver.save(modelProvider, contentResource);
        Assert.assertEquals("application/json", stored.contentType());
        Assert.assertEquals("70000000001", stored.getContentId());
    }

    @Test
    public void verifyKey() {
        Assert.assertEquals("default-gridfs", contentProviderReceiver.getKey());
    }

    @Test
    public void verifyScheme() {
        Assert.assertEquals(Scheme.REPOSITORY, contentProviderReceiver.getScheme());
    }

}
