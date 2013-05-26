package piecework.form;

import java.util.Map;

/**
 * @author James Renfro
 */
public enum FieldTag {
    CHECKBOX(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "checkbox"))),
    FILE(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "file"))),
    RADIO(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "radio"))),
    SELECT_MULTIPLE(new FieldTagDefinition("select", new FieldAttributeDefinition("multiple"))),
    SELECT_ONE(new FieldTagDefinition("select")),
    TEXT(new FieldTagDefinition("input", new FieldAttributeDefinition("type", "text"))),
    TEXTAREA(new FieldTagDefinition("textarea"));

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
        String uppercase = type.toUpperCase();
        uppercase = uppercase.replace('-', '_');
        return FieldTag.valueOf(uppercase);
    }
}
