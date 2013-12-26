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
package piecework.common;

import java.util.Date;

/**
 * @author James Renfro
 */
public class AccessLog {

    private final long startTimeInMillis;
    private final long accessCount;
    private final long countLimit;

    public AccessLog(long countLimit) {
        this.startTimeInMillis = System.currentTimeMillis();
        this.accessCount = 1l;
        this.countLimit = countLimit;
    }

    public AccessLog(AccessLog previous) {
        this.startTimeInMillis = previous.getStartTimeInMillis();
        this.accessCount = previous.getAccessCount() + 1;
        this.countLimit = previous.getCountLimit();
    }

    // Only alarm when we hit a modulo of the count limit
    public boolean isAlarming() {
        return accessCount % countLimit == 0;
    }

    public boolean isExpired(long interval) {
        return startTimeInMillis + interval < System.currentTimeMillis();
    }

    public long getStartTimeInMillis() {
        return startTimeInMillis;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public long getCountLimit() {
        return countLimit;
    }
}
