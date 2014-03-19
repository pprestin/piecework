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
import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.Constants;
import piecework.enumeration.FieldTag;
import piecework.model.Container;
import piecework.model.Field;

import java.util.Set;
import java.util.TreeSet;

/**
 * This TagNodeVisitor traverses an external web page and deduces the submission template that should be
 * @author James Renfro
 */
public class RemoteTemplateVisitor implements TagNodeVisitor {

    private final Container parentContainer;
    private final Container container;
    private final Set<Field> fields;

    public RemoteTemplateVisitor(Container parentContainer, Container container) {
        this.parentContainer = parentContainer;
        this.container = container;
        this.fields = new TreeSet<Field>();
    }

    @Override
    public boolean visit(TagNode parentNode, HtmlNode htmlNode) {

        if (htmlNode instanceof TagNode) {
            TagNode tag = (TagNode) htmlNode;
            String tagName = tag.getName() != null ? tag.getName().toLowerCase() : "";
            String typeAttribute = tag.getAttributeByName("type");
            String multipleAttribute = tag.getAttributeByName("multiple");

            String activeAttribute = tag.getAttributeByName("data-wf-active");
            String dateAttribute = tag.getAttributeByName("data-wf-date");
            String fileAttribute = tag.getAttributeByName("data-wf-file");
            String personAttribute = tag.getAttributeByName("data-wf-person");
            String nameAttribute = tag.getAttributeByName("data-name");
            if (StringUtils.isEmpty(nameAttribute))
                nameAttribute = tag.getAttributeByName("name");

            String requiredAttribute = tag.getAttributeByName("required");

            if (StringUtils.isEmpty(activeAttribute) || activeAttribute.equals("" + container.getOrdinal())) {
                boolean required = StringUtils.isNotEmpty(requiredAttribute);

                FieldTag fieldTag = null;

                if (StringUtils.isNotEmpty(dateAttribute))
                    fieldTag = FieldTag.DATE;
                else if (StringUtils.isNotEmpty(fileAttribute))
                    fieldTag = FieldTag.FILE;
                else if (StringUtils.isNotEmpty(personAttribute))
                    fieldTag = FieldTag.PERSON;
                else
                    fieldTag = FieldTag.getInstance(tagName, typeAttribute, multipleAttribute);

                if (fieldTag != null && StringUtils.isNotEmpty(nameAttribute)) {
                    fields.add(new Field.Builder()
                            .name(nameAttribute)
                            .type(fieldTag.getFieldType())
                            .editable()
                            .required(required)
                            .build());
                }
            }
        }
        return true;
    }

    public Set<Field> getFields() {
        return fields;
    }

}
