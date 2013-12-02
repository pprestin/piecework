package piecework.enumeration;

import piecework.form.FieldAttributeDefinition;
import piecework.form.FieldTagDefinition;

import java.util.Map;

/**
 * @author James Renfro
 */
public enum FieldTag {
    CHECKBOX(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "checkbox"))),
    DATE(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "date"))),
    EMAIL(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "email"))),
    FILE(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "file"))),
    HTML(new FieldTagDefinition("div")),
    NUMBER(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "number"))),
    PERSON(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "text"))),
    RADIO(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "radio"))),
    SELECT_MULTIPLE(new FieldTagDefinition("select", new FieldAttributeDefinition("multiple"))),
    SELECT_ONE(new FieldTagDefinition("select")),
    TEXT(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "text"))),
    TEXTAREA(new FieldTagDefinition("textarea")),
    URL(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "url")));

    private FieldTagDefinition definition;

    private FieldTag(FieldTagDefinition definition) {
        this.definition = definition;
    }

    public String getTagName() {
        return definition.getTagName();
    }

    public Map<String, String> getAttributes() {
        return definition.getAttributes();
    }

    public static FieldTag getInstance(String type) {
        if (type == null)
            return TEXT;

        String uppercase = type.toUpperCase();
        uppercase = uppercase.replace('-', '_');
        return FieldTag.valueOf(uppercase);
    }
}
