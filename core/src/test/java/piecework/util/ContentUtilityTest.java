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
package piecework.util;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import piecework.exception.InternalServerError;
import piecework.exception.PieceworkException;
import piecework.content.ContentResource;
import piecework.model.ContentProfile;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentUtilityTest {

    @Mock
    GridFSDBFile gridFsFile;

    @Mock
    GridFsResource gridFsResource;

    @Mock
    HttpEntity entity;

    @Test(expected = InternalServerError.class)
    public void contentHandlerKeyNullProvider() throws PieceworkException {
        ContentUtility.contentHandlerKey(null);
    }

    @Test
    public void contentHandlerKeyNullProfile() throws PieceworkException {
        ContentProfile contentProfile = null;
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        Assert.assertNull(ContentUtility.contentHandlerKey(modelProvider));
    }

    @Test
    public void contentHandlerKey() throws PieceworkException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .contentHandlerKey("TEST-KEY")
                .build();
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        Assert.assertEquals("TEST-KEY", ContentUtility.contentHandlerKey(modelProvider));
    }

    @Test
    public void gridFsFileToContent() throws IOException {
        String id = "123";
        String contentType = "text/html";
        String originalFilename = "SomeFileName.html";
        String filename = "file.html";
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test".getBytes("UTF-8"));
        Date testDate = new Date();
        long length = "test".length();

        DBObject metadata = new BasicDBObject();
        metadata.put("originalFilename", originalFilename);

        Mockito.when(gridFsFile.getMetaData())
                .thenReturn(metadata);
        Mockito.when(gridFsFile.getId())
               .thenReturn(id);
        Mockito.when(gridFsFile.getContentType())
                .thenReturn(contentType);
        Mockito.when(gridFsFile.getFilename())
                .thenReturn(filename);
        Mockito.when(gridFsFile.getInputStream())
                .thenReturn(inputStream);
        Mockito.when(gridFsFile.getUploadDate())
                .thenReturn(testDate);
        Mockito.when(gridFsFile.getLength())
                .thenReturn(length);

        ContentResource contentResource = ContentUtility.toContent(gridFsFile);

        Assert.assertEquals(id, contentResource.getContentId());
        Assert.assertEquals(contentType, contentResource.contentType());
        Assert.assertEquals(originalFilename, contentResource.getFilename());
        Assert.assertEquals(filename, contentResource.getLocation());
        Assert.assertEquals(inputStream, contentResource.getInputStream());
        Assert.assertEquals(testDate.getTime(), contentResource.lastModified());
        Assert.assertEquals(length, contentResource.contentLength());
    }

    @Test
    public void gridFsResourceToContent() throws IOException {

        String id = "123";
        String contentType = "text/html";
        String filename = "file.html";
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test".getBytes("UTF-8"));
        Date testDate = new Date();
        long length = "test".length();
        Long contentLength = Long.valueOf(length);

        Mockito.when(gridFsResource.getId())
                .thenReturn(id);
        Mockito.when(gridFsResource.getContentType())
                .thenReturn(contentType);
        Mockito.when(gridFsResource.getFilename())
                .thenReturn(filename);
        Mockito.when(gridFsResource.getInputStream())
                .thenReturn(inputStream);
        Mockito.when(gridFsResource.lastModified())
                .thenReturn(testDate.getTime());
        Mockito.when(gridFsResource.contentLength())
                .thenReturn(length);

        ContentResource contentResource = ContentUtility.toContent(gridFsResource);

        Assert.assertEquals(id, contentResource.getContentId());
        Assert.assertEquals(contentType, contentResource.contentType());
        Assert.assertEquals(filename, contentResource.getFilename());
        Assert.assertEquals(filename, contentResource.getLocation());
        Assert.assertEquals(inputStream, contentResource.getInputStream());
        Assert.assertEquals(testDate.getTime(), contentResource.lastModified());
        Assert.assertEquals(length, contentResource.contentLength());
    }

//    @Test
//    public void uriToContent() throws IOException {
//        URI uri = URI.create("https://raw.github.com/piecework/piecework/master/README.md");
//        String id = uri.toString();
//        String contentType = "text/plain; charset=utf-8";
//        String filename = "README.md";
//
//        CloseableHttpClient client = HttpClients.createDefault();
//        ContentResource contentResource = ContentUtility.toContent(client, uri);
//
//        String actual = IOUtils.toString(contentResource.getInputStream());
//
//        Assert.assertEquals(id, contentResource.getContentId());
//        Assert.assertEquals(contentType, contentResource.contentType());
//        Assert.assertEquals(filename, contentResource.getFilename());
//        Assert.assertEquals("https://raw.github.com/piecework/piecework/master/README.md", contentResource.getLocation());
//        Assert.assertTrue(actual.length() > 0);
////        Assert.assertEquals(testDate, content.getLastModified());
////        Assert.assertEquals(Long.valueOf(actual.length()), content.getLength());
////        Assert.assertEquals(eTag, content.getMd5());
//    }

    @Test
    public void validateValidRemoteLocation() {
        Set<String> acceptable = Sets.newHashSet("http://test.edu/search/.*");
        URI uri = URI.create("http://test.edu/search/some/path");
        Assert.assertTrue(ContentUtility.validateRemoteLocation(acceptable, uri));
    }

}
