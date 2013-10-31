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

import piecework.enumeration.FlowElementType;

/**
 * @author James Renfro
 */
public class FlowElement implements Comparable<FlowElement> {

    private final String id;
    private final String label;
    private final FlowElementType type;

    public FlowElement() {
        this(null, null, null);
    }

    public FlowElement(String id, String label, FlowElementType type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public FlowElementType getType() {
        return type;
    }

    @Override
    public int compareTo(FlowElement o) {
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
        FlowElement other = FlowElement.class.cast(o);

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
