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
package piecework.engine;

import piecework.Registrant;

/**
 * @author James Renfro
 */
public interface ProcessEngineProxy extends Registrant<ProcessEngineProxy>, ProcessEngineCapabilities {

    String template = "<div class=\"main-content container\" data-ng-switch on=\"form.layout\">\n" +
            "        <div class=\"row\" data-ng-switch-when=\"multipage\">\n" +
            "            <wf-multipage form=\"form\" state=\"state\"></wf-multipage>\n" +
            "        </div>\n" +
            "        <div class=\"row\" data-ng-switch-when=\"multistep\">\n" +
            "            <wf-multistep form=\"form\" state=\"state\"></wf-multistep>\n" +
            "        </div>\n" +
            "        <div class=\"row\" data-ng-switch-when=\"review\">\n" +
            "            <wf-review form=\"form\" state=\"state\"></wf-review>\n" +
            "        </div>\n" +
            "        <div class=\"row\" data-ng-switch-when=\"normal\">\n" +
            "            <wf-container form=\"form\" container=\"form.container\" state=\"state\"/>\n" +
            "        </div>\n" +
            "    </div>";
}
