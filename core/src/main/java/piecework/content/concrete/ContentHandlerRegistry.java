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

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import piecework.common.ManyMap;
import piecework.content.ContentProvider;
import piecework.content.ContentProviderVoter;
import piecework.content.ContentReceiver;
import piecework.content.ContentReceiverVoter;
import piecework.enumeration.ContentHandlerPriority;
import piecework.enumeration.Scheme;

import java.util.*;

/**
 * @author James Renfro
 */
public class ContentHandlerRegistry {

    private static final Logger LOG = Logger.getLogger(ContentHandlerRegistry.class);

    private final ContentProviderVoter contentProviderVoter;
    private final ContentReceiverVoter contentReceiverVoter;

    private Map<String, ContentProvider> contentProviderKeyMap;
    private Map<Scheme, ContentProvider> contentProviderPrimaryMap;
    private ManyMap<Scheme, ContentProvider> contentProviderBackupMap;
    private Map<String, ContentReceiver> contentReceiverKeyMap;
    private ContentReceiver contentReceiver;
    private Set<ContentReceiver> contentReceiverBackupSet;

    private Storage storage;

    public ContentHandlerRegistry(ContentProviderVoter contentProviderVoter, ContentReceiverVoter contentReceiverVoter) {
        this.contentProviderVoter = contentProviderVoter;
        this.contentReceiverVoter = contentReceiverVoter;
        this.contentProviderKeyMap = new Hashtable<String, ContentProvider>();
        this.contentProviderPrimaryMap = new Hashtable<Scheme, ContentProvider>();
        this.contentProviderBackupMap = new ManyMap<Scheme, ContentProvider>();
        this.contentReceiverBackupSet = new HashSet<ContentReceiver>();
        this.contentReceiverKeyMap = new Hashtable<String, ContentReceiver>();
    }

    public void init() {
        this.storage = new Storage(contentProviderKeyMap, contentProviderPrimaryMap, contentProviderBackupMap, contentReceiver, contentReceiverBackupSet, contentReceiverKeyMap);
    }

    public List<ContentProvider> providers(Scheme scheme) {
        if (storage == null)
            throw new RuntimeException("Storage is not initialized");
        return storage.providers(scheme);
    }

    public List<ContentProvider> providers(Scheme scheme, String key) {
        if (storage == null)
            throw new RuntimeException("Storage is not initialized");
        return storage.providers(scheme, key);
    }

    public ContentReceiver primaryReceiver() {
        if (storage == null)
            throw new RuntimeException("Storage is not initialized");
        return storage.primaryReceiver();
    }

    public ContentReceiver contentReceiver(String key) {
        if (storage == null)
            throw new RuntimeException("Storage is not initialized");
        return storage.receiver(key);
    }

    public Set<ContentReceiver> backupReceivers() {
        if (storage == null)
            throw new RuntimeException("Storage is not initialized");
        return storage.backupReceivers();
    }

    public <P extends ContentProvider> void registerProviders(P... providers) {
        // Sanity check
        if (providers == null)
            return;

        // Loop through all the providers and decide which is primary and which are backup for
        // a given scheme
        for (P provider : providers) {
            Scheme scheme = provider.getScheme();
            ContentHandlerPriority priority = ContentHandlerPriority.PRIMARY;

            if (contentProviderVoter == null) {
                // By default, make any additional providers after the first one a backup
                // even though this will lead to unreliable choices if multiple providers
                // are injected for a specific scheme and no ContentProviderVoter is implemented -
                // at least then we won't fail to look
                if (contentProviderPrimaryMap.containsKey(scheme))
                    priority = ContentHandlerPriority.BACKUP;
            } else {
                priority = contentProviderVoter.vote(provider);
            }

            switch (priority) {
                case PRIMARY:
                    contentProviderPrimaryMap.put(scheme, provider);
                    break;
                case BACKUP:
                    contentProviderBackupMap.putOne(scheme, provider);
                    break;
            }

            if (provider.getKey() != null)
                contentProviderKeyMap.put(provider.getKey(), provider);
        }
    }

