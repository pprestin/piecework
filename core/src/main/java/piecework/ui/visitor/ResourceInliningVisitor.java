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

import org.htmlcleaner.TagNode;
import piecework.model.Explanation;
import piecework.ui.InlinePageModelSerializer;
import piecework.ui.TagAttributeAction;
import piecework.settings.UserInterfaceSettings;

/**
 * @author James Renfro
 */
public class ResourceInliningVisitor extends HtmlProviderVisitor {

    private final InlinePageModelSerializer modelSerializer;
    private final StaticResourceAggregatingVisitor visitor;

    public ResourceInliningVisitor(UserInterfaceSettings settings, InlinePageModelSerializer modelSerializer, StaticResourceAggregatingVisitor visitor, boolean isAnonymous) {
        super(settings, isAnonymous);
        this.modelSerializer = modelSerializer;
        this.visitor = visitor;
    }

    @Override
    protected void handleHead(String tagName, TagNode tagNode) {
        tagNode.addChild(modelSerializer.getStylesheetContent(visitor.getStylesheetResource()));
    }

    @Override
    protected void handleBody(String tagName, TagNode tagNode) {
        if (modelSerializer.isExplanation()) {
            Explanation explanation = modelSerializer.getObject(Explanation.class);
            tagNode.addAttribute("data-message", explanation.getMessage());
            tagNode.addAttribute("data-messageDetail", explanation.getMessageDetail());
        }
//        tagNode.addChild(modelSerializer.getPageModelScript());
        tagNode.addChild(modelSerializer.getScriptContent(visitor.getScriptResource()));
    }

    protected void handleStylesheet(String tagName, TagNode tagNode) {
        TagAttributeAction action = visitor.getLinkAction(tagNode);
        handleAction(action, tagNode);
    }

    protected void handleScript(String tagName, TagNode tagNode) {
        TagAttributeAction action = visitor.getScriptAction(tagNode);
        handleAction(action, tagNode);
    }

    private void handleAction(TagAttributeAction action, TagNode tagNode) {
        switch (action.getType()) {
            case REMOVE:
                tagNode.removeFromTree();
                break;
            case MODIFY:
                tagNode.addAttribute(action.getName(), action.getReplace());
                break;
        }
    }


}
