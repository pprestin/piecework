package piecework.enumeration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author James Renfro
 */
@XmlType(name = "OperationEnumType")
@XmlEnum
public enum OperationType {
    ACTIVATION("activation", "Reactivated"), ASSIGNMENT("assignment", "Assigned"), CANCELLATION("cancellation", "Cancelled"), START("start", "Started"), SUSPENSION("suspension", "Suspended"), UPDATE("update", "Updated");

    private final String value;
    private final String description;

    private OperationType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonIgnore
    public String description() {
        return description;
    }

}
