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

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import piecework.repository.AccessEventRepository;
import piecework.service.CacheService;
import piecework.settings.NotificationSettings;

/**
 * @author James Renfro
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class AccessTrackerTest {

    @InjectMocks
    AccessTracker accessTracker;

    @Mock
    AccessEventRepository accessEventRepository;

    @Mock
    CacheService cacheService;

    @Mock
    Environment environment;

    @Mock
    NotificationSettings notificationSettings;

    @Test
    public void verify() {

    }

}
