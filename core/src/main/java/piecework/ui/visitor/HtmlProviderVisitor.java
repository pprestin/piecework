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
package piecework.ui.visitor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.settings.UserInterfaceSettings;
import piecework.util.PathUtility;

import java.util.Map;

/**
 * @author James Renfro
 */
public class HtmlProviderVisitor implements TagNodeVisitor {

    private static final Logger LOG = Logger.getLogger(HtmlProviderVisitor.class);

    protected final UserInterfaceSettings settings;
    protected final boolean isAnonymous;

    public HtmlProviderVisitor(UserInterfaceSettings settings, boolean isAnonymous) {
        this.settings = settings;
        this.isAnonymous = isAnonymous;
    }

    @Override
    public boolean visit(TagNode parentNode, HtmlNode htmlNode) {

        if (htmlNode instanceof TagNode) {
            TagNode tagNode = TagNode.class.cast(htmlNode);
            String tagName = tagNode.getName();

            if (tagName != null) {
                if (tagName.equals("body")) {
                    handleBody(tagName, tagNode);
                } else if (tagName.equals("head")) {
                    handleHead(tagName, tagNode);
                } else if (tagName.equals("script")) {
                    handleScript(tagName, tagNode);
                } else if (tagName.equals("link")) {
                    handleStylesheet(tagName, tagNode);
                } else if (tagName.equals("base")) {
                    handleBase(tagNode);
                }
            }
        }

        return true;
    }

    protected void handleBase(TagNode tagNode) {
        Map<String, String> attributes = tagNode.getAttributes();
        String href = tagNode.getAttributeByName("href");
        if (PathUtility.checkForSecurePath(href)) {
            if (StringUtils.isEmpty(href))
                attributes.put("href", settings.getApplicationUrl());
            else
                attributes.put("href", PathUtility.recomputeSecurePath(href, settings, isAnonymous));
            tagNode.setAttributes(attributes);
        }
    }

    private void handleReferences(TagNode tagNode) {
        Map<String, String> attributes = tagNode.getAttributes();
        String href = attributes.get("href");
        String src = attributes.get("src");
        String main = attributes.get("data-main");

        if (PathUtility.checkForStaticPath(href)) {
            attributes.put("href", PathUtility.recomputeStaticPath(href, settings));
            tagNode.setAttributes(attributes);
        }
        if (PathUtility.checkForStaticPath(src)) {
            attributes.put("src", PathUtility.recomputeStaticPath(src, settings));
            tagNode.setAttributes(attributes);
        }
        if (PathUtility.checkForStaticPath(main)) {
            attributes.put("data-main", PathUtility.recomputeStaticPath(main, settings));
            tagNode.setAttributes(attributes);
        }
    }

    protected void handleHead(String tagName, TagNode tagNode) {

    }

    protected void handleBody(String tagName, TagNode tagNode) {

    }

    protected void handleStylesheet(String tagName, TagNode tagNode) {
        handleReferences(tagNode);
    }

    protected void handleScript(String tagName, TagNode tagNode) {
        handleReferences(tagNode);
    }


}
