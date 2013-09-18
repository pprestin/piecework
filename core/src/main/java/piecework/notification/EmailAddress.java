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
package piecework.notification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class EmailAddress {

    private final String address;
    private final String name;

    public EmailAddress(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public static EmailAddress parse(String email) {
        Pattern pattern = Pattern.compile("\"?([^<|\"]*)\"?\\s+<\"?([^>|\"]*)");
        Matcher matcher = pattern.matcher(email);

        String address = null;
        String name = null;

        if (matcher.find()) {
            name = matcher.group(1);
            address = matcher.group(2);
        } else {
            address = email;
        }

        return new EmailAddress(address, name);
    }

}
