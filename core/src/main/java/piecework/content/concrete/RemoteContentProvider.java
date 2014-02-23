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

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ContentUtility;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;

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
//        CacheConfig cacheConfig = CacheConfig.custom()
//                .build();
//        CachingHttpClientBuilder builder = CachingHttpClients.custom();
//        builder.setConnectionManager(cm);
//        builder.setCacheConfig(cacheConfig);
//        this.client = builder.build();
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
    public Content findByPath(Process process, String base, String location, Entity principal) {
        String url = "";
        if (StringUtils.isNotEmpty(base))
            url += base;
        if (StringUtils.isNotEmpty(location))
            url += location;

        URI uri;
        try {
            uri = URI.create(url);

            // A known scheme is necessary if we're going to retrieve the item remotely
            String scheme = uri.getScheme();
            if (StringUtils.isEmpty(scheme) || (!scheme.equals("http") && !scheme.equals("https")))
                return null;
        } catch (IllegalArgumentException iae) {
            return null;
        }

        //String storageLocation = "/remote/" + uri.getHost() + "/" + uri.getPath();

//        // Check to see if we already have a cached copy of this file
//        GridFSDBFile file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(storageLocation)));
//        if (file != null) {
//            LOG.debug("Retrieved remote resource " + location + " from " + storageLocation);
//            // TODO: Add logic to check normal caching and time-to-live based on expires header
//            DBObject metadata = file.getMetaData();
//            Date uploadDate = file.getUploadDate();
//            return ContentUtility.toContent(file);
//        }

        return ContentUtility.toContent(client, uri);
    }


//    private Content retrieve(URI uri) {
//        GridFSDBFile file = null;
////        HttpContext context = new BasicHttpContext();
//        HttpCacheContext context = HttpCacheContext.create();
//        HttpGet get = new HttpGet(uri);
//        CloseableHttpResponse response = null;
//        try {
//            LOG.info("Retrieving resource from " + uri.toString());
//            response = client.execute(get, context);
//            HttpEntity entity = response.getEntity();
//            Header lastModifiedHeader = response.getFirstHeader("Last-Modified");
//            Date lastModified = lastModifiedHeader != null && StringUtils.isNotEmpty(lastModifiedHeader.getValue()) ? DateUtils.parseDate(lastModifiedHeader.getValue()) : null;
//            Header eTagHeader = response.getFirstHeader("ETag");
//            String eTag = eTagHeader != null ? eTagHeader.getValue() : null;
//
//            return ContentUtility.toContent(uri, entity, lastModified, eTag);
////            if (entity != null) {
////                BasicDBObject metadata = new BasicDBObject();
////                metadata.put("originalFilename", location);
////                gridFsOperations.store(entity.getContent(), storageLocation, entity.getContentType().getValue(), metadata);
////                file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(storageLocation)));
////            }
//        } catch (Exception e) {
//            LOG.error("Unable to retrieve remote resource", e);
//        } finally {
//            if (response != null) {
//                try {
//                    response.close();
//                } catch (IOException ioe) {
//                    LOG.error("Unable to close response", ioe);
//                }
//            }
//        }
//
//        return null;
//    }


    @Override
    public Scheme getScheme() {
        return Scheme.REMOTE;
    }

    @Override
    public String getKey() {
        return "default-remote";
    }

}
