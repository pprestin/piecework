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

import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.common.ManyMap;
import piecework.model.Form;

import java.util.List;

/**
 * @author James Renfro
 */
public class StaticPathAdjustingVisitor implements TagNodeVisitor {

   private final Form form;
    private final ManyMap<String, TagDecorator> decoratorMap;

    public StaticPathAdjustingVisitor(Form form) {
        this.form = form;
        this.decoratorMap = new ManyMap<String, TagDecorator>();
        initialize();
    }

    public void initialize() {
        decoratorMap.putOne("script", new SrcDecorator());
        decoratorMap.putOne("link", new HrefDecorator());
        decoratorMap.putOne("img", new SrcDecorator());
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

    class SrcDecorator implements TagDecorator {

        @Override
        public void decorate(TagNode tag) {
            String src = tag.getAttributeByName("src");

            if (!src.startsWith("/") && !src.startsWith("http://") && !src.startsWith("https://")) {
                tag.addAttribute("src", form.getStaticRoot() + "/" + src);
            }
        }
    }

    class HrefDecorator implements TagDecorator {

        @Override
        public void decorate(TagNode tag) {
            String href = tag.getAttributeByName("href");

            if (!href.startsWith("/") && !href.startsWith("http://") && !href.startsWith("https://")) {
                tag.addAttribute("href", form.getStaticRoot() + "/" + href);
            }
        }

    }
}
