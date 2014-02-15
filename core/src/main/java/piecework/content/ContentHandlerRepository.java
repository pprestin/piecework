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
package piecework.content;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.content.concrete.ContentHandlerRegistry;
import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.util.PathUtility;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Implementation of ContentRepository that finds any ContentProvider beans in the
 * application context and delegates to them to get content. To store content
 * it will delegate to a bean that implements ContentReceiver -- generally this
 * will be implemented by one the beans that implements ContentProvider
 *
 *
 * @author James Renfro
 */
@Service
public class ContentHandlerRepository implements ContentRepository {

    private static final Logger LOG = Logger.getLogger(ContentHandlerRepository.class);

    @Autowired(required = false)
    private ContentProviderVoter contentProviderVoter;

    @Autowired(required = false)
    private ContentReceiverVoter contentReceiverVoter;

    // Must be at least one provider in the application context
    @Autowired(required = true)
    private ContentProvider[] providers;

    // Must be at least one content receiver in the application context
    @Autowired(required = true)
    private ContentReceiver[] receivers;

    private ContentHandlerRegistry contentHandlerRegistry;

    @PostConstruct
    public void init() {
        this.contentHandlerRegistry = new ContentHandlerRegistry(contentProviderVoter, contentReceiverVoter);
        this.contentHandlerRegistry.registerProviders(providers);
        this.contentHandlerRegistry.registerReceivers(receivers);
        this.contentHandlerRegistry.init();
    }

    @Override
    public Content findByLocation(Process process, String location) {
        return findByLocation(process, null, location);
    }

    @Override
    public Content findByLocation(final Process process, final String base, final String location) {
        if (StringUtils.isEmpty(location))
            return null;

        String contentReceiverKey = null;

        if (StringUtils.isEmpty(contentReceiverKey) && process != null && StringUtils.isNotEmpty(process.getContentReceiverKey()))
            contentReceiverKey = process.getContentReceiverKey();

        String path;

        if (StringUtils.isNotEmpty(base))
            path = base + "/" + location;
        else
            path = location;

        Scheme scheme = PathUtility.findScheme(path);
        List<ContentProvider> contentProviders = contentHandlerRegistry.providers(scheme, contentReceiverKey);

        for (ContentProvider contentProvider : contentProviders) {
            try {
                Content content = contentProvider.findByPath(process, base, location);
                if (content != null)
                    return content;
            } catch (IOException e) {
                LOG.error("Could not retrieve content from provider " + contentProvider.toString(), e);
            }
        }

        return null;
    }

    @Override
    public Content save(Process process, Content content, Entity principal) throws IOException {
        String contentReceiverKey = null;

        if (content.getMetadata() != null)
            contentReceiverKey = content.getMetadata().get(Constants.ContentMetadataKeys.CONTENT_RECEIVER);

        if (StringUtils.isEmpty(contentReceiverKey) && StringUtils.isNotEmpty(process.getContentReceiverKey()))
            contentReceiverKey = process.getContentReceiverKey();

        // Return the result from the specified receiver (or primary receiver if key is null)
        ContentReceiver contentReceiver = contentHandlerRegistry.contentReceiver(contentReceiverKey);
        Content saved = contentReceiver.save(content, principal);
        // Backup receivers should also be saved to, but IOExceptions from them
        // should not bubble up
        Set<ContentReceiver> backupReceivers = contentHandlerRegistry.backupReceivers();
        if (backupReceivers != null && !backupReceivers.isEmpty()) {
            for (ContentReceiver backupReceiver : backupReceivers) {
                try {
                    // Don't bother to get back the result, since it won't be returned
                    backupReceiver.save(content, principal);
                } catch (IOException ioe) {
                    LOG.error("Error saving content to a backup receiver");
                }
            }
        }
        return saved;
    }

    public ContentHandlerRegistry getContentHandlerRegistry() {
        return contentHandlerRegistry;
    }
}
