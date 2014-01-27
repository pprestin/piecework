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
package piecework.enumeration;

/**
 * @author James Renfro
 */
public enum CacheName {
    ACCESS_ANONYMOUS(true),
    ACCESS_AUTHENTICATED(true),
    GROUP(true),
    PROCESS,
    PROCESS_BASIC,
    PROCESS_DEPLOYMENT,
    SCRIPT(true),
    STYLESHEET(true),
    MULTI_USER(true),
    IDENTITY(true);

    private boolean local;

    private CacheName() {
        this.local = false;
    }

    private CacheName(boolean local) {
        this.local = local;
    }

    public boolean isLocal() {
        return local;
    }
}
