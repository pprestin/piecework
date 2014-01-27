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
package piecework.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ManyMapSet;

import java.util.Date;
import java.util.Set;

/**
 * @author James Renfro
 */
@Document(collection = "cache.event")
public class CacheEvent {

    @Id
    private String id;

    @Indexed
    private String cacheAgentId;

    @Indexed(expireAfterSeconds = 60)
    private Date eventDate;

    private ManyMapSet<String, String> cacheKeyMap;

    public CacheEvent() {

    }

    public CacheEvent(String cacheAgentId, ManyMapSet<String, String> cacheKeyMap) {
        this.cacheAgentId = cacheAgentId;
        this.cacheKeyMap = cacheKeyMap;
        this.eventDate = new Date();
    }

    public String getId() {
        return id;
    }

    public String getCacheAgentId() {
        return cacheAgentId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public ManyMapSet<String, String> getCacheKeyMap() {
        return cacheKeyMap;
    }
}
