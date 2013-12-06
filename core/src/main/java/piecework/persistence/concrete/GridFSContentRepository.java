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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import piecework.enumeration.Scheme;
import piecework.persistence.ContentRepository;
import piecework.model.Content;
import piecework.util.PathUtility;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author James Renfro
 */
@Service
public class GridFSContentRepository implements ContentRepository {

    private static final Logger LOG = Logger.getLogger(GridFSContentRepository.class);

    @Autowired
    GridFsOperations gridFsOperations;

    private CloseableHttpClient client;

    @PostConstruct
    public void init() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        this.client = HttpClients.custom().setConnectionManager(cm).build();
    }

    @PreDestroy
    public void destroy() {
        if (this.client != null) {
            try {
                this.client.close();
            } catch (IOException ioe) {
                LOG.error("Unable to close http client", ioe);
            }
        }
    }

    @Override
    public Content findByLocation(String location) {
        if (StringUtils.isEmpty(location))
            return null;

        try {
            Scheme scheme = PathUtility.findScheme(location);

            Content content = null;
            switch (scheme) {
                case REMOTE:
                    content = getRemotely(location);
                    break;
                case CLASSPATH:
                    content = getFromClasspath(location);
                    break;
                case FILESYSTEM:
                    content = getFromFileSystem(location);
                    break;
                case REPOSITORY:
                    content = getFromGridFS(location);
                    break;
            };
            return content;
        } catch (IOException e) {
            LOG.error("Could not retrieve content", e);
            return null;
        }
    }

    @Override
    public List<Content> findByLocationPattern(String locationPattern) throws IOException {
        GridFsResource[] resources = gridFsOperations.getResources(locationPattern);

        if (resources != null && resources.length > 0) {
            List<Content> contents = new ArrayList<Content>(resources.length);
            for (GridFsResource resource : resources) {
                contents.add(toContent(resource));
            }
            return contents;
        }
        return Collections.emptyList();
    }

    @Override
    public Content save(Content content) throws IOException {
        BasicDBObject metadata = new BasicDBObject();
        metadata.put("originalFilename", content.getName());

        GridFSFile file = gridFsOperations.store(content.getInputStream(), content.getLocation(), content.getContentType(), metadata);
        String contentId = file.getId().toString();

        return new Content.Builder(content)
                .contentId(contentId)
                .length(file.getLength())
                .lastModified(file.getUploadDate())
                .md5(file.getMD5())
                .build();
    }

    private Content toContent(GridFSDBFile file) {
        if (file == null)
            return null;

        String fileId = file.getId().toString();
        DBObject metadata = file.getMetaData();
        String originalFileName = metadata != null ? String.class.cast(metadata.get("originalFilename")) : null;

        return new Content.Builder()
                .contentId(fileId)
                .contentType(file.getContentType())
                .filename(originalFileName)
                .location(file.getFilename())
                .inputStream(file.getInputStream())
                .lastModified(file.getUploadDate())
                .length(Long.valueOf(file.getLength()))
                .md5(file.getMD5())
                .build();
    }

    private Content getFromClasspath(String location) throws IOException {
        if (!location.startsWith("classpath:"))
            return null;

        String classpathLocation = location.substring("classpath:".length());
        ClassPathResource resource = new ClassPathResource(classpathLocation);

        String contentType = null;
//        BufferedInputStream inputStream = new BufferedInputStream(resource.getInputStream());
//        if (contentType == null)
//            contentType = URLConnection.guessContentTypeFromStream(inputStream);
        if (contentType == null) {
            if (location.endsWith(".css"))
                contentType = "text/css";
            else if (location.endsWith(".js"))
                contentType = "application/json";
            else if (location.endsWith(".html"))
                contentType = "text/html";
        }
        return new Content.Builder().resource(resource).contentType(contentType).location(location).filename(location).build();
    }

    private Content getFromFileSystem(String location) throws IOException {
        if (!location.startsWith("file:"))
            return null;

        File file = new File(location.substring("file:".length()));
        FileSystemResource resource = new FileSystemResource(file);
        BufferedInputStream inputStream = new BufferedInputStream(resource.getInputStream());
        String contentType = URLConnection.guessContentTypeFromStream(inputStream);
        if (contentType == null) {
            if (location.endsWith(".css"))
                contentType = "text/css";
            else if (location.endsWith(".js"))
                contentType = "application/json";
            else if (location.endsWith(".html"))
                contentType = "text/html";
        }
        return new Content.Builder().inputStream(inputStream).contentType(contentType).location(location).filename(file.getPath()).build();
    }

    private Content getFromGridFS(String location) {
        GridFSDBFile file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(location)));
        return toContent(file);
    }

    private Content getRemotely(String location) {
        URI uri;
        try {
            uri = URI.create(location);

            // A known scheme is necessary if we're going to retrieve the item remotely
            String scheme = uri.getScheme();
            if (StringUtils.isEmpty(scheme) || (!scheme.equals("http") && !scheme.equals("https")))
                return null;
        } catch (IllegalArgumentException iae) {
            return null;
        }

        String storageLocation = "/remote/" + uri.getHost() + "/" + uri.getPath();

        // Check to see if we already have a cached copy of this file
        GridFSDBFile file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(storageLocation)));
        if (file != null) {
            LOG.debug("Retrieved remote resource " + location + " from " + storageLocation);
            // TODO: Add logic to check normal caching and time-to-live based on expires header
            DBObject metadata = file.getMetaData();
            return toContent(file);
        }

        HttpContext context = new BasicHttpContext();
        HttpGet get = new HttpGet(location);
        CloseableHttpResponse response = null;
        try {
            LOG.debug("Retrieving resource from " + location);
            response = client.execute(get, context);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BasicDBObject metadata = new BasicDBObject();
                metadata.put("originalFilename", location);
                gridFsOperations.store(entity.getContent(), storageLocation, entity.getContentType().getValue(), metadata);
                file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(storageLocation)));
            }
        } catch (Exception e) {
            LOG.error("Unable to retrieve remote resource", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ioe) {
                    LOG.error("Unable to close response", ioe);
                }
            }
        }

        return toContent(file);
    }

    private Content toContent(GridFsResource resource) throws IOException {
        String resourceId = resource.getId().toString();

        return new Content.Builder()
                .contentId(resourceId)
                .contentType(resource.getContentType())
                .location(resource.getFilename())
                .inputStream(resource.getInputStream())
                .lastModified(resource.lastModified())
                .length(Long.valueOf(resource.contentLength()))
                .build();
    }

}
