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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.ContentProfileProvider;
import piecework.util.ContentUtility;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class RemoteContentProvider extends GridFSContentProviderReceiver {

    private static final Logger LOG = Logger.getLogger(RemoteContentProvider.class);
    private static final Set<String> VALID_URI_SCHEMES = Sets.newHashSet("http", "https");

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
    public Content findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException {
        ContentProfile contentProfile = modelProvider.contentProfile();

        // Should never use RemoteContentProvider unless the content profile explicitly
        // whitelists specific URLs
        if (contentProfile == null)
            return null;

        Set<String> remoteResourceLocations = contentProfile.getRemoteResourceLocations();

        URI uri;
        try {
            uri = URI.create(location);

            // Ensure that code does not try to access a local URI
            if (!uri.isAbsolute()) {
                LOG.error("Attempting to resolve a relative uri");
                throw new ForbiddenError();
            }

            // Ensure that code only tries to access uris with valid schemes
            ContentUtility.validateScheme(uri, VALID_URI_SCHEMES);
            ContentUtility.validateRemoteLocation(remoteResourceLocations, uri);

            return ContentUtility.toContent(client, uri);
        } catch (IllegalArgumentException iae) {
            LOG.error("Caught exception trying to find by remote location", iae);
            return null;
        }
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
