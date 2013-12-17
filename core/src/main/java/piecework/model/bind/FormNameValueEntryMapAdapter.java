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

import piecework.model.Value;
import piecework.common.ManyMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class FormNameValueEntryMapAdapter extends XmlAdapter<FormNameValueEntry[], Map<String, List<Value>>> {

    public FormNameValueEntryMapAdapter() {

    }

    public FormNameValueEntry[] marshal(Map<String, List<Value>> map) throws Exception {
        List<FormNameValueEntry> entries = new ArrayList<FormNameValueEntry>();
        if (map != null) {
            for (Map.Entry<String, List<Value>> entry : map.entrySet()) {
                entries.add(new FormNameValueEntry(entry.getKey(), entry.getValue()));
            }
        }
        return entries.toArray(new FormNameValueEntry[entries.size()]);
    }

    public ManyMap<String, Value> unmarshal(FormNameValueEntry[] entries) throws Exception {
        ManyMap<String, Value> map = new ManyMap<String, Value>();
        if (entries != null) {
            for (FormNameValueEntry entry : entries) {
                map.put(entry.getName(), entry.getValues());
            }
        }
        return map;
    }

}
