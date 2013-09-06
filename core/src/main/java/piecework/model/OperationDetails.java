package piecework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author Paul Prestin
 */


@XmlRootElement(name = OperationDetails.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = OperationDetails.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationDetails implements Serializable {

    private static final long serialVersionUID = 8567425873159842685L;

    @XmlElement
    private final String reason;

    private OperationDetails() {
        this(new OperationDetails.Builder(), new ViewContext());
    }

    private OperationDetails(OperationDetails.Builder builder, ViewContext context) {
        this.reason = builder.reason;
    }

    public String getReason() {
        return reason;
    }

    public final static class Builder {

        private String reason;

        public Builder() {
            super();
        }

        public Builder(OperationDetails acumen) {
            this.reason = acumen.reason;
        }

        public Builder(OperationDetails acumen, Sanitizer sanitizer) {
            this.reason = sanitizer.sanitize(acumen.reason);
        }

        public OperationDetails build() {
            return new OperationDetails(this, null);
        }

        public OperationDetails build(ViewContext context) {
            return new OperationDetails(this, context);
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "OperationDetails";
        public static final String ROOT_ELEMENT_NAME = "operationDetails";
        public static final String TYPE_NAME = "OperationDetailsType";
    }
}