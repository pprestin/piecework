package piecework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Paul Prestin
 */


@XmlRootElement(name = Attachment.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Attachment.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Attachment.Constants.ROOT_ELEMENT_NAME)
public class Acumen implements Serializable {

    private static final long serialVersionUID = 8567425873159842685L;

    @XmlElement
    private final String reason;

    private Acumen() {
        this(new Acumen.Builder(), new ViewContext());
    }

    private Acumen(Acumen.Builder builder, ViewContext context) {
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

        public Builder(Acumen acumen) {
            this.reason = acumen.reason;
        }

        public Builder(Acumen acumen, Sanitizer sanitizer) {
            this.reason = sanitizer.sanitize(acumen.reason);
        }

        public Acumen build() {
            return new Acumen(this, null);
        }

        public Acumen build(ViewContext context) {
            return new Acumen(this, context);
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
    }
}