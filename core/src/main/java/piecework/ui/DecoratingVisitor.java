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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.Constants;
import piecework.form.FieldTag;
import piecework.model.*;
import piecework.util.ManyMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Decorates HTML based on
 *
 * @author James Renfro
 */
public class DecoratingVisitor implements TagNodeVisitor {

    private static final Logger LOG = Logger.getLogger(DecoratingVisitor.class);
    private static final String NEWLINE = System.getProperty("line.separator");

    private final Form form;
    private final ManyMap<String, TagDecorator> decoratorMap;

    public DecoratingVisitor(Form form) {
        this.form = form;
        this.decoratorMap = new ManyMap<String, TagDecorator>();
        initialize();
    }

    public void initialize() {
        decoratorMap.putOne("form", new FormDecorator(form));
        decoratorMap.putOne("body", new BodyDecorator(form));

        Map<String, FormValue> formValueMap = form.getFormValueMap();

        VariableDecorator variableDecorator = new VariableDecorator(formValueMap);
        decoratorMap.putOne("span", variableDecorator);
        decoratorMap.putOne("img", variableDecorator);

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
            String variable = tag.getAttributeByName("data-variable");

            for (TagDecorator decorator : decorators) {
                if (decorator != null && decorator.canDecorate(id, cls, name, variable)) {
                    decorator.decorate(tag, id , cls, name, variable);
                }
            }
        }
        return true;
    }

    class BodyDecorator implements TagDecorator {

        private final Form form;

        public BodyDecorator(Form form) {
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag, String id, String cls, String name, String variable) {
            Screen screen = form.getScreen();
            String screenType = screen.getType();

            if (screenType != null) {
                if (screenType.equals(Constants.ScreenTypes.WIZARD) || screenType.equals(Constants.ScreenTypes.WIZARD_TEMPLATE)) {
                    TagNode contextScriptTag = new TagNode("script");

                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        StringBuilder content = new StringBuilder(NEWLINE);
                        content.append("\t\tpiecework = {};").append(NEWLINE)
                               .append("\t\tpiecework.context = {};").append(NEWLINE)
                               .append("\t\tpiecework.context.resource = ").append(mapper.writer().writeValueAsString(form))
                               .append(";").append(NEWLINE);

                        contextScriptTag.addAttribute("type", "text/javascript");
                        contextScriptTag.addChild(new ContentNode(content.toString()));
                        tag.addChild(contextScriptTag);
                    } catch (JsonMappingException e) {
                        LOG.error("Unable to add script tag with form resource", e);
                    } catch (JsonGenerationException e) {
                        LOG.error("Unable to add script tag with form resource", e);
                    } catch (IOException e) {
                        LOG.error("Unable to add script tag with form resource", e);
                    }

                    TagNode requirejsScriptTag = new TagNode("script");
                    requirejsScriptTag.addAttribute("type", "text/javascript");
                    requirejsScriptTag.addAttribute("data-main", "../static/js/form.js");
                    requirejsScriptTag.addAttribute("src", "../static/js/vendor/require.js");
                }
            }
        }

        @Override
        public boolean canDecorate(String id, String cls, String name, String variable) {
            Screen screen = form.getScreen();
            String screenType = screen.getType();

            return screenType != null && screenType.equals(Constants.ScreenTypes.WIZARD) || screenType.equals(Constants.ScreenTypes.WIZARD_TEMPLATE);
        }

        public boolean isReusable() {
            return false;
        }
    }

    class FormDecorator implements TagDecorator {

        private final Form form;

        public FormDecorator(Form form) {
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag, String id, String cls, String name, String variable) {
            String formUri = form.getAction() != null ? form.getAction() : "";

            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("action", formUri);
            attributes.put("method", "POST");
            attributes.put("enctype", "multipart/form-data");
            tag.setAttributes(attributes);
        }

        @Override
        public boolean canDecorate(String id, String cls, String name, String variable) {
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
        public void decorate(TagNode tag, String id, String cls, String name, String variable) {

        }

        @Override
        public boolean canDecorate(String id, String cls, String name, String variable) {
            return id == null || section.getTagId() != null && section.getTagId().equals(id);
        }

        public boolean isReusable() {
            return true;
        }

    }

    class VariableDecorator implements TagDecorator {

        private Map<String, FormValue> formValueMap;

        public VariableDecorator(Map<String, FormValue> formValueMap) {
            this.formValueMap = formValueMap;
        }

        @Override
        public void decorate(TagNode tag, String id, String cls, String name, String variable) {
            FormValue formValue = formValueMap.get(variable);

            if (formValue != null) {
                // Image tags are a special case update src and alt attributes
                if (tag.getName() != null && tag.getName().equals("img")) {
                    Map<String, String> attributes = new HashMap<String, String>();
                    attributes.putAll(tag.getAttributes());
                    attributes.put("alt", formValue.getValue());
                    attributes.put("src", formValue.getLink());
                    tag.setAttributes(attributes);
                    return;
                }

                tag.removeAllChildren();
                List<String> values = formValue.getAllValues();

                if (values != null) {
                    for (String value : values) {
                        tag.addChild(new ContentNode(value));
                    }
                }
            }
        }

        @Override
        public boolean canDecorate(String id, String cls, String name, String variable) {
            if (variable != null && formValueMap.containsKey(variable))
                return true;

            return false;
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
        public void decorate(TagNode tag, String id, String cls, String name, String variable) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.putAll(tag.getAttributes());
            attributes.putAll(fieldTag.getAttributes());
            tag.setAttributes(attributes);

            String value = null;

            if (formValue != null) {
                List<String> values = formValue.getAllValues();

                if (values.size() > index)
                    value = values.get(index);

                List<Message> messages = formValue.getMessages();
                if (messages != null && !messages.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    Iterator<Message> iterator = messages.iterator();
                    while (iterator.hasNext()) {
                        Message message = iterator.next();
                        builder.append(message.getText());
                        if (iterator.hasNext())
                            builder.append(",");
                    }
                    attributes.put("data-messages", builder.toString());
                }
            }

            if (!field.isVisible())
                attributes.put("class", cls + " hide");

            switch (fieldTag) {
                case EMAIL:
                case NUMBER:
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
        public boolean canDecorate(String id, String cls, String name, String variable) {

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
