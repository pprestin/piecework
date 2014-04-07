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
package piecework.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Service;

/**
 * @author James Renfro
 */
public class CustomCacheManager extends net.sf.ehcache.CacheManager {

    private static final Logger LOG = Logger.getLogger(CustomCacheManager.class);

    @Override
    public Ehcache getEhcache(String name) throws IllegalStateException {
        addCacheIfMissing(name);
        return super.getEhcache(name);
    }

    @Override
    public net.sf.ehcache.Cache getCache(String name) throws IllegalStateException, ClassCastException {
        addCacheIfMissing(name);
        return super.getCache(name);
    }

    private void addCacheIfMissing(String name) {
        if (!cacheExists(name)) {
            LOG.info("Creating a new cache for " + name);
            CacheConfiguration configuration = new CacheConfiguration();
            configuration.setName(name);
            configuration.setTimeToIdleSeconds(300);
            configuration.setTimeToLiveSeconds(600);
            configuration.setMaxEntriesLocalHeap(10000);
            Ehcache ehcache = new net.sf.ehcache.Cache(configuration);
            addCacheIfAbsent(ehcache);
        }
    }

}
