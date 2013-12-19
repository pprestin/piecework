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
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.TagNode;
import org.springframework.core.io.Resource;
import piecework.designer.model.view.IndexView;
import piecework.model.*;

import java.io.IOException;

/**
 * @author James Renfro
 */
public class InlinePageModelSerializer {

    private static final Logger LOG = Logger.getLogger(InlinePageModelSerializer.class);

    private final UserInterfaceSettings settings;
    private final Object t;
    private final Class<?> type;
    private final String pageContextAsJson;
    private final String modelAsJson;
    private final boolean isExplanation;

    public InlinePageModelSerializer(UserInterfaceSettings settings, Object t, Class<?> type, Entity user, ObjectMapper objectMapper) {
        this.settings = settings;
        this.t = t;
        this.type = type;
        PageContext pageContext = new PageContext.Builder()
                .applicationTitle(settings.getApplicationTitle())
                .assetsUrl(settings.getAssetsUrl())
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

    public TagNode getPageModelScript() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("piecework = {};")
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
        return script;
    }

    public TagNode getScriptContent(Resource resource) {
        TagNode script = new TagNode("script");
        script.addAttribute("id", "piecework-context");
        script.addAttribute("type", "text/javascript");

        try {
            script.addChild(new ContentNode(IOUtils.toString(resource.getInputStream())));
        } catch (IOException ioe) {
            LOG.error("Unable to inject the script content because of an io exception", ioe);
        }
        return script;
    }

    public TagNode getScriptLink() {
        TagNode script = new TagNode("script");
        script.addAttribute("id", "piecework-dependencies");
        script.addAttribute("type", "text/javascript");
        script.addAttribute("rel", "script");

        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);
            if (form.isAnonymous())
                script.addAttribute("src", settings.getPublicUrl() + "/resource/script/Form.js");
            else
                script.addAttribute("src", settings.getApplicationUrl() + "/resource/script/Form.js");
        } else if (type.equals(Report.class)) {
            script.addAttribute("src", settings.getApplicationUrl() + "/resource/script/Report.js");
        } else if (type.equals(SearchResults.class)) {
            script.addAttribute("src", settings.getApplicationUrl() + "/resource/script/SearchResults.form.js");
        } else if (type.equals(IndexView.class)) {
            script.addAttribute("src", settings.getApplicationUrl() + "/resource/script/IndexView.js");
        } else if (type.equals(Explanation.class)) {
            script.addAttribute("href", settings.getPublicUrl() + "/resource/script/Explanation.js");
        }
        return script;
    }

    public TagNode getStylesheetContent(Resource resource) {
        TagNode style = new TagNode("style");
        style.addAttribute("id", "piecework-context");
        style.addAttribute("type", "text/css");

        try {
            style.addChild(new ContentNode(IOUtils.toString(resource.getInputStream())));
        } catch (IOException ioe) {
            LOG.error("Unable to inject the stylesheet content because of an io exception", ioe);
        }
        return style;
    }

    public TagNode getStylesheetLink() {
        TagNode link = new TagNode("link");
        link.addAttribute("id", "piecework-stylesheet");
        link.addAttribute("type", "text/css");
        link.addAttribute("rel", "stylesheet");

        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);
            if (form.isAnonymous())
                link.addAttribute("href", settings.getPublicUrl() + "/resource/css/Form.css");
            else
                link.addAttribute("href", settings.getApplicationUrl() + "/resource/css/Form.css");
        } else if (type.equals(Report.class)) {
            link.addAttribute("href", settings.getApplicationUrl() + "/resource/css/Report.css");
        } else if (type.equals(SearchResults.class)) {
            link.addAttribute("href", settings.getApplicationUrl() + "/resource/css/SearchResults.form.css");
        } else if (type.equals(IndexView.class)) {
            link.addAttribute("href", settings.getApplicationUrl() + "/resource/css/IndexView.css");
        } else if (type.equals(Explanation.class)) {
            link.addAttribute("href", settings.getPublicUrl() + "/resource/css/Explanation.css");
        }

        return link;
    }
}
