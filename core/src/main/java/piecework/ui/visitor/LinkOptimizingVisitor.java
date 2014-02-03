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
import org.htmlcleaner.TagNode;
import piecework.ui.InlinePageModelSerializer;
import piecework.settings.UserInterfaceSettings;
import piecework.util.PathUtility;

import java.util.Map;

/**
 * @author James Renfro
 */
public class LinkOptimizingVisitor extends HtmlProviderVisitor {
    private static final Logger LOG = Logger.getLogger(LinkOptimizingVisitor.class);

    private final InlinePageModelSerializer modelSerializer;

    public LinkOptimizingVisitor(UserInterfaceSettings settings, InlinePageModelSerializer modelSerializer, boolean isAnonymous) {
        super(settings, isAnonymous);
        this.modelSerializer = modelSerializer;
    }

    @Override
    protected void handleHead(String tagName, TagNode tagNode) {
        tagNode.addChild(modelSerializer.getStylesheetLink());
    }

    @Override
    protected void handleBody(String tagName, TagNode tagNode) {
        tagNode.addChild(modelSerializer.getPageModelScript());
        tagNode.addChild(modelSerializer.getScriptLink());
    }

    protected void handleBase(TagNode tagNode) {
        Map<String, String> attributes = tagNode.getAttributes();
        String href = tagNode.getAttributeByName("href");
        if (PathUtility.checkForSecurePath(href)) {
            attributes.put("href", PathUtility.recomputeSecurePath(href, settings, isAnonymous));
            tagNode.setAttributes(attributes);
        }
    }

    protected void handleScript(String tagName, TagNode tagNode) {
        String id = tagNode.getAttributeByName("id");
        if (StringUtils.isEmpty(id) || !id.startsWith("piecework-"))
            tagNode.removeFromTree();
    }

    protected void handleStylesheet(String tagName, TagNode tagNode) {
        String id = tagNode.getAttributeByName("id");
        if (StringUtils.isEmpty(id) || !id.startsWith("piecework-"))
            tagNode.removeFromTree();
    }

}