    public <R extends ContentReceiver> void registerReceivers(R... receivers) {
        // Sanity check
        if (receivers == null)
            return;

        for (R receiver : receivers) {
            ContentHandlerPriority priority = ContentHandlerPriority.PRIMARY;

            if (contentReceiverVoter == null) {
                if (contentReceiver != null)
                    priority = ContentHandlerPriority.BACKUP;
            } else {
                priority = contentReceiverVoter.vote(receiver);
            }

            switch (priority) {
                case PRIMARY:
                    contentReceiver = receiver;
                    break;
                case BACKUP:
                    contentReceiverBackupSet.add(receiver);
                    break;
            }

            if (receiver.getKey() != null)
                contentReceiverKeyMap.put(receiver.getKey(), receiver);
        }
    }

    /*
     * Internal immutable to hold data for retrieval
     */
    public class Storage {
        private final Map<String, ContentProvider> contentProviderKeyMap;
        private final Map<Scheme, List<ContentProvider>> contentProviderMap;
        private final ContentReceiver contentReceiver;
        private final Set<ContentReceiver> contentReceiverBackupSet;
        private final Map<String, ContentReceiver> contentReceiverKeyMap;

        public Storage(Map<String, ContentProvider> contentProviderKeyMap,
                       Map<Scheme, ContentProvider> contentProviderPrimaryMap,
                       ManyMap<Scheme, ContentProvider> contentProviderBackupMap,
                       ContentReceiver contentReceiver,
                       Set<ContentReceiver> contentReceiverBackupSet,
                       Map<String, ContentReceiver> contentReceiverKeyMap) {

            Map<String, ContentProvider> temporaryProviderKeyMap = new HashMap<String, ContentProvider>();
            // Build a new many map that has the primary first, followed by any backups
            ManyMap<Scheme, ContentProvider> temporaryProviderMap = new ManyMap<Scheme, ContentProvider>();
            if (contentProviderPrimaryMap != null && !contentProviderPrimaryMap.isEmpty()) {
                for (Map.Entry<Scheme, ContentProvider> entry : contentProviderPrimaryMap.entrySet()) {
                    ContentProvider primary = entry.getValue();
                    List<ContentProvider> backups = contentProviderBackupMap.get(entry.getKey());
                    List<ContentProvider> providers = new ArrayList<ContentProvider>();
                    providers.add(primary);
                    if (backups != null && !backups.isEmpty())
                        providers.addAll(backups);
                    temporaryProviderMap.put(entry.getKey(), providers);
                }
            }

            this.contentProviderKeyMap = Collections.unmodifiableMap(contentProviderKeyMap);
            this.contentProviderMap = temporaryProviderMap.unmodifiableMap();
            this.contentReceiver = contentReceiver;
            this.contentReceiverBackupSet = Collections.unmodifiableSet(contentReceiverBackupSet);
            this.contentReceiverKeyMap = Collections.unmodifiableMap(contentReceiverKeyMap);
        }

        public List<ContentProvider> providers(Scheme scheme, String key) {
            if (key == null || contentProviderKeyMap == null || contentProviderKeyMap.isEmpty())
                return providers(scheme);
            ContentProvider contentProvider = scheme == Scheme.REPOSITORY ? contentProviderKeyMap.get(key) : null;
            List<ContentProvider> allProviders = new ArrayList<ContentProvider>();
            if (contentProvider != null)
                allProviders.add(contentProvider);
            List<ContentProvider> otherProviders = providers(scheme);
            if (otherProviders != null && !otherProviders.isEmpty())
                allProviders.addAll(otherProviders);
            return allProviders;
        }

        public List<ContentProvider> providers(Scheme scheme) {
            List<ContentProvider> providers = this.contentProviderMap.get(scheme);
            if (providers == null)
                return Collections.emptyList();
            return providers;
        }

        public ContentReceiver receiver(String key) {
            if (key == null || contentReceiverKeyMap == null || contentReceiverKeyMap.isEmpty() || !contentReceiverKeyMap.containsKey(key))
                return primaryReceiver();

            return contentReceiverKeyMap.get(key);
        }

        public ContentReceiver primaryReceiver() {
            return this.contentReceiver;
        }

        public Set<ContentReceiver> backupReceivers() {
            if (this.contentReceiverBackupSet == null)
                return Collections.emptySet();
            return this.contentReceiverBackupSet;
        }

    }

}
