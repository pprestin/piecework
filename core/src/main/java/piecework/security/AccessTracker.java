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
package piecework.security;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.common.AccessLog;
import piecework.enumeration.CacheName;
import piecework.model.AccessEvent;
import piecework.model.Entity;
import piecework.model.ProcessInstance;
import piecework.model.RequestDetails;
import piecework.persistence.AccessEventRepository;
import piecework.service.CacheService;

import javax.annotation.PostConstruct;

/**
 * @author James Renfro
 */
@Service
public class AccessTracker {

    private static final Logger LOG = Logger.getLogger(AccessTracker.class);

    @Autowired
    AccessEventRepository accessEventRepository;

    @Autowired
    CacheService cacheService;

    @Autowired
    Environment environment;

    private long accessCacheInterval;
    private long accessCountLimit;

    @PostConstruct
    public void init() {
        // Default to 1000 calls in 5 minutes
        Long intervalInMinutes = environment.getProperty("access.cache.interval.minutes", Long.class, Long.valueOf(5l));
        this.accessCacheInterval = intervalInMinutes.longValue() * 60 * 1000;
        this.accessCountLimit = environment.getProperty("access.count.limit", Long.class, Long.valueOf(1000));
    }

    public void track(RequestDetails requestDetails, boolean isUpdate, boolean isAnonymous) {
        CacheName cacheName = isAnonymous ? CacheName.ACCESS_ANONYMOUS : CacheName.ACCESS_AUTHENTICATED;
        String key = isAnonymous ? requestDetails.getRemoteAddr() : requestDetails.getRemoteUser();

        Cache.ValueWrapper wrapper = cacheService.get(cacheName, key);

        AccessLog accessLog = null;
        AccessLog previousAccessLog = null;
        if (wrapper != null) {
            previousAccessLog = AccessLog.class.cast(wrapper.get());

            if (previousAccessLog != null && !previousAccessLog.isExpired(accessCacheInterval)) {
                accessLog = new AccessLog(previousAccessLog);
            }
        }

        if (accessLog == null)
            accessLog = new AccessLog(accessCountLimit);

        cacheService.put(cacheName, key, accessLog);

        if (accessLog.isAlarming())
            LOG.warn("Access attempt " + accessLog.getAccessCount() + " by " + key);
    }

    public void track(ProcessInstance instance, String secretId, String key, String reason, Entity principal) {
        accessEventRepository.save(new AccessEvent(instance, secretId, key, reason, principal));
    }

}
