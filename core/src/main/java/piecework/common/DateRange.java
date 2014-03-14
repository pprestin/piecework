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
package piecework.common;

/**
 * @author James Renfro
 */
public class DateRange {

    private String after;
    private String before;

    public DateRange() {

    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        if (after.contains("\""))
            after = after.replaceAll("\"", "");
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        if (before.contains("\""))
            before = before.replaceAll("\"", "");
        this.before = before;
    }
}
