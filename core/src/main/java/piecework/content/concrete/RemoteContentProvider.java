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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.stereotype.Service;
import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.util.ContentUtility;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author James Renfro
 */
@Service
public class RemoteContentProvider extends GridFSContentProviderReceiver {

    private static final Logger LOG = Logger.getLogger(RemoteContentProvider.class);

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
    public Content findByPath(piecework.model.Process process, String base, String location) {
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
            return ContentUtility.toContent(file);
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

        return ContentUtility.toContent(file);
    }

    @Override
    public Scheme getScheme() {
        return Scheme.REMOTE;
    }

    @Override
    public String getKey() {
        return "default-remote";
    }

}
