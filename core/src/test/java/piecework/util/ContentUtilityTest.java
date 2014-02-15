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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import piecework.model.Content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

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

    @Test
    public void gridFsFileToContent() throws IOException {
        String id = "123";
        String contentType = "text/html";
        String originalFilename = "SomeFileName.html";
        String filename = "file.html";
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test".getBytes("UTF-8"));
        Date testDate = new Date();
        long length = "test".length();
        Long contentLength = Long.valueOf(length);

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

        Content content = ContentUtility.toContent(gridFsFile);

        Assert.assertEquals(id, content.getContentId());
        Assert.assertEquals(contentType, content.getContentType());
        Assert.assertEquals(originalFilename, content.getFilename());
        Assert.assertEquals(filename, content.getLocation());
        Assert.assertEquals(inputStream, content.getInputStream());
        Assert.assertEquals(testDate, content.getLastModified());
        Assert.assertEquals(contentLength, content.getLength());
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

        Content content = ContentUtility.toContent(gridFsResource);

        Assert.assertEquals(id, content.getContentId());
        Assert.assertEquals(contentType, content.getContentType());
        Assert.assertEquals(filename, content.getFilename());
        Assert.assertEquals(filename, content.getLocation());
        Assert.assertEquals(inputStream, content.getInputStream());
        Assert.assertEquals(testDate, content.getLastModified());
        Assert.assertEquals(contentLength, content.getLength());
    }


    @Test
    public void httpEntityToContent() throws IOException {
        URI uri = URI.create("http://testserver.com/some/file.html");
        String id = uri.toString();
        String contentType = "text/html";
        String filename = "file.html";
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test".getBytes("UTF-8"));
        Date testDate = new Date();
        long length = "test".length();
        Long contentLength = Long.valueOf(length);
        String eTag = "1e3dc9c02b2fe35a2b9c6fca3f5c9e21:1326411454";

        Mockito.when(entity.getContentType())
                .thenReturn(new BasicHeader("Content-Type", contentType));
        Mockito.when(entity.getContent())
                .thenReturn(inputStream);
        Mockito.when(entity.getContentLength())
                .thenReturn(length);

        Content content = ContentUtility.toContent(uri, entity, testDate, eTag);

        String expected = "test";
        String actual = IOUtils.toString(content.getInputStream());

        Assert.assertEquals(id, content.getContentId());
        Assert.assertEquals(contentType, content.getContentType());
        Assert.assertEquals(filename, content.getFilename());
        Assert.assertEquals("http://testserver.com/some/file.html", content.getLocation());
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(testDate, content.getLastModified());
        Assert.assertEquals(contentLength, content.getLength());
        Assert.assertEquals(eTag, content.getMd5());
    }

}
