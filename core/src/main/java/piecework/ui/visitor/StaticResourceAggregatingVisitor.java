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
import org.springframework.core.io.Resource;
import piecework.content.ContentResource;
import piecework.form.FormDisposition;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.ContentRepository;
import piecework.ui.StaticResourceAggregator;
import piecework.ui.TagAttributeAction;
import piecework.settings.UserInterfaceSettings;
import piecework.util.PathUtility;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
public class StaticResourceAggregatingVisitor<P extends ProcessDeploymentProvider> extends HtmlProviderVisitor {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final Logger LOG = Logger.getLogger(StaticResourceAggregatingVisitor.class);

    private final StaticResourceAggregator scriptAggregator;
    private final StaticResourceAggregator stylesheetAggregator;
    private final Map<String, TagAttributeAction> scriptAttributeActionMap;
    private final Map<String, TagAttributeAction> linkAttributeActionMap;

    public StaticResourceAggregatingVisitor(ServletContext servletContext, ContentProfileProvider modelProvider, FormDisposition disposition, UserInterfaceSettings settings, ContentRepository contentRepository, boolean isAnonymous) {
        super(settings, isAnonymous);
        this.scriptAggregator = new StaticResourceAggregator(servletContext, modelProvider, contentRepository, settings, disposition);
        this.stylesheetAggregator = new StaticResourceAggregator(servletContext, modelProvider, contentRepository, settings, disposition);
        this.scriptAttributeActionMap = new HashMap<String, TagAttributeAction>();
        this.linkAttributeActionMap = new HashMap<String, TagAttributeAction>();
    }

    public TagAttributeAction getLinkAction(TagNode tagNode) {
        return getTagAction(tagNode, "href", this.linkAttributeActionMap);
    }

    public TagAttributeAction getScriptAction(TagNode tagNode) {
        return getTagAction(tagNode, "src", this.scriptAttributeActionMap);
    }

    public ContentResource getScriptResource() {
        return this.scriptAggregator.getStaticResource();
    }

    public ContentResource getStylesheetResource() {
        if (StringUtils.isNotEmpty(settings.getCustomStylesheetUrl()))
            this.stylesheetAggregator.handle(settings.getCustomStylesheetUrl());
        return this.stylesheetAggregator.getStaticResource();
    }

    protected void handleBody(String tagName, TagNode tagNode) {

    }

    protected void handleStylesheet(String tagName, TagNode tagNode) {
        String type = tagNode.getAttributeByName("type");

        // Skip non-css links
        if (StringUtils.isNotEmpty(type) && !type.equals("text/css"))
            return;

        String id = tagNode.getAttributeByName("id");
        if (StringUtils.isEmpty(id) || !id.startsWith("piecework-"))
            handleAttribute(tagNode, "href", stylesheetAggregator, linkAttributeActionMap);
    }

    protected void handleScript(String tagName, TagNode tagNode) {
        String id = tagNode.getAttributeByName("id");
        String type = tagNode.getAttributeByName("type");

        // Skip non-javascript scripts
        if (StringUtils.isNotEmpty(type) && !type.equals("text/javascript"))
            return;

        if (StringUtils.isEmpty(id) || !id.startsWith("piecework-")) {
            //handleAttribute(tagNode, "href", scriptAggregator, scriptAttributeActionMap);
            handleAttribute(tagNode, "src", scriptAggregator, scriptAttributeActionMap);
        }
    }

    private void handleAttribute(TagNode tagNode, String attributeName, StaticResourceAggregator aggregator, Map<String, TagAttributeAction> actionMap) {
        TagAttributeAction action = PathUtility.handleAttribute(tagNode, aggregator, attributeName);
        String key = action.getName() + "::" + action.getValue();
        actionMap.put(key, action);
    }

    private TagAttributeAction getTagAction(TagNode tagNode, String name, Map<String, TagAttributeAction> actionMap) {
        String value = tagNode.getAttributeByName(name);

        if (StringUtils.isNotEmpty(value)) {
            String key = name + "::" + value;
            TagAttributeAction action = actionMap.get(key);
            if (action != null)
                return action;
        }
        return new TagAttributeAction(TagAttributeAction.TagAttributeActionType.LEAVE, name, value, null);
    }

}
