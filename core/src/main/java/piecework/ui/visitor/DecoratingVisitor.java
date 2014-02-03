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
import org.apache.log4j.Logger;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.enumeration.FieldTag;
import piecework.model.*;
import piecework.model.Process;
import piecework.settings.UserInterfaceSettings;
import piecework.common.ManyMap;

import java.util.*;

/**
 * Decorates HTML based on
 *
 * @author James Renfro
 */
public class DecoratingVisitor implements TagNodeVisitor {

    private static final Logger LOG = Logger.getLogger(DecoratingVisitor.class);
    private static final String NEWLINE = System.getProperty("line.separator");

    private final Process process;
    private final Form form;
    private final ManyMap<String, TagDecorator> decoratorMap;
    private final UserInterfaceSettings settings;

    public DecoratingVisitor(UserInterfaceSettings settings, Process process, Form form) {
        this.settings = settings;
        this.process = process;
        this.form = form;
        this.decoratorMap = new ManyMap<String, TagDecorator>();
        initialize();
    }

    public void initialize() {
        decoratorMap.putOne("head", new HeadDecorator(process, form));
        decoratorMap.putOne("body", new BodyDecorator(process, form));

        decoratorMap.putOne("form", new FormDecorator(form));

//        decoratorMap.putOne("div", new AttachmentsDecorator(form));

        VariableDecorator variableDecorator = new VariableDecorator(form);
        decoratorMap.putOne("span", variableDecorator);
        decoratorMap.putOne("input", variableDecorator);

        decoratorMap.putOne("script", new ScriptDecorator());
        decoratorMap.putOne("img", new ScriptDecorator());
        decoratorMap.putOne("link", new StyleSheetDecorator());

        Container container = form.getContainer();
        Map<String, List<Value>> data = variableDecorator.getData();
        Map<String, List<Message>> results = variableDecorator.getResults();
        handleContainer(container, data, results);
    }

    private void handleContainer(Container container, Map<String, List<Value>> data, Map<String, List<Message>> results) {
        if (container != null) {
            boolean readonly = container.isReadonly();
            if (readonly)
                decoratorMap.putOne("button", new ButtonDecorator(readonly));

            List<Field> fields = container.getFields();
            if (fields != null && !fields.isEmpty()) {

                for (Field field : fields) {
                    if (StringUtils.isEmpty(field.getName()))
                        continue;

                    List<Value> values = data.get(field.getName());
                    List<Message> messages = results.get(field.getName());
                    FieldDecorator fieldDecorator = new FieldDecorator(field, values, messages, readonly);
                    FieldTag fieldTag = fieldDecorator.getFieldTag();
                    decoratorMap.putOne(fieldTag.getTagName(), fieldDecorator);
                }
            }

            List<Container> children = container.getChildren();
            if (children != null && !children.isEmpty()) {
                for (Container child : children) {
                    handleContainer(child, data, results);
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

            try {
                for (TagDecorator decorator : decorators) {
                    if (decorator != null)
                        decorator.decorate(tag);
                }
            } catch (Exception e) {
                LOG.error("Unable to decorate tag node with name " + tagName, e);
            }
        }
        return true;
    }

    interface TagDecorator {

        void decorate(TagNode tag);

    }

    class BodyDecorator implements TagDecorator {

        private final Process process;
        private final Form form;

        public BodyDecorator(Process process, Form form) {
            this.process = process;
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag) {
            TagNode dependencies = new TagNode("script");
            dependencies.addAttribute("id", "piecework-dependencies");
            dependencies.addAttribute("type", "text/javascript");
            dependencies.addAttribute("rel", "script");

            String processDefinitionKey = process.getProcessDefinitionKey();
            if (form.isAnonymous())
                dependencies.addAttribute("src", settings.getPublicUrl() + "/resource/script/" + processDefinitionKey + ".js");
            else
                dependencies.addAttribute("src", settings.getApplicationUrl() + "/resource/script/" + processDefinitionKey + ".js");

            tag.addChild(dependencies);
        }
    }

    class HeadDecorator implements TagDecorator {

        private final Process process;
        private final Form form;

        public HeadDecorator(Process process, Form form) {
            this.process = process;
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag) {
            TagNode dependencies = new TagNode("link");
            dependencies.addAttribute("id", "piecework-stylesheet");
            dependencies.addAttribute("type", "text/css");
            dependencies.addAttribute("rel", "stylesheet");

            String processDefinitionKey = process.getProcessDefinitionKey();
            if (form.isAnonymous())
                dependencies.addAttribute("href", settings.getPublicUrl() + "/resource/css/" + processDefinitionKey + ".css");
            else
                dependencies.addAttribute("href", settings.getApplicationUrl() + "/resource/css/" + processDefinitionKey + ".css");

            tag.addChild(dependencies);
        }
    }

    class AttachmentsDecorator implements TagDecorator {

        private final Form form;

        public AttachmentsDecorator(Form form) {
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag) {
            String contentType = tag.getAttributeByName("data-process-content-type");
            String[] contentTypes = contentType != null ? contentType.split("\\s*,\\s*") : new String[0];
            Set<String> contentTypeSet = Sets.newHashSet(contentTypes);

            if (form != null && form.getAttachments() != null && !form.getAttachments().isEmpty()) {
                tag.removeAllChildren();

                TagNode ulNode = new TagNode("ul");
                ulNode.addAttribute("class", "attachments");

                for (Attachment attachment : form.getAttachments()) {
                    if (contentTypeSet.isEmpty() || (attachment.getContentType() != null && contentTypeSet.contains(attachment.getContentType()))) {
                        TagNode liNode = new TagNode("li");
                        TagNode anchorNode = new TagNode("a");
                        anchorNode.addAttribute("href", attachment.getLink());
                        anchorNode.addChild(new ContentNode(attachment.getName()));
                        ulNode.addChild(liNode);
                    }
                }
            }
        }

    }

    class ButtonDecorator implements TagDecorator {

        private final boolean readonly;

        public ButtonDecorator(boolean readonly) {
            this.readonly = readonly;
        }

        @Override
        public void decorate(TagNode tag) {
            Map<String, String> attributes = new HashMap<String, String>();
            if (readonly)
                attributes.put("disabled", "disabled");
            attributes.putAll(tag.getAttributes());
        }

    }

    class FormDecorator implements TagDecorator {

        private final Form form;
        private final Map<String, List<Value>> data;

        public FormDecorator(Form form) {
            this.form = form;
            this.data = form.getData();
        }

        @Override
        public void decorate(TagNode tag) {
            String type = tag.getAttributeByName("data-process-form");
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.putAll(tag.getAttributes());

            if (type == null || type.equals("main")) {
                String formUri = form.getAction() != null ? form.getAction() : "";
                attributes.put("action", formUri);
                attributes.put("method", "POST");
                if (!form.isAnonymous())
                    attributes.put("enctype", "multipart/form-data");
            } else if (type.equals("attachments")) {
                String attachmentUri = form.getAttachment() != null ? form.getAttachment() : "";
                attributes.put("action", attachmentUri);
                attributes.put("method", "POST");
            } else if (type.equals("cancellation")) {
                String attachmentUri = form.getCancellation() != null ? form.getCancellation() : "";
                attributes.put("action", attachmentUri);
                attributes.put("method", "POST");
            }
            tag.setAttributes(attributes);
        }

    }

//    class SectionDecorator implements TagDecorator {
//
//        private final Section section;
//
//        public SectionDecorator(Section section) {
//            this.section = section;
//        }
//
//        @Override
//        public void decorate(TagNode tag, String id, String cls, String name, String variable) {
//
//        }
//
//        @Override
//        public boolean canDecorate(TagNode tag, String id, String cls, String name, String variable) {
//            return id == null || section.getTagId() != null && section.getTagId().equals(id);
//        }
//
//        public boolean isReusable() {
//            return true;
//        }
//
//    }

    class ScriptDecorator implements TagDecorator {

        @Override
        public void decorate(TagNode tag) {
//            String src = tag.getAttributeByName("src");
//
//            if (!src.startsWith("/") && !src.startsWith("http://") && !src.startsWith("https://")) {
//                tag.addAttribute("src", form.getStaticRoot() + "/" + src);
//            }
            String id = tag.getAttributeByName("id");
            if (StringUtils.isEmpty(id) || !id.startsWith("piecework-"))
                tag.removeFromTree();
        }
    }

    class StyleSheetDecorator implements TagDecorator {

        @Override
        public void decorate(TagNode tag) {
//            String href = tag.getAttributeByName("href");
//
//            if (!href.startsWith("/") && !href.startsWith("http://") && !href.startsWith("https://")) {
//                tag.addAttribute("href", form.getStaticRoot() + "/" + href);
//            }

            String id = tag.getAttributeByName("id");
            if (StringUtils.isEmpty(id) || !id.startsWith("piecework-"))
                tag.removeFromTree();
        }

    }

    class VariableDecorator implements TagDecorator {

        private final Map<String, List<Value>> data;
        private final Map<String, List<Message>> results;

        public VariableDecorator(Form form) {
            this.data = form.getData();
            this.results = form.getValidation();
        }

        @Override
        public void decorate(TagNode tag) {
            String cls = tag.getAttributeByName("class");
            String variable = tag.getAttributeByName("data-element");

            if (cls != null && !cls.equals("process-variable"))
                return;

            if (StringUtils.isEmpty(variable))
                return;

            String fieldName = variable;
            String attributeName = null;

            int indexOf = variable.indexOf('.');
            if (indexOf != -1) {
                fieldName = variable.substring(0, indexOf);

                if (variable.length() > indexOf)
                    attributeName = variable.substring(indexOf + 1);
            }

            List<Value> values = data.get(fieldName);

            String tagName = tag.getName() != null ? tag.getName() : "";

            if (tagName.equalsIgnoreCase("input")) {
                // Only modify the value if the input tag is disabled and it's a text input
                String type = tag.getAttributeByName("type");
                String disabled = tag.getAttributeByName("disabled");

                if (type != null && disabled != null && type.equals("text")) {
                    if (values != null) {
                        for (Value value : values) {
                            tag.addAttribute("value", value.getValue());
                            break;
                        }
                    }
                }
            } else {
                tag.removeAllChildren();
                if (values != null) {
                    for (Value value : values) {
                        if (value instanceof User) {
                            User user = User.class.cast(value);
                            if (user != null) {
                                if (attributeName == null || attributeName.equals("displayName"))
                                    tag.addChild(new ContentNode(user.getDisplayName()));
                                else if (attributeName.equals("visibleId"))
                                    tag.addChild(new ContentNode(user.getVisibleId()));
                            }
                        } else if (value != null && StringUtils.isNotEmpty(value.getValue())) {
                            tag.addChild(new ContentNode(value.getValue()));
                        }
                    }
                } else if (fieldName.equals("ConfirmationNumber")) {
                    tag.addChild(new ContentNode(form.getProcessInstanceId()));
                }
            }
        }

        Map<String, List<Value>> getData() {
            return data;
        }

        Map<String, List<Message>> getResults() {
            return results;
        }

    }

    class FieldDecorator implements TagDecorator {

        private final Field field;
        private final FieldTag fieldTag;
        private final List<Value> values;
        private final List<Message> messages;
        private final boolean readonly;
        private int index;

        public FieldDecorator(Field field, List<Value> values, List<Message> messages, boolean readonly) {
            this.field = field;
            this.fieldTag = FieldTag.getInstance(field.getType());
            this.messages = messages;
            this.values = values;
            this.readonly = readonly;
            this.index = 0;
        }

        @Override
        public void decorate(TagNode tag) {
            String tagName = tag.getName();
            if (StringUtils.isEmpty(tagName))
                return;
            if (fieldTag == null)
                return;

            if (!fieldTag.getTagName().equals(tagName))
                return;

            String inputName = tag.getAttributeByName("name");

            if (StringUtils.isEmpty(inputName))
                return;
            if (StringUtils.isEmpty(field.getName()))
                return;
            if (!inputName.equals(field.getName()))
                return;

            Map<String, String> attributes = new HashMap<String, String>();
            attributes.putAll(tag.getAttributes());
            attributes.putAll(fieldTag.getAttributes());

//            if (StringUtils.isNotEmpty(field.getAccept()))
//                attributes.put("accept", field.getAccept());

            tag.setAttributes(attributes);

            Value value = null;

            String cls = tag.getAttributeByName("class");
            if (values != null) {
                if (values.size() > index) {
                    value = values.get(index);
                }

//                if (messages != null && !messages.isEmpty()) {
//                    StringBuilder builder = new StringBuilder();
//                    Iterator<Message> iterator = messages.iterator();
//                    while (iterator.hasNext()) {
//                        Message message = iterator.next();
//                        builder.append(message.getText());
//                        if (iterator.hasNext())
//                            builder.append(",");
//                    }
//                    attributes.put("data-process-messages", builder.toString());
//                }
            }

            if (!field.isVisible() && StringUtils.isNotEmpty(cls))
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
                    if (value != null) {
                        if (value instanceof User) {
                            User user = User.class.cast(value);
                            if (user != null && user.getDisplayName() != null)
                                attributes.put("value", user.getDisplayName());
                        } else if (value.getValue() != null) {
                            attributes.put("value", value.getValue());
                        }
                    }
                    break;
                case TEXTAREA:
                    if (value != null && value.getValue() != null) {
                        tag.removeAllChildren();
                        tag.addChild(new ContentNode(value.getValue()));
                    }
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
                            if (option.getValue() != null && value != null && option.getValue().equals(value.getValue()))
                                optionAttributes.put("selected", "selected");
                            optionNode.setAttributes(optionAttributes);
                            if (StringUtils.isNotEmpty(option.getLabel()))
                                optionNode.addChild(new ContentNode(option.getLabel()));
                            else if (StringUtils.isNotEmpty(option.getValue()))
                                optionNode.addChild(new ContentNode(option.getValue()));
                            else
                                optionNode.addChild(new ContentNode(""));
                            tag.addChild(optionNode);
                        }
                    }
                    break;
            }

            if (readonly)
                attributes.put("disabled", "disabled");

            this.index++;
        }

        public FieldTag getFieldTag() {
            return fieldTag;
        }

    }

    class ImageFieldDecorator implements TagDecorator {

        private final Field field;
        private final List<Value> values;

        public ImageFieldDecorator(Field field, List<Value> values) {
            this.field = field;
            this.values = values;
        }

        @Override
        public void decorate(TagNode tag) {
            if (values != null) {
                for (Value value : values) {
                    if (value instanceof File) {
                        File file = File.class.cast(value);
                        Map<String, String> attributes = new HashMap<String, String>();
                        attributes.putAll(tag.getAttributes());
                        attributes.put("src", file.getLink());
                        tag.setAttributes(attributes);
                        break;
                    }
                }
            }
        }

    }

    class FileFieldFormDecorator implements TagDecorator {

        private final Field field;
        private final boolean readonly;

        public FileFieldFormDecorator(Field field, boolean readonly) {
            this.field = field;
            this.readonly = readonly;
        }

        @Override
        public void decorate(TagNode tag) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.putAll(tag.getAttributes());
            if (!readonly) {
                attributes.put("action", field.getLink());
                attributes.put("method", "POST");
                attributes.put("enctype", "multipart/form-data");
            }
            tag.setAttributes(attributes);
        }

    }

}
