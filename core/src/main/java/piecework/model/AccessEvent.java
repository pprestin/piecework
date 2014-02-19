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
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author James Renfro
 */
@Document(collection = "access.event")
public class AccessEvent {

    @Id
    private final String accessEventId;

    private final String processDefinitionKey;

    private final String processInstanceId;

    private final String secretId;

    private final String key;

    private final String entityId;

    private final Date accessDate;

    private final String reason;

    private final boolean isAnonymousAllowed;

    public AccessEvent() {
        this(null, null, null, null, null, false);
    }

    public AccessEvent(ProcessInstance instance, String secretId, String key, String reason, Entity entity, boolean isAnonymousAllowed) {
        this.accessEventId = null;
        this.processDefinitionKey = instance != null ? instance.getProcessDefinitionKey() : null;
        this.processInstanceId = instance != null ? instance.getProcessInstanceId() : null;
        this.secretId = secretId;
        this.key = key;
        this.reason = reason;
        this.entityId = entity != null ? entity.getEntityId() : null;
        this.accessDate = new Date();
        this.isAnonymousAllowed = isAnonymousAllowed;
    }

    public String getAccessEventId() {
        return accessEventId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getKey() {
        return key;
    }

    public String getEntityId() {
        return entityId;
    }

    public Date getAccessDate() {
        return accessDate;
    }

    public String getReason() {
        return reason;
    }

}
