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

import org.apache.cxf.common.util.StringUtils;
import org.htmlcleaner.TagNode;
import piecework.enumeration.Scheme;
import piecework.model.Content;
import piecework.ui.StaticResourceAggregator;
import piecework.ui.TagAttributeAction;
import piecework.settings.UserInterfaceSettings;

/**
 * @author James Renfro
 */
public class PathUtility {

    public static Scheme findScheme(String location) {
        int indexOf = location.indexOf(':');
        String scheme = "";
        if (indexOf != -1)
            scheme = location.substring(0, indexOf);

        Content content;
        if (scheme.equals("http") || scheme.endsWith("https"))
            return Scheme.REMOTE;
        else if (scheme.equals("classpath"))
            return Scheme.CLASSPATH;
        else if (scheme.equals("file"))
            return Scheme.FILESYSTEM;

        return Scheme.REPOSITORY;
    }

    public static TagAttributeAction handleAttribute(TagNode tagNode, StaticResourceAggregator aggregator, String attributeName) {
        // Sanity checks
        if (tagNode == null)
            return null;
        if (aggregator == null)
            return null;
        if (StringUtils.isEmpty(attributeName))
            return null;

        String attributeValue = tagNode.getAttributeByName(attributeName);
        if (attributeValue != null) {
            String result = aggregator.handle(attributeValue);
            if (result == null)
                return new TagAttributeAction(TagAttributeAction.TagAttributeActionType.REMOVE, attributeName, attributeValue, null);
            else
                return new TagAttributeAction(TagAttributeAction.TagAttributeActionType.MODIFY, attributeName, attributeValue, result);
        }
        return new TagAttributeAction(TagAttributeAction.TagAttributeActionType.LEAVE, attributeName, attributeValue, null);
    }

    public static boolean checkForSecurePath(String path) {
        if (path == null)
            return false;

        return path.startsWith("ui/") || path.startsWith("../ui/") || path.startsWith(("../../ui"));
    }

    public static boolean checkForStaticPath(String path) {
        if (path == null)
            return false;

        return path.startsWith("static/") || path.startsWith("../static/") || path.startsWith(("../../static"));
    }

    public static String recomputeSecurePath(final String path, final UserInterfaceSettings settings, boolean isAnonymous) {
        int indexOf = path.indexOf("ui/");

        if (indexOf > path.length())
            return path;

        String base = isAnonymous ? settings.getPublicUrl() : settings.getApplicationUrl();

        if (base == null)
            base = "";

        String adjustedPath = path.substring(indexOf+3);
        return new StringBuilder(base).append("/").append(adjustedPath).toString();
    }

    public static String recomputeStaticPath(final String path, final UserInterfaceSettings settings) {
        int indexOf = path.indexOf("static/");

        if (indexOf > path.length())
            return path;

        String adjustedPath = indexOf != -1 ? path.substring(indexOf) : path;

        return new StringBuilder(settings.getAssetsUrl() != null ? settings.getAssetsUrl() : "")
                .append("/").append(adjustedPath).toString();
    }

}
