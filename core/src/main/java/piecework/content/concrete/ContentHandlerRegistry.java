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

    private Map<Scheme, ContentProvider> contentProviderPrimaryMap;
    private ManyMap<Scheme, ContentProvider> contentProviderBackupMap;
    private ContentReceiver contentReceiver;
    private Set<ContentReceiver> contentReceiverBackupSet;

    private Storage storage;

    public ContentHandlerRegistry(ContentProviderVoter contentProviderVoter, ContentReceiverVoter contentReceiverVoter) {
        this.contentProviderVoter = contentProviderVoter;
        this.contentReceiverVoter = contentReceiverVoter;
        this.contentProviderPrimaryMap = new Hashtable<Scheme, ContentProvider>();
        this.contentProviderBackupMap = new ManyMap<Scheme, ContentProvider>();
        this.contentReceiverBackupSet = new HashSet<ContentReceiver>();
    }

    public void init() {
        this.storage = new Storage(contentProviderPrimaryMap, contentProviderBackupMap, contentReceiver, contentReceiverBackupSet);
    }

    public List<ContentProvider> providers(Scheme scheme) {
        if (storage == null)
            throw new RuntimeException("Storage is not initialized");
        return storage.providers(scheme);
    }

    public ContentReceiver primaryReceiver() {
        if (storage == null)
            throw new RuntimeException("Storage is not initialized");
        return storage.primaryReceiver();
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
        }
    }

    /*
     * Internal immutable to hold data for retrieval
     */
    public class Storage {
        private final Map<Scheme, List<ContentProvider>> contentProviderMap;
        private final ContentReceiver contentReceiver;
        private final Set<ContentReceiver> contentReceiverBackupSet;

        public Storage(Map<Scheme, ContentProvider> contentProviderPrimaryMap,
                                   ManyMap<Scheme, ContentProvider> contentProviderBackupMap,
                                   ContentReceiver contentReceiver,
                                   Set<ContentReceiver> contentReceiverBackupSet) {

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

            this.contentProviderMap = temporaryProviderMap.unmodifiableMap();
            this.contentReceiver = contentReceiver;
            this.contentReceiverBackupSet = Collections.unmodifiableSet(contentReceiverBackupSet);
        }

        public List<ContentProvider> providers(Scheme scheme) {
            List<ContentProvider> providers = this.contentProviderMap.get(scheme);
            if (providers == null)
                return Collections.emptyList();
            return providers;
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
