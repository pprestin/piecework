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
package piecework.ui;

/**
 * @author James Renfro
 */
public class TagAttributeAction {

    public enum TagAttributeActionType { REMOVE, MODIFY, LEAVE }

    private final TagAttributeActionType type;
    private final String name;
    private final String value;
    private final String replace;

    public TagAttributeAction(TagAttributeActionType type, String name, String value, String replace) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.replace = replace;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getReplace() {
        return replace;
    }

    public TagAttributeActionType getType() {
        return type;
    }
}
