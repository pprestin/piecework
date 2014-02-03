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
package piecework.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import piecework.common.ManyMapSet;
import piecework.enumeration.CacheName;
import piecework.model.CacheEvent;
import piecework.persistence.CacheEventRepository;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * This is just a way to keep track of the different caches available
 * in the system. It delegates everything to the Spring cache abstraction.
 *
 * @author James Renfro
 */
@Service
public class CacheService {

    private static final Logger LOG = Logger.getLogger(CacheService.class);

    @Autowired
    CacheManager cacheManager;

    @Autowired
    CacheEventRepository cacheEventRepository;

    // Populated on startup to uniquely identify this instance
    private static String CACHE_AGENT_ID;

    private ManyMapSet<String, String> cacheKeyMap;

    @PostConstruct
    public void init() {
        CACHE_AGENT_ID = UUID.randomUUID().toString();
        this.cacheKeyMap = new ManyMapSet<String, String>();
    }

    @Scheduled(fixedRate=5000)
    public synchronized void runEveryFiveSeconds() {
        if (this.cacheKeyMap != null && !this.cacheKeyMap.isEmpty()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Saving cache event as " + CACHE_AGENT_ID);

            CacheEvent event = new CacheEvent(CACHE_AGENT_ID, new ManyMapSet<String, String>(cacheKeyMap));
            cacheEventRepository.save(event);
            this.cacheKeyMap = new ManyMapSet<String, String>();
        }
    }

    @Scheduled(fixedRate=30000)
    public void runEveryThirtySeconds() {
        List<CacheEvent> events = cacheEventRepository.findAllOtherCacheAgentEvents(CACHE_AGENT_ID);
        long thirtySecondsAgo = System.currentTimeMillis() - 30000;
        if (events != null) {
            for (CacheEvent event : events) {
                long eventTime = event.getEventDate() != null ? event.getEventDate().getTime() : -1;
                if (eventTime > thirtySecondsAgo) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Processing cache event as " + CACHE_AGENT_ID);

                    ManyMapSet<String, String> eventCacheKeyMap = event.getCacheKeyMap();
                    if (eventCacheKeyMap != null && !eventCacheKeyMap.isEmpty()) {
                        for (Map.Entry<String, Set<String>> entry : eventCacheKeyMap.entrySet()) {
                            evict(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }
    }

    public Cache.ValueWrapper get(CacheName cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName.name());
        return cache != null ? cache.get(key) : null;
    }

    public synchronized void put(CacheName cacheName, String key, Object value) {
        String cacheId = cacheName.name();
        Cache cache = cacheManager.getCache(cacheId);
        if (cache != null) {
            cache.put(key, value);

            if (!cacheName.isLocal())
                this.cacheKeyMap.putOne(cacheId, key);
        }
    }

    public void evict(CacheName cacheName, String key) {
        evict(cacheName.name(), Collections.singleton(key));
    }

    public synchronized void evict(String cacheName, Set<String> keys) {
        if (keys == null || keys.size() <= 0)
            return;

        if (LOG.isDebugEnabled())
            LOG.debug("Evicting " + keys.size() + " keys from " + cacheName);

        Cache cache = cacheManager.getCache(cacheName);
        for (String key : keys) {
            cache.evict(key);
        }
    }

}
