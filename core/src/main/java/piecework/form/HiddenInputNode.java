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
package piecework.form;

import org.htmlcleaner.TagNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
public class HiddenInputNode extends TagNode {

    public HiddenInputNode(String name, String value) {
        super("input");
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("name", name);
        attributes.put("type", "hidden");
        attributes.put("value", value);
        this.setAttributes(attributes);
    }

}
