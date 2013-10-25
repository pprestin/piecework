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

import java.io.Serializable;

/**
 * @author James Renfro
 */
public class ActivityResponse implements Serializable {

    private final Container container;
    private final String location;

    public ActivityResponse() {
        this(null, null);
    }

    public ActivityResponse(Container container, String location) {
        this.container = container;
        this.location = location;
    }

    public Container getContainer() {
        return container;
    }

    public String getLocation() {
        return location;
    }
}
