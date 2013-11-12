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
import org.apache.cxf.common.util.StringUtils;
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
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import piecework.persistence.ContentRepository;
import piecework.model.Content;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
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
        if (location == null)
            return null;

        String storageLocation = location;
        GridFSDBFile file = null;

        int indexOf = storageLocation.indexOf("://");
        if (indexOf == -1 || (indexOf + 3 ) >= storageLocation.length()) {
            // If the storage location doesn't have a :// it's not remote so just retrieve it
            file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(storageLocation)));
        } else {
            // A known protocol is necessary if we're going to retrieve the item remotely
            String protocol = location.substring(0, indexOf);
            if (StringUtils.isEmpty(protocol) || (!protocol.equals("http") && !protocol.equals("https")))
                return null;

            storageLocation = "/remote/" + location.substring((indexOf+3));

            // Check to see if we already have a cached copy of this file
            file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(storageLocation)));
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
        }

        if (file == null)
            return null;

        return toContent(file);
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
