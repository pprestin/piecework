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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import piecework.content.ContentResource;
import piecework.content.Version;
import piecework.util.FileUtility;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class RemoteResource implements ContentResource {

    private static final Logger LOG = Logger.getLogger(RemoteResource.class);

    private final CloseableHttpClient client;
    private final URI uri;
    private String contentType;
    private long contentLength;
    private long lastModified;
    private String eTag;
    private boolean initialized;

    public RemoteResource(CloseableHttpClient client, URI uri) {
        this.client = client;
        this.uri = uri;
        this.initialized = false;
    }

    @Override
    public String getContentId() {
        return this.uri.toString();
    }

    @Override
    public String getLocation() {
        return this.uri.toString();
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public synchronized long contentLength() {
        ensureInitialized();
        return contentLength;
    }

    @Override
    public synchronized long lastModified()  {
        ensureInitialized();
        return lastModified;
    }

    @Override
    public String lastModifiedBy() {
        return null;
    }

    @Override
    public List<Version> versions() {
        return Collections.<Version>emptyList();
    }

    public synchronized String eTag() {
        ensureInitialized();
        return eTag;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getFilename() {
        return FileUtility.resolveFilenameFromPath(uri.getPath());
    }

    @Override
    public String getDescription() {
        return null;
    }

    public synchronized String contentType() {
        ensureInitialized();
        return contentType;
    }

    protected synchronized void ensureInitialized() {
        if (!this.initialized) {
            this.initialized = true;
            HttpCacheContext context = HttpCacheContext.create();
            HttpHead head = new HttpHead(uri);

            final ConnectionCloser connectionCloser = new ConnectionCloser();
            try {
                LOG.info("Retrieving resource from " + uri.toString());
                CloseableHttpResponse response = client.execute(head, context);
                connectionCloser.setResponse(response);
                addDetails(response);
            } catch (Exception e) {
                LOG.error("Unable to retrieve details about remote resource", e);
            } finally {
                connectionCloser.closeEverythingQuietly();
            }
        }
    }

    private synchronized void addDetails(CloseableHttpResponse response) {
        Header contentTypeHeader = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
        if (contentTypeHeader != null)
            this.contentType = contentTypeHeader.getValue();
        Header contentLengthHeader = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        if (contentLengthHeader != null) {
            try {
                this.contentLength = StringUtils.isNotEmpty(contentLengthHeader.getValue()) ? Long.valueOf(contentLengthHeader.getValue()) : -1;
            } catch (NumberFormatException nfe) {
                LOG.warn("Unable to format content length from " + contentLengthHeader.getValue() + " for " + uri.toString());
            }
        }
        Header lastModifiedHeader = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (lastModifiedHeader != null) {
            Date lastModifiedDate = StringUtils.isNotEmpty(lastModifiedHeader.getValue()) ? DateUtils.parseDate(lastModifiedHeader.getValue()) : null;
            if (lastModifiedDate != null)
                this.lastModified = lastModifiedDate.getTime();
        }
        Header eTagHeader = response.getFirstHeader(HttpHeaders.ETAG);
        if (eTagHeader != null)
            this.eTag = StringUtils.isNotEmpty(eTagHeader.getValue()) ? eTagHeader.getValue() : null;

        this.initialized = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        HttpCacheContext context = HttpCacheContext.create();
        HttpGet get = new HttpGet(uri);

        addHeaders(get);
        final ConnectionCloser connectionCloser = new ConnectionCloser();
        try {
            LOG.info("Retrieving resource from " + uri.toString());
            CloseableHttpResponse response = client.execute(get, context);
            connectionCloser.setResponse(response);
            addDetails(response);
            HttpEntity entity = response.getEntity();
            connectionCloser.setEntity(entity);

            InputStream inputStream = entity.getContent();

            if (inputStream != null) {
                inputStream = new AutoCloseInputStream(inputStream) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        connectionCloser.closeEverythingQuietly();
                    }
                };
            }

            return inputStream;

        } catch (Exception e) {
            connectionCloser.closeEverythingQuietly();
            LOG.error("Unable to retrieve remote resource", e);
            throw new IOException("Unable to retrieve remote resource content", e);
        } finally {

        }
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        InputStream input = getInputStream();
        try {
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    protected void addHeaders(HttpGet get) {

    }

    public class ConnectionCloser {
        private CloseableHttpResponse response;
        private HttpEntity entity;

        public ConnectionCloser() {

        }

        public CloseableHttpResponse getResponse() {
            return response;
        }

        public void setResponse(CloseableHttpResponse response) {
            this.response = response;
        }

        public HttpEntity getEntity() {
            return entity;
        }

        public void setEntity(HttpEntity entity) {
            this.entity = entity;
        }

        public void closeEverythingQuietly() {
            try {
                EntityUtils.consume(entity);
            } catch (IOException ioe) {
                LOG.error("Unable to close entity", ioe);
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ioe) {
                    LOG.error("Unable to close response", ioe);
                }
            }
        }

    }

}
