package piecework.enumeration;

import org.apache.commons.lang.StringUtils;
import piecework.Constants;
import piecework.form.FieldAttributeDefinition;
import piecework.form.FieldTagDefinition;

import java.util.Map;

/**
 * @author James Renfro
 */
public enum FieldTag {
    CHECKBOX(Constants.FieldTypes.CHECKBOX, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "checkbox"))),
    DATE(Constants.FieldTypes.DATE, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "date"))),
    EMAIL(Constants.FieldTypes.EMAIL, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "email"))),
    FILE(Constants.FieldTypes.FILE, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "file"))),
    HTML(Constants.FieldTypes.HTML, new FieldTagDefinition("div")),
    NUMBER(Constants.FieldTypes.NUMBER, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "number"))),
    PERSON(Constants.FieldTypes.PERSON, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "text"))),
    RADIO(Constants.FieldTypes.RADIO, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "radio"))),
    SELECT_MULTIPLE(Constants.FieldTypes.SELECT_MULTIPLE, new FieldTagDefinition("select", new FieldAttributeDefinition("multiple"))),
    SELECT_ONE(Constants.FieldTypes.SELECT_ONE, new FieldTagDefinition("select")),
    TEXT(Constants.FieldTypes.TEXT, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "text"))),
    TEXTAREA(Constants.FieldTypes.TEXTAREA, new FieldTagDefinition("textarea")),
    URL(Constants.FieldTypes.URL, new FieldTagDefinition("input", new FieldAttributeDefinition("type", "url")));

    private String fieldType;
    private FieldTagDefinition definition;

    private FieldTag(String fieldType, FieldTagDefinition definition) {
        this.fieldType = fieldType;
        this.definition = definition;
    }

    public String getTagName() {
        return definition.getTagName();
    }

    public String getFieldType() {
        return fieldType;
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

    public static FieldTag getInstance(String tagName, String type, String multiple) {
        if (tagName.equalsIgnoreCase("input")) {
            if (type.equalsIgnoreCase(Constants.FieldTypes.CHECKBOX))
                return CHECKBOX;
            if (type.equalsIgnoreCase(Constants.FieldTypes.DATE))
                return DATE;
            if (type.equalsIgnoreCase(Constants.FieldTypes.EMAIL))
                return EMAIL;
            if (type.equalsIgnoreCase(Constants.FieldTypes.FILE))
                return FILE;
            if (type.equalsIgnoreCase(Constants.FieldTypes.NUMBER))
                return NUMBER;
            if (type.equalsIgnoreCase(Constants.FieldTypes.PERSON))
                return PERSON;
            if (type.equalsIgnoreCase(Constants.FieldTypes.RADIO))
                return RADIO;
            if (type.equalsIgnoreCase(Constants.FieldTypes.TEXT))
                return TEXT;
            if (type.equalsIgnoreCase(Constants.FieldTypes.URL))
                return URL;
        } else if (tagName.equalsIgnoreCase("select")) {
            if (StringUtils.isNotEmpty(multiple))
                return SELECT_MULTIPLE;
            return SELECT_ONE;
        } else if (tagName.equalsIgnoreCase(Constants.FieldTypes.TEXTAREA))
            return TEXTAREA;

        return null;
    }

}
