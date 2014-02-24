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
package piecework.security.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import piecework.repository.config.MockRepositoryConfiguration;
import piecework.security.AccessTracker;
import piecework.security.data.DataFilterService;
import piecework.service.CacheService;
import piecework.settings.NotificationSettings;
import piecework.settings.UserInterfaceSettings;

/**
 * @author James Renfro
 */
@Configuration
@Import({EncryptionTestConfiguration.class, MockRepositoryConfiguration.class})
public class DataFilterTestConfiguration {

    @Bean
    public AccessTracker accessTracker() {
        return new AccessTracker();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public CacheService cacheService() {
        return new CacheService();
    }

    @Bean
    public DataFilterService dataFilterService() {
        return new DataFilterService();
    }

    @Bean
    public NotificationSettings notificationSettings() {
        return new NotificationSettings();
    }

    @Bean
    public UserInterfaceSettings userInterfaceSettings() {
        return new UserInterfaceSettings();
    }

}
