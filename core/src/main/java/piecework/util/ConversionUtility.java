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
package piecework.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ConversionUtility {

    private static final Logger LOG = Logger.getLogger(ConversionUtility.class);
    private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile("(\\d+)\\s*(\\w)B");

    public static int kilobytes(String str, int defaultValue) {
        String bytes = str;
        int multiplier = 1;

        if (StringUtils.isNotEmpty(str)) {
            Matcher matcher = BYTE_SIZE_PATTERN.matcher(str);
            if (matcher.find()) {
                bytes = matcher.group(1);
                String multiplierChar = matcher.group(2);

                if (StringUtils.isNotEmpty(multiplierChar) && multiplierChar.length() == 1) {
                    char m = multiplierChar.charAt(0);
                    switch (m) {
                        case 'K':
                            multiplier = 1;
                            break;
                        case 'M':
                            multiplier = 1024;
                            break;
                        case 'G':
                            multiplier = 1024 * 1024;
                            break;
                    }
                }
            }
        }
        int numeric = integer(bytes, defaultValue);
        if (numeric < 0)
            numeric = 0;

        return numeric * multiplier;
    }

    public static int integer(String str, int defaultValue) {
        if (StringUtils.isNotEmpty(str)) {
            try {
                return Integer.valueOf(str);
            } catch (NumberFormatException nfe) {
                LOG.error("Unable to format " + str + " as integer");
            }
        }
        return defaultValue;
    }

}
