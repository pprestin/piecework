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
package piecework.model.bind;

import piecework.common.ManyMap;
import piecework.model.Message;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class FormNameMessageMapAdapter extends XmlAdapter<FormNameMessageEntry[], Map<String, List<Message>>> {

    public FormNameMessageMapAdapter() {

    }

    public FormNameMessageEntry[] marshal(Map<String, List<Message>> map) throws Exception {
        List<FormNameMessageEntry> entries = new ArrayList<FormNameMessageEntry>();
        if (map != null) {
            for (Map.Entry<String, List<Message>> entry : map.entrySet()) {
                entries.add(new FormNameMessageEntry(entry.getKey(), entry.getValue()));
            }
        }
        return entries.toArray(new FormNameMessageEntry[entries.size()]);
    }

    public ManyMap<String, Message> unmarshal(FormNameMessageEntry[] entries) throws Exception {
        ManyMap<String, Message> map = new ManyMap<String, Message>();
        if (entries != null) {
            for (FormNameMessageEntry entry : entries) {
                map.put(entry.getName(), entry.getMessages());
            }
        }
        return map;
    }

}
