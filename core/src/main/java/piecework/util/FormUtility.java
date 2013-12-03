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
import piecework.enumeration.ActivityUsageType;
import piecework.model.*;

import java.net.URI;

/**
 * @author James Renfro
 */
public class FormUtility {

    private static final Logger LOG = Logger.getLogger(FormUtility.class);

    public static void addConfirmationNumber(Field.Builder fieldBuilder, String confirmationNumber) {
        String defaultValue = fieldBuilder.getDefaultValue();
        fieldBuilder.defaultValue(defaultValue.replaceAll("\\{\\{ConfirmationNumber\\}\\}", confirmationNumber));
    }

    public static void addStateOptions(Field.Builder fieldBuilder) {
        fieldBuilder.option(new Option.Builder().value("").label("").build())
                .option(new Option.Builder().value("AL").label("Alabama").build())
                .option(new Option.Builder().value("AK").label("Alaska").build())
                .option(new Option.Builder().value("AZ").label("Arizona").build())
                .option(new Option.Builder().value("AR").label("Arkansas").build())
                .option(new Option.Builder().value("CA").label("California").build())
                .option(new Option.Builder().value("CO").label("Colorado").build())
                .option(new Option.Builder().value("CT").label("Connecticut").build())
                .option(new Option.Builder().value("DE").label("Delaware").build())
                .option(new Option.Builder().value("DC").label("District Of Columbia").build())
                .option(new Option.Builder().value("FL").label("Florida").build())
                .option(new Option.Builder().value("GA").label("Georgia").build())
                .option(new Option.Builder().value("HI").label("Hawaii").build())
                .option(new Option.Builder().value("ID").label("Idaho").build())
                .option(new Option.Builder().value("IL").label("Illinois").build())
                .option(new Option.Builder().value("IN").label("Indiana").build())
                .option(new Option.Builder().value("IA").label("Iowa").build())
                .option(new Option.Builder().value("KS").label("Kansas").build())
                .option(new Option.Builder().value("KY").label("Kentucky").build())
                .option(new Option.Builder().value("LA").label("Louisiana").build())
                .option(new Option.Builder().value("ME").label("Maine").build())
                .option(new Option.Builder().value("MD").label("Maryland").build())
                .option(new Option.Builder().value("MA").label("Massachusetts").build())
                .option(new Option.Builder().value("MI").label("Michigan").build())
                .option(new Option.Builder().value("MN").label("Minnesota").build())
                .option(new Option.Builder().value("MS").label("Mississippi").build())
                .option(new Option.Builder().value("MO").label("Missouri").build())
                .option(new Option.Builder().value("MT").label("Montana").build())
                .option(new Option.Builder().value("NE").label("Nebraska").build())
                .option(new Option.Builder().value("NV").label("Nevada").build())
                .option(new Option.Builder().value("NH").label("New Hampshire").build())
                .option(new Option.Builder().value("NJ").label("New Jersey").build())
                .option(new Option.Builder().value("NM").label("New Mexico").build())
                .option(new Option.Builder().value("NY").label("New York").build())
                .option(new Option.Builder().value("NC").label("North Carolina").build())
                .option(new Option.Builder().value("ND").label("North Dakota").build())
                .option(new Option.Builder().value("OH").label("Ohio").build())
                .option(new Option.Builder().value("OK").label("Oklahoma").build())
                .option(new Option.Builder().value("OR").label("Oregon").build())
                .option(new Option.Builder().value("PA").label("Pennsylvania").build())
                .option(new Option.Builder().value("RI").label("Rhode Island").build())
                .option(new Option.Builder().value("SC").label("South Carolina").build())
                .option(new Option.Builder().value("SD").label("South Dakota").build())
                .option(new Option.Builder().value("TN").label("Tennessee").build())
                .option(new Option.Builder().value("TX").label("Texas").build())
                .option(new Option.Builder().value("UT").label("Utah").build())
                .option(new Option.Builder().value("VT").label("Vermont").build())
                .option(new Option.Builder().value("VA").label("Virginia").build())
                .option(new Option.Builder().value("WA").label("Washington").build())
                .option(new Option.Builder().value("WV").label("West Virginia").build())
                .option(new Option.Builder().value("WI").label("Wisconsin").build())
                .option(new Option.Builder().value("WY").label("Wyoming").build());
    }

    public static void layout(Form.Builder builder, Activity activity) {
        if (activity == null)
            return;

        ActivityUsageType usageType = activity.getUsageType() != null ? activity.getUsageType() : ActivityUsageType.USER_FORM;
        switch (usageType) {
            case MULTI_PAGE:
                builder.layout("multipage");
                break;
            case MULTI_STEP:
                builder.layout("multistep");
                break;
            case REVIEW_PAGE:
                builder.layout("review");
                break;
            default:
                builder.layout("normal");
                break;
        }
    }

    public static boolean isExternal(URI uri) {

        if (uri != null) {
            String scheme = uri.getScheme();
            return StringUtils.isNotEmpty(scheme) && (scheme.equals("http") || scheme.equals("https"));
        }

        return false;
    }

    public static URI safeUri(Action action, Task task) {
        URI uri = null;
        try {
            uri = action.getUri(task);
        } catch (IllegalArgumentException iae) {
            LOG.error("Failed to convert location into uri:" + action.getLocation(), iae);
        }
        return uri;
    }
}
