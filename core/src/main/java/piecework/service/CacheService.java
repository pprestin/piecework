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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import piecework.enumeration.CacheName;

/**
 * This is just a way to keep track of the different caches available
 * in the system. It delegates everything to the Spring cache abstraction.
 *
 * @author James Renfro
 */
@Service
public class CacheService {

    @Autowired
    CacheManager cacheManager;

    public Cache.ValueWrapper get(CacheName cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName.name());
        return cache.get(key);
    }

    public void put(CacheName cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName.name());
        cache.put(key, value);
    }

    public void evict(CacheName cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName.name());
        cache.evict(key);
    }

}
