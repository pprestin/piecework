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

import org.htmlcleaner.TagNode;
import piecework.model.*;
import piecework.util.ManyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class DecoratorFactory {

    private final Form form;
    private final ManyMap<String, TagDecorator> decoratorMap;

    public DecoratorFactory(Form form) {
        this.form = form;
        this.decoratorMap = new ManyMap<String, TagDecorator>();
    }

    public void initialize() {
        decoratorMap.putOne("form", new FormDecorator(form));

        Screen screen = form.getScreen();

        if (screen != null) {
            List<Section> sections = screen.getSections();
            if (sections != null && !sections.isEmpty()) {
                for (Section section : sections) {

                    // TODO: Find form data and include


                }
            }
        }
    }

    public TagDecorator decorator(String tagName, String id, String cls, String name) {
        List<TagDecorator> decorators = decoratorMap.get(tagName);

        if (decorators == null || decorators.isEmpty())
            return null;

        for (TagDecorator decorator : decorators) {
            if (decorator != null && decorator.canDecorate(id, cls, name)) {
                return decorator;
            }
        }

        return null;
    }

    class FormDecorator implements TagDecorator {

        private final Form form;

        public FormDecorator(Form form) {
            this.form = form;
        }

        @Override
        public void decorate(TagNode tag) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("action", form.getUri());
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
        public void decorate(TagNode tag) {

        }

        @Override
        public boolean canDecorate(String id, String cls, String name) {
            return id == null || section.getTagId() != null && section.getTagId().equals(id);
        }

        public boolean isReusable() {
            return true;
        }

    }

    class InputDecorator implements TagDecorator {

        private final Field field;
        private final FormValue formValue;

        public InputDecorator(Field field, FormValue formValue) {
            this.field = field;
            this.formValue = formValue;
        }

        @Override
        public void decorate(TagNode tag) {

            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("value", form.getUri());
            attributes.put("method", "POST");
            attributes.put("enctype", "multipart/form-data");
            tag.setAttributes(attributes);
        }

        @Override
        public boolean canDecorate(String id, String cls, String name) {

            if (id != null && id.equals(field.getFieldId()))
                return true;

            if (name != null && name.equals(field.getName()))
                return true;

            return false;
        }

        public boolean isReusable() {
            return true;
        }

    }


}
