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
import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.TagNode;
import org.springframework.core.env.Environment;
import piecework.model.Form;
import piecework.model.SearchResults;
import piecework.model.User;

import java.util.Map;

/**
 * @author James Renfro
 */
public class LinkOptimizingVisitor extends HtmlProviderVisitor {

    public LinkOptimizingVisitor(Object t, Class<?> type, User user, ObjectMapper objectMapper, Environment environment) {
        super(t, type, user, objectMapper, environment);
    }

    protected void handleBody(String tagName, TagNode tagNode) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("; piecework = {};")
                .append("piecework.context = ").append(pageContextAsJson).append(";");

        if (modelAsJson != null) {
            if (isExplanation)
                buffer.append("piecework.explanation = ").append(modelAsJson).append(";");
            else
                buffer.append("piecework.model = ").append(modelAsJson).append(";");
        }

        TagNode script = new TagNode("script");
        script.addAttribute("id", "piecework-context");
        script.addAttribute("type", "text/javascript");
        script.addChild(new ContentNode(buffer.toString()));
        tagNode.addChild(script);

        TagNode dependencies = new TagNode("script");
        dependencies.addAttribute("id", "piecework-dependencies");
        dependencies.addAttribute("type", "text/javascript");

        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);
            dependencies.addAttribute("src", form.getLink() + ".js");
        } else if (type.equals(SearchResults.class)) {
            dependencies.addAttribute("src", "/workflow/secure/form.js");
        }
        tagNode.addChild(dependencies);
    }

    protected void handleScript(String tagName, TagNode tagNode) {
        String id = tagNode.getAttributeByName("id");
        if (StringUtils.isEmpty(id) || !id.startsWith("piecework-"))
            tagNode.removeFromTree();
    }

}
