package piecework.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author James Renfro
 */
@XmlType(name = "EventEnumType")
@XmlEnum
public enum EventType {
    OPERATION("operation"), TASK("task");

    private final String value;

    private EventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
