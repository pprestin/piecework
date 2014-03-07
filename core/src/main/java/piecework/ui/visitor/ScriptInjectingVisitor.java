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

import com.google.common.collect.Sets;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.common.ManyMap;
import piecework.model.Form;

import java.util.List;
import java.util.Set;

/**
 * Decorates a web form with minimal impact -- adding task-based script to
 * completion data injection on the client as part of
 * {@see piecework.enumeration.DataInjectionStrategy.INCLUDE_SCRIPT}
 *
 * @author James Renfro
 */
public class ScriptInjectingVisitor implements TagNodeVisitor {

    // Only children of these tags will be traversed -- to avoid going down through the whole DOM
    private static final Set<String> FOLLOW_TAGS = Sets.newHashSet("html", "body", "head");

    private final Form form;
    private final ManyMap<String, TagDecorator> decoratorMap;

    public ScriptInjectingVisitor(Form form) {
        this.form = form;
        this.decoratorMap = new ManyMap<String, TagDecorator>();
        initialize();
    }

    public void initialize() {
        decoratorMap.putOne("body", new BodyDecorator(form));
        decoratorMap.putOne("script", new ScriptDecorator());
        decoratorMap.putOne("link", new StyleSheetDecorator());
    }

    public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
        if (htmlNode instanceof TagNode) {
            TagNode tag = (TagNode) htmlNode;
            String tagName = tag.getName();
            List<TagDecorator> decorators = decoratorMap.get(tagName);

            if (decorators != null && !decorators.isEmpty()) {
                for (TagDecorator decorator : decorators) {
                    decorator.decorate(tag);
                }
            }
        }
        return true;
    }

    interface TagDecorator {

        void decorate(TagNode tag);

    }

    class BodyDecorator implements TagDecorator {

        private final Form form;

        public BodyDecorator(Form form) {
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag) {
            TagNode scriptTag = new TagNode("script");
            scriptTag.addAttribute("type", "text/javascript");
            scriptTag.addAttribute("src", form.getSrc() + ".js");

            tag.addChild(scriptTag);
        }
    }

    class ScriptDecorator implements TagDecorator {

        @Override
        public void decorate(TagNode tag) {
            String src = tag.getAttributeByName("src");

            if (!src.startsWith("/") && !src.startsWith("http://") && !src.startsWith("https://")) {
                tag.addAttribute("src", form.getStaticRoot() + "/" + src);
            }
        }
    }

    class StyleSheetDecorator implements TagDecorator {

        @Override
        public void decorate(TagNode tag) {
            String href = tag.getAttributeByName("href");

            if (!href.startsWith("/") && !href.startsWith("http://") && !href.startsWith("https://")) {
                tag.addAttribute("href", form.getStaticRoot() + "/" + href);
            }
        }

    }
}
