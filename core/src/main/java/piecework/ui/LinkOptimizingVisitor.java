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
import org.apache.log4j.Logger;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.TagNode;
import org.springframework.core.env.Environment;
import piecework.designer.model.view.IndexView;
import piecework.model.Explanation;
import piecework.model.Form;
import piecework.model.SearchResults;
import piecework.model.User;

import java.util.Map;

/**
 * @author James Renfro
 */
public class LinkOptimizingVisitor extends HtmlProviderVisitor {
    private static final Logger LOG = Logger.getLogger(LinkOptimizingVisitor.class);

    private final Object t;
    private final Class<?> type;
    private final String pageContextAsJson;
    private final String modelAsJson;
    private final boolean isExplanation;

    public LinkOptimizingVisitor(String applicationTitle, String applicationUrl, String publicUrl, String assetsUrl, Object t, Class<?> type, User user, ObjectMapper objectMapper, Environment environment) {
        super(applicationTitle, applicationUrl, publicUrl, assetsUrl);
        this.t = t;
        this.type = type;
        PageContext pageContext = new PageContext.Builder()
                .applicationTitle(applicationTitle)
                .assetsUrl(assetsUrl)
                .user(user)
                .build();

        String pageContextAsJson;
        String modelAsJson;
        boolean isExplanation;
        try {
            pageContextAsJson = objectMapper.writer().writeValueAsString(pageContext);
            modelAsJson = objectMapper.writer().writeValueAsString(t);
            isExplanation = type != null && type.equals(Explanation.class);
        } catch (Exception e) {
            LOG.error("Unable to construct json", e);
            pageContextAsJson = "";
            modelAsJson = "";
            isExplanation = false;
        }
        this.pageContextAsJson = pageContextAsJson;
        this.modelAsJson = modelAsJson;
        this.isExplanation = isExplanation;
    }

    @Override
    protected void handleHead(String tagName, TagNode tagNode) {
        TagNode dependencies = new TagNode("link");
        dependencies.addAttribute("id", "piecework-stylesheet");
        dependencies.addAttribute("type", "text/css");
        dependencies.addAttribute("rel", "stylesheet");

        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);
            if (form.isAnonymous())
                dependencies.addAttribute("href", publicUrl + "/resource/css/Form.css");
            else
                dependencies.addAttribute("href", applicationUrl + "/resource/css/Form.css");

        } else if (type.equals(SearchResults.class)) {
            dependencies.addAttribute("href", applicationUrl + "/resource/css/SearchResults.form.css");
        } else if (type.equals(IndexView.class)) {
            dependencies.addAttribute("href", applicationUrl + "/resource/css/IndexView.css");
        }
        tagNode.addChild(dependencies);
    }

    @Override
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
        dependencies.addAttribute("rel", "script");

        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);
            if (form.isAnonymous())
                dependencies.addAttribute("src", publicUrl + "/resource/script/Form.js");
            else
                dependencies.addAttribute("src", applicationUrl + "/resource/script/Form.js");

        } else if (type.equals(SearchResults.class)) {
            dependencies.addAttribute("src", applicationUrl + "/resource/script/SearchResults.form.js");
        } else if (type.equals(IndexView.class)) {
            dependencies.addAttribute("src", applicationUrl + "/resource/script/IndexView.js");
        }
        tagNode.addChild(dependencies);
    }

    protected void handleBase(TagNode tagNode) {
        Map<String, String> attributes = tagNode.getAttributes();
        String href = tagNode.getAttributeByName("href");
        if (checkForSecurePath(href)) {
            String url = applicationUrl;
            if (type.equals(Form.class)) {
                Form form = Form.class.cast(t);
                if (form.isAnonymous())
                    url = publicUrl;
            }
            attributes.put("href", recomputeSecurePath(href, url));
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
