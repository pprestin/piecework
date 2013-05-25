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
package piecework.form.response;

import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.model.Form;

/**
 * @author James Renfro
 */
public class DecoratingVisitor implements TagNodeVisitor {

    private final DecoratorFactory factory;

    public DecoratingVisitor(Form form) {
        this.factory = new DecoratorFactory(form);
    }

    public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
        if (htmlNode instanceof TagNode) {
            TagNode tag = (TagNode) htmlNode;
            String tagName = tag.getName();
            String id = tag.getAttributeByName("id");
            String cls = tag.getAttributeByName("class");

            TagDecorator decorator = factory.decorator(tagName, id, cls);

            if (decorator != null)
                decorator.decorate(tag);
        }
        return true;
    }

}
