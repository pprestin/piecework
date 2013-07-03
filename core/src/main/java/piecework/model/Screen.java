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
package piecework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.view.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Screen.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Screen.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "screen")
public class Screen implements Serializable {

	private static final long serialVersionUID = -455579611494172459L;

	@XmlAttribute
	@XmlID
	@Id
	private final String screenId;
	
	@XmlAttribute
	@Transient
	private final String processDefinitionKey;
	
	@XmlElement
	private final String title;
	
	@XmlElement
	private final String type;
	
	@XmlElement
	private final String location;

    @XmlAttribute
    private final boolean isAttachmentAllowed;

    @XmlElementWrapper(name="stylesheets")
    private final List<String> stylesheets;

    @XmlElementWrapper(name="groupings")
    @XmlElementRef
    private final List<Grouping> groupings;

	@XmlElementWrapper(name="sections")
	@XmlElementRef
	private final List<Section> sections;

    @XmlElementWrapper(name="constraints")
    @XmlElementRef
    private final List<Constraint> constraints;

    @XmlAttribute
    private final int reviewIndex;

	@XmlAttribute
    private final int ordinal;
	
	@XmlTransient
	@JsonIgnore
	private final boolean isDeleted;
	
	@XmlAttribute
	private final String link;

	private Screen() {
		this(new Screen.Builder(), new ViewContext());
	}

	private Screen(Screen.Builder builder, ViewContext context) {
		this.screenId = builder.screenId;
		this.processDefinitionKey = builder.processDefinitionKey;
		this.title = builder.title;
		this.type = builder.type;
		this.location = builder.location;
		this.ordinal = builder.ordinal;
		this.isDeleted = builder.isDeleted;
        this.isAttachmentAllowed = builder.isAttachmentAllowed;
        this.groupings = Collections.unmodifiableList(builder.groupings);
        this.stylesheets = Collections.unmodifiableList(builder.stylesheets);
		this.sections = Collections.unmodifiableList(builder.sections);
        this.constraints = builder.constraints != null ? Collections.unmodifiableList(builder.constraints) : null;
        this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.interactionId, builder.screenId) : null;
	    this.reviewIndex = builder.reviewIndex;
    }
	
	public String getScreenId() {
		return screenId;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getLink() {
		return link;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public String getLocation() {
		return location;
	}

    public boolean isAttachmentAllowed() {
        return isAttachmentAllowed;
    }

    public List<Grouping> getGroupings() {
        return groupings;
    }

    public List<String> getStylesheets() {
        return stylesheets;
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public int getReviewIndex() {
        return reviewIndex;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

		private String screenId;
		private String processDefinitionKey;
		private String interactionId;
		private String title;
		private String type;
		private String location;
        private boolean isAttachmentAllowed;
        private List<Grouping> groupings;
        private List<String> stylesheets;
		private List<Section> sections;
        private List<Constraint> constraints;
        private int reviewIndex;
		private int ordinal;
		private boolean isDeleted;
		
		public Builder() {
			super();
            this.groupings = new ArrayList<Grouping>();
            this.stylesheets = new ArrayList<String>();
            this.sections = new ArrayList<Section>();
            this.constraints = new ArrayList<Constraint>();
		}

        public Builder(Screen screen, Sanitizer sanitizer) {
            this(screen, sanitizer, true);
        }

		public Builder(Screen screen, Sanitizer sanitizer, boolean includeSections) {
			this.screenId = sanitizer.sanitize(screen.screenId);
			this.processDefinitionKey = sanitizer.sanitize(screen.processDefinitionKey);
			this.title = sanitizer.sanitize(screen.title);
			this.type = sanitizer.sanitize(screen.type);
            this.isAttachmentAllowed = screen.isAttachmentAllowed;
			this.location = sanitizer.sanitize(screen.location);
            this.reviewIndex = screen.reviewIndex;
			this.ordinal = screen.ordinal;

            if (screen.groupings != null && !screen.groupings.isEmpty()) {
                this.groupings = new ArrayList<Grouping>(screen.groupings.size());
                for (Grouping grouping : screen.groupings) {
                    this.groupings.add(new Grouping.Builder(grouping, sanitizer).build());
                }
            } else {
                this.groupings = new ArrayList<Grouping>();
            }

            if (screen.stylesheets != null && !screen.stylesheets.isEmpty()) {
                this.stylesheets = new ArrayList<String>(screen.stylesheets.size());
                for (String stylesheet : screen.stylesheets) {
                    this.stylesheets.add(sanitizer.sanitize(stylesheet));
                }
            } else {
                this.stylesheets = new ArrayList<String>();
            }

            if (screen.constraints != null && !screen.constraints.isEmpty()) {
                this.constraints = new ArrayList<Constraint>(screen.constraints.size());
                for (Constraint constraint : screen.constraints) {
                    this.constraints.add(new Constraint.Builder(constraint, sanitizer).build());
                }
            } else {
                this.constraints = new ArrayList<Constraint>();
            }

			if (includeSections && screen.sections != null && !screen.sections.isEmpty()) {
				this.sections = new ArrayList<Section>(screen.sections.size());
				for (Section section : screen.sections) {
					this.sections.add(new Section.Builder(section, sanitizer).processDefinitionKey(processDefinitionKey).build());
				}
			} else {
                this.sections = new ArrayList<Section>();
            }
		}

		public Screen build() {
			return new Screen(this, null);
		}

		public Screen build(ViewContext context) {
			return new Screen(this, context);
		}
		
		public Builder screenId(String screenId) {
			this.screenId = screenId;
			return this;
		}
		
		public Builder processDefinitionKey(String processDefinitionKey) {
			this.processDefinitionKey = processDefinitionKey;
			return this;
		}
		
		public Builder interactionId(String interactionId) {
			this.interactionId = interactionId;
			return this;
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		public Builder type(String type) {
			this.type = type;
			return this;
		}
		
		public Builder location(String location) {
			this.location = location;
			return this;
		}

        public Builder attachmentAllowed(boolean isAttachmentAllowed) {
            this.isAttachmentAllowed = isAttachmentAllowed;
            return this;
        }

        public Builder constraint(Constraint constraint) {
            if (this.constraints == null)
                this.constraints = new ArrayList<Constraint>();
            this.constraints.add(constraint);
            return this;
        }

        public Builder grouping(Grouping grouping) {
            if (this.groupings == null)
                this.groupings = new ArrayList<Grouping>();
            this.groupings.add(grouping);
            return this;
        }

		public Builder section(Section section) {
			if (this.sections == null)
				this.sections = new ArrayList<Section>();
			this.sections.add(section);
			return this;
		}

        public Builder reviewIndex(int reviewIndex) {
            this.reviewIndex = reviewIndex;
            return this;
        }

		public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }
		
		public Builder delete() {
			this.isDeleted = true;
			return this;
		}
		
		public Builder undelete() {
			this.isDeleted = false;
			return this;
		}
	}
	
	public static class Constants {
		public static final String RESOURCE_LABEL = "Screen";
		public static final String ROOT_ELEMENT_NAME = "screen";
		public static final String TYPE_NAME = "ScreenType";
	}
}
