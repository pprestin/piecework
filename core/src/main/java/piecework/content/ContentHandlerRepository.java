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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import piecework.content.concrete.ContentHandlerRegistry;
import piecework.enumeration.Scheme;
import piecework.exception.PieceworkException;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.repository.ContentRepository;
import piecework.util.ContentUtility;
import piecework.util.PathUtility;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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
    public ContentResource checkoutByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException {
        ContentReceiver contentReceiver = lookupContentReceiver(modelProvider, null);
        return contentReceiver.checkout(modelProvider, location);
    }

    @Override
    public boolean expireByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException {
        ContentReceiver contentReceiver = lookupContentReceiver(modelProvider, null);
        // Return the result from the specified receiver (or primary receiver if key is null)
        boolean expired = contentReceiver.expire(modelProvider, location);

        // Backup receivers should also expire, but IOExceptions from them
        // should not bubble up
        backReceivers(new BackupReceiverExpire(modelProvider, location));
        return expired;
    }

    @Override
    public ContentResource findByLocation(ContentProfileProvider modelProvider, final String location) throws PieceworkException {
        if (StringUtils.isEmpty(location))
            return null;
        Scheme scheme = PathUtility.findScheme(location);

        List<ContentProvider> contentProviders = lookupContentProviders(modelProvider, scheme);

        for (ContentProvider contentProvider : contentProviders) {
            ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
            if (contentResource != null)
                return contentResource;
        }

        return null;
    }

    @Override
    public boolean publish(ProcessInstanceProvider instanceProvider) throws PieceworkException, IOException {
        ContentReceiver contentReceiver = lookupContentReceiver(instanceProvider, null);
        // Return the result from the specified receiver (or primary receiver if key is null)
        return contentReceiver.publish(instanceProvider);
    }

    @Override
    public boolean releaseByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException {
        ContentReceiver contentReceiver = lookupContentReceiver(modelProvider, null);
        // Return the result from the specified receiver (or primary receiver if key is null)
        return contentReceiver.release(modelProvider, location);
    }

    @Override
    public ContentResource replace(ContentProfileProvider modelProvider, ContentResource contentResource, String location) throws PieceworkException, IOException {
        ContentReceiver contentReceiver = lookupContentReceiver(modelProvider, contentResource);
        // Return the result from the specified receiver (or primary receiver if key is null)
        ContentResource saved = contentReceiver.replace(modelProvider, contentResource, location);
        // Backup receivers should also be saved to, but IOExceptions from them
        // should not bubble up
        backReceivers(new BackupReceiverReplace(modelProvider, contentResource, location));
        return saved;
    }

    @Override
    public ContentResource save(ContentProfileProvider modelProvider, ContentResource contentResource) throws PieceworkException, IOException {
        ContentReceiver contentReceiver = lookupContentReceiver(modelProvider, contentResource);
        // Return the result from the specified receiver (or primary receiver if key is null)
        ContentResource saved = contentReceiver.save(modelProvider, contentResource);
        // Backup receivers should also be saved to, but IOExceptions from them
        // should not bubble up
        backReceivers(new BackupReceiverSave(modelProvider, contentResource));
        return saved;
    }

    @Async
    private void backReceivers(BackupReceiverAction action) {
        Set<ContentReceiver> backupReceivers = contentHandlerRegistry.backupReceivers();
        if (backupReceivers != null && !backupReceivers.isEmpty()) {
            for (ContentReceiver backupReceiver : backupReceivers) {
                action.action(backupReceiver);
            }
        }
    }

    public ContentHandlerRegistry getContentHandlerRegistry() {
        return contentHandlerRegistry;
    }

    private <P extends ContentProfileProvider> List<ContentProvider> lookupContentProviders(P modelProvider, Scheme scheme) throws PieceworkException {
        // Note that the content receiver key is only used if the scheme is REPOSITORY -- the idea here is
        // that repository is for the storage of content, as opposed to CLASSPATH or FILESYSTEM, which are
        // intended to be readonly
        List<ContentProvider> contentProviders;

        if (scheme == Scheme.REPOSITORY || scheme == Scheme.REMOTE) {
            String contentHandlerKey = ContentUtility.contentHandlerKey(modelProvider);
            contentProviders = contentHandlerRegistry.providers(scheme, contentHandlerKey);
        } else {
            contentProviders = contentHandlerRegistry.providers(scheme);
        }

        return contentProviders;
    }

    private <P extends ContentProfileProvider> ContentReceiver lookupContentReceiver(P modelProvider, ContentResource contentResource) throws PieceworkException {
//        if (contentResource == null)
//            throw new InternalServerError(Constants.ExceptionCodes.system_misconfigured, "Trying to save null content object");

        String contentHandlerKey = ContentUtility.contentHandlerKey(modelProvider);

        // Return the result from the specified receiver (or primary receiver if key is null)
        return contentHandlerRegistry.contentReceiver(contentHandlerKey);
    }

    public abstract class BackupReceiverAction {

        final ContentProfileProvider modelProvider;

        BackupReceiverAction(ContentProfileProvider modelProvider) {
            this.modelProvider = modelProvider;
        }

        public abstract void action(ContentReceiver contentReceiver);

    }

    public class BackupReceiverExpire extends BackupReceiverAction {

        private final String location;

        public BackupReceiverExpire(ContentProfileProvider modelProvider, String location) {
            super(modelProvider);
            this.location = location;
        }

        public void action(ContentReceiver receiver) {
            try {
                // Don't bother to get back the result, since it won't be returned
                receiver.expire(modelProvider, location);
            } catch (Exception e) {
                LOG.error("Error expiring content for a backup receiver ", e);
            }
        }

    }

    public class BackupReceiverReplace extends BackupReceiverAction {

        private final ContentResource contentResource;
        private final String location;

        public BackupReceiverReplace(ContentProfileProvider modelProvider, ContentResource contentResource, String location) {
            super(modelProvider);
            this.contentResource = contentResource;
            this.location = location;
        }

        public void action(ContentReceiver receiver) {
            try {
                // Don't bother to get back the result, since it won't be returned
                receiver.replace(modelProvider, contentResource, location);
            } catch (Exception e) {
                LOG.error("Error saving content to a backup receiver ", e);
            }
        }

    }

    public class BackupReceiverSave extends BackupReceiverAction {

        private final ContentResource contentResource;

        public BackupReceiverSave(ContentProfileProvider modelProvider, ContentResource contentResource) {
            super(modelProvider);
            this.contentResource = contentResource;
        }

        public void action(ContentReceiver receiver) {
            try {
                // Don't bother to get back the result, since it won't be returned
                receiver.save(modelProvider, contentResource);
            } catch (Exception e) {
                LOG.error("Error saving content to a backup receiver ", e);
            }
        }

    }

}
