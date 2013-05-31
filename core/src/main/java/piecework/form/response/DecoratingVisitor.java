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

import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.form.FieldTag;
import piecework.model.*;
import piecework.util.ManyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decorates HTML based on
 *
 * @author James Renfro
 */
public class DecoratingVisitor implements TagNodeVisitor {

    private final Form form;
    private final ManyMap<String, TagDecorator> decoratorMap;

    public DecoratingVisitor(Form form) {
        this.form = form;
        this.decoratorMap = new ManyMap<String, TagDecorator>();
        initialize();
    }

    public void initialize() {
        decoratorMap.putOne("form", new FormDecorator(form));

        Map<String, FormValue> formValueMap = form.getFormValueMap();
        Screen screen = form.getScreen();

        if (screen != null) {
            List<Section> sections = screen.getSections();
            if (sections != null && !sections.isEmpty()) {
                for (Section section : sections) {
                    if (section.getTagId() != null)
                        decoratorMap.putOne("div", new SectionDecorator(section));

                    List<Field> fields = section.getFields();
                    if (fields == null || fields.isEmpty())
                        continue;

                    for (Field field : fields) {
                        FormValue formValue = formValueMap.get(field.getName());
                        FieldDecorator fieldDecorator = new FieldDecorator(field, formValue);
                        FieldTag fieldTag = fieldDecorator.getFieldTag();
                        decoratorMap.putOne(fieldTag.getTagName(), fieldDecorator);
                    }
                }
            }
        }
    }

    public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
        if (htmlNode instanceof TagNode) {
            TagNode tag = (TagNode) htmlNode;
            String tagName = tag.getName();
            List<TagDecorator> decorators = decoratorMap.get(tagName);

            if (decorators == null || decorators.isEmpty())
                return true;

            String id = tag.getAttributeByName("id");
            String cls = tag.getAttributeByName("class");
            String name = tag.getAttributeByName("name");

            for (TagDecorator decorator : decorators) {
                if (decorator != null && decorator.canDecorate(id, cls, name)) {
                    decorator.decorate(tag, id , cls, name);
                }
            }
        }
        return true;
    }

    class FormDecorator implements TagDecorator {

        private final Form form;

        public FormDecorator(Form form) {
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag, String id, String cls, String name) {
            String formUri = form.getLink() != null ? form.getLink() : "";

            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("action", formUri);
            attributes.put("method", "POST");
            attributes.put("enctype", "multipart/form-data");
            tag.setAttributes(attributes);
        }

        @Override
        public boolean canDecorate(String id, String cls, String name) {
            return id == null || id.equals("main-form");
        }

        public boolean isReusable() {
            return false;
        }
    }

    class SectionDecorator implements TagDecorator {

        private final Section section;

        public SectionDecorator(Section section) {
            this.section = section;
        }

        @Override
        public void decorate(TagNode tag, String id, String cls, String name) {

        }

        @Override
        public boolean canDecorate(String id, String cls, String name) {
            return id == null || section.getTagId() != null && section.getTagId().equals(id);
        }

        public boolean isReusable() {
            return true;
        }

    }

    class FieldDecorator implements TagDecorator {

        private final Field field;
        private final FieldTag fieldTag;
        private final FormValue formValue;
        private int index;

        public FieldDecorator(Field field, FormValue formValue) {
            this.field = field;
            this.fieldTag = FieldTag.getInstance(field.getType());
            this.formValue = formValue;
            this.index = 0;
        }

        @Override
        public void decorate(TagNode tag, String id, String cls, String name) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.putAll(tag.getAttributes());
            attributes.putAll(fieldTag.getAttributes());
            tag.setAttributes(attributes);

            String value = null;

            if (formValue != null) {
                List<String> values = formValue.getAllValues();


                if (values.size() > index)
                    value = values.get(index);
            }

            switch (fieldTag) {
                case TEXT:
                    if (field.getMaxValueLength() > 0)
                        attributes.put("maxlength", "" + field.getMaxValueLength());
                    if (field.getDisplayValueLength() > 0)
                        attributes.put("size", "" + field.getDisplayValueLength());
                case CHECKBOX:
                case RADIO:
                    if (value != null)
                        attributes.put("value", value);
                    break;
                case TEXTAREA:
                    if (value != null)
                        tag.addChild(new ContentNode(value));
                    break;
                case SELECT_MULTIPLE:
                case SELECT_ONE:
                    List<Option> options = field.getOptions();
                    if (options != null && !options.isEmpty()) {
                        tag.removeAllChildren();

                        for (Option option : options) {
                            TagNode optionNode = new TagNode("option");
                            Map<String, String> optionAttributes = new HashMap<String, String>();
                            optionAttributes.put("value", option.getValue());
                            if (option.getValue() != null && value != null && option.getValue().equals(value))
                                optionAttributes.put("selected", "selected");
                            optionNode.setAttributes(optionAttributes);
                            optionNode.addChild(new ContentNode(option.getLabel()));
                            tag.addChild(optionNode);
                        }
                    }
                    break;
            }

            this.index++;
        }

        @Override
        public boolean canDecorate(String id, String cls, String name) {

            if (id != null && id.equals(field.getFieldId()))
                return true;

            if (name != null && name.equals(field.getName()))
                return true;

            return false;
        }

        public FieldTag getFieldTag() {
            return fieldTag;
        }

        public boolean isReusable() {
            return true;
        }

    }
}
