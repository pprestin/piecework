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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import piecework.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
public class ScreenBuildingVisitor implements TagNodeVisitor {

    private static final Set<String> INPUT_TAG_NAMES = Sets.newHashSet("input", "select", "textarea");

    private List<Grouping.Builder> groupingBuilders;
    private List<Section.Builder> sectionBuilders;

    public ScreenBuildingVisitor() {
        this.sectionBuilders = new ArrayList<Section.Builder>();
    }

    public Screen build() {
        Screen.Builder screenBuilder = new Screen.Builder();
        if (groupingBuilders != null) {
            for (Grouping.Builder groupingBuilder : groupingBuilders) {
                screenBuilder.grouping(groupingBuilder.build());
            }
        }
        if (sectionBuilders != null) {
            for (Section.Builder sectionBuilder : sectionBuilders) {
                screenBuilder.section(sectionBuilder.build());
            }
        }
        return screenBuilder.build();
    }

    public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
        if (htmlNode instanceof TagNode) {
            TagNode tag = (TagNode) htmlNode;
            String tagName = tag.getName();

            if (tagName.equals("input")) {
                createFieldFromInputTag(tag);
            } else if (tagName.equals("select")) {
                createFieldFromSelectTag(tag);
            } else if (tagName.equals("textarea")) {
                createFieldFromTextareaTag(tag);
            } else if (tagName.equals("div")) {
                String id = tag.getAttributeByName("id");
                String title = tag.getAttributeByName("title");
                Set<String> classes = getClasses(tag);
                if (!classes.isEmpty() && classes.contains("section")) {
                    Section.Builder sectionBuilder = new Section.Builder();
                    if (StringUtils.isNotEmpty(id))
                        sectionBuilder.tagId(id);
                    if (StringUtils.isNotEmpty(title))
                        sectionBuilder.title(title);
                    sectionBuilder.ordinal(sectionBuilders.size() + 1);
                    sectionBuilders.add(sectionBuilder);
                }
            } else if (tagName.equals("button")) {
                String type = tag.getAttributeByName("type");
                Button.Builder buttonBuilder = new Button.Builder()
                        .type(type);

                Grouping.Builder groupingBuilder = getCurrentGroupingBuilder();
                buttonBuilder.ordinal(groupingBuilder.numberOfButtons() + 1);
                groupingBuilder.button(buttonBuilder.build());
            }
        }
        return true;
    }

    private void createFieldFromInputTag(TagNode tag) {
        String name = tag.getAttributeByName("name");
        Set<String> classes = getClasses(tag);
        String type = tag.getAttributeByName("type");
        String disabled = tag.getAttributeByName("disabled");
        String required = tag.getAttributeByName("required");
        String value = tag.getAttributeByName("value");

        Field.Builder fieldBuilder = new Field.Builder()
                .name(name)
                .type(type);

        if (StringUtils.isEmpty(disabled))
            fieldBuilder.editable();
        if (StringUtils.isNotEmpty(required))
            fieldBuilder.required();
        if (classes.contains("restricted"))
            fieldBuilder.restricted();
        if (StringUtils.isNotEmpty(value))
            fieldBuilder.defaultValue(value);

        Section.Builder sectionBuilder = getCurrentSectionBuilder();
        fieldBuilder.ordinal(sectionBuilder.numberOfFields() + 1);
        sectionBuilder.field(fieldBuilder.build());
    }

    private void createFieldFromSelectTag(TagNode tag) {
        String name = tag.getAttributeByName("name");
        Set<String> classes = getClasses(tag);
        String multiple = tag.getAttributeByName("multiple");
        String disabled = tag.getAttributeByName("disabled");
        String required = tag.getAttributeByName("required");
        String value = tag.getAttributeByName("value");

        String type = "select-one";
        if (StringUtils.isNotEmpty(multiple))
            type = "select-multiple";

        Field.Builder fieldBuilder = new Field.Builder()
                .name(name)
                .type(type);

        if (StringUtils.isEmpty(disabled))
            fieldBuilder.editable();
        if (StringUtils.isNotEmpty(required))
            fieldBuilder.required();
        if (classes.contains("restricted"))
            fieldBuilder.restricted();

        List<TagNode> children = tag.getAllChildren();
        for (TagNode child : children) {
            if (child.getName().equals("option")) {
                String optionValue = child.getAttributeByName("value");
                String optionLabel = child.getText() != null ? child.getText().toString() : "";

                fieldBuilder.option(new Option.Builder().value(optionValue).label(optionLabel).build());
            }
        }

        Section.Builder sectionBuilder = getCurrentSectionBuilder();
        fieldBuilder.ordinal(sectionBuilder.numberOfFields() + 1);
        sectionBuilder.field(fieldBuilder.build());
    }

    private void createFieldFromTextareaTag(TagNode tag) {
        String name = tag.getAttributeByName("name");
        Set<String> classes = getClasses(tag);
        String disabled = tag.getAttributeByName("disabled");
        String required = tag.getAttributeByName("required");
        String value = tag.getText() != null ? tag.getText().toString() : "";

        Field.Builder fieldBuilder = new Field.Builder()
                .name(name)
                .type("textarea");

        if (StringUtils.isEmpty(disabled))
            fieldBuilder.editable();
        if (StringUtils.isNotEmpty(required))
            fieldBuilder.required();
        if (classes.contains("restricted"))
            fieldBuilder.restricted();
        if (StringUtils.isNotEmpty(value))
            fieldBuilder.defaultValue(value);

        Section.Builder sectionBuilder = getCurrentSectionBuilder();
        fieldBuilder.ordinal(sectionBuilder.numberOfFields() + 1);
        sectionBuilder.field(fieldBuilder.build());
    }

    private Grouping.Builder getCurrentGroupingBuilder() {
        Grouping.Builder groupingBuilder;
        if (groupingBuilders.isEmpty()) {
            groupingBuilder = new Grouping.Builder();
            groupingBuilder.ordinal(1);
            groupingBuilders.add(groupingBuilder);
        } else {
            groupingBuilder = groupingBuilders.get(groupingBuilders.size() - 1);
        }
        return groupingBuilder;
    }

    private Section.Builder getCurrentSectionBuilder() {
        Section.Builder sectionBuilder;
        if (sectionBuilders.isEmpty()) {
            sectionBuilder = new Section.Builder();
            sectionBuilder.ordinal(1);
            sectionBuilders.add(sectionBuilder);
        } else {
            sectionBuilder = sectionBuilders.get(sectionBuilders.size() - 1);
        }
        return sectionBuilder;
    }

    private Set<String> getClasses(TagNode tag) {
        String cls = tag.getAttributeByName("class");
        if (cls != null)
            return Sets.newHashSet(cls.split("\\s+"));
        return Collections.emptySet();
    }

}
