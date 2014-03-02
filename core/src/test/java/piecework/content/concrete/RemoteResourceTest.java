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

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.content.config.ContentConfiguration;
import piecework.enumeration.Scheme;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ContentConfiguration.class})
public class RemoteResourceTest {

    private CloseableHttpClient client;
    private RemoteResource remoteResource;
    private final static String URI_STRING = "http://localhost:10001/external/some/resource";

    @Before
    public void setup() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        this.client = HttpClients.custom().setConnectionManager(cm).build();
        URI uri = URI.create(URI_STRING);
        this.remoteResource = new RemoteResource(client, uri);
    }

    @After
    public void teardown() throws IOException {
        this.client.close();
    }

    @Test
    public void verifyLocation() throws MalformedURLException, IOException {
        Assert.assertEquals(URI_STRING, remoteResource.getLocation());
    }

    @Test
    public void verifyContentLength() throws IOException {
        Assert.assertTrue(remoteResource.contentLength() > 1);
    }

    @Test
    public void verifyContentType() throws IOException {
        Assert.assertEquals("text/plain;charset=UTF-8", remoteResource.contentType());
    }

    @Test
    public void verifyInputStream() throws IOException {
        String content = IOUtils.toString(remoteResource.getInputStream());
        Assert.assertEquals(41, content.length());
    }

}
