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
package piecework.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import org.springframework.core.env.Environment;
import piecework.model.Explanation;
import piecework.model.User;

import java.util.Map;

/**
 * @author James Renfro
 */
public class HtmlProviderVisitor implements TagNodeVisitor {

    private static final Logger LOG = Logger.getLogger(HtmlProviderVisitor.class);

    protected final String applicationTitle;
    protected final String applicationUrl;
    protected final String publicUrl;
    protected final String assetsUrl;

    public HtmlProviderVisitor(String applicationTitle, String applicationUrl, String publicUrl, String assetsUrl) {
        this.applicationTitle = applicationTitle;
        this.applicationUrl = applicationUrl;
        this.publicUrl = publicUrl;
        this.assetsUrl = assetsUrl;
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
        if (checkForSecurePath(href)) {
            attributes.put("href", recomputeSecurePath(href, applicationUrl));
            tagNode.setAttributes(attributes);
        }
    }

    private void handleReferences(TagNode tagNode) {
        Map<String, String> attributes = tagNode.getAttributes();
        String href = attributes.get("href");
        String src = attributes.get("src");
        String main = attributes.get("data-main");

        if (checkForStaticPath(href)) {
            attributes.put("href", recomputeStaticPath(href, assetsUrl));
            tagNode.setAttributes(attributes);
        }
        if (checkForStaticPath(src)) {
            attributes.put("src", recomputeStaticPath(src, assetsUrl));
            tagNode.setAttributes(attributes);
        }
        if (checkForStaticPath(main)) {
            attributes.put("data-main", recomputeStaticPath(main, assetsUrl));
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

    protected boolean checkForSecurePath(String path) {
        if (path == null)
            return false;

        return path.startsWith("ui/") || path.startsWith("../ui/") || path.startsWith(("../../ui"));
    }

    protected boolean checkForStaticPath(String path) {
        if (path == null)
            return false;

        return path.startsWith("static/") || path.startsWith("../static/") || path.startsWith(("../../static"));
    }

    protected String recomputeSecurePath(final String path, String assetsUrl) {
        int indexOf = path.indexOf("ui/");

        if (indexOf > path.length())
            return path;

        String adjustedPath = path.substring(indexOf+7);
        return new StringBuilder(assetsUrl).append("/").append(adjustedPath).toString();
    }

    protected String recomputeStaticPath(final String path, String assetsUrl) {
        int indexOf = path.indexOf("static/");

        if (indexOf > path.length())
            return path;

        String adjustedPath = path.substring(indexOf);
        return new StringBuilder(assetsUrl).append("/").append(adjustedPath).toString();
    }
}
