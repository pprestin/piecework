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
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ViewContext;
import piecework.enumeration.DataInjectionStrategy;
import piecework.enumeration.ScreenUsage;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

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

    @XmlAttribute
    private final ScreenUsage usage;

    @XmlElement
    private final DataInjectionStrategy strategy;
	
	@XmlElement
	private final String location;

    @XmlAttribute
    private final boolean isAttachmentAllowed;

    @XmlElementWrapper(name="groupings")
    @XmlElementRef
    private final List<Grouping> groupings;

    @XmlElementWrapper(name="constraints")
    @XmlElementRef
    private final List<Constraint> constraints;

    @XmlElementWrapper(name="sections")
    @XmlElementRef
    @Transient
    private final List<Section> sections;

    @XmlAttribute
    private final int maxActiveGroupingIndex;

	@XmlAttribute
    private final int ordinal;

    @XmlAttribute
    private final boolean readonly;

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
        this.usage = builder.usage;
        this.strategy = builder.strategy;
		this.location = builder.location;
		this.ordinal = builder.ordinal;
		this.isDeleted = builder.isDeleted;
        this.isAttachmentAllowed = builder.isAttachmentAllowed;
        this.groupings = Collections.unmodifiableList(builder.groupings);
//        this.stylesheets = Collections.unmodifiableList(builder.stylesheets);
//        this.scripts = Collections.unmodifiableList(builder.scripts);
		this.constraints = builder.constraints != null ? Collections.unmodifiableList(builder.constraints) : null;
        this.sections = Collections.unmodifiableList(builder.sections);
        this.link = context != null ? context.getApplicationUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.interactionId, builder.screenId) : null;
	    this.maxActiveGroupingIndex = builder.maxActiveGroupingIndex;
        this.readonly = builder.readonly;
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

    public ScreenUsage getUsage() {
        return usage;
    }

    public DataInjectionStrategy getStrategy() {
        return strategy;
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

//    public List<File> getStylesheets() {
//        return stylesheets;
//    }
//
//    public List<File> getScripts() {
//        return scripts;
//    }

    public List<Section> getSections() {
        return sections;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public int getMaxActiveGroupingIndex() {
        return maxActiveGroupingIndex;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public boolean isReadonly() {
        return readonly;
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
        private ScreenUsage usage;
        private DataInjectionStrategy strategy;
		private String location;
        private boolean isAttachmentAllowed;
        private List<Grouping> groupings;
        private List<File> stylesheets;
        private List<File> scripts;
        private List<Constraint> constraints;
        private List<Section> sections;
        private int maxActiveGroupingIndex;
		private int ordinal;
        private boolean readonly;
		private boolean isDeleted;
		
		public Builder() {
			super();
            this.usage = ScreenUsage.DATA_ENTRY;
            this.strategy = DataInjectionStrategy.NONE;
            this.groupings = new ArrayList<Grouping>();
            this.sections = new ArrayList<Section>();
            this.stylesheets = new ArrayList<File>();
            this.scripts = new ArrayList<File>();
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
            this.usage = screen.usage;
            this.strategy = screen.strategy;
            this.isAttachmentAllowed = screen.isAttachmentAllowed;
			this.location = sanitizer.sanitize(screen.location);
            this.maxActiveGroupingIndex = screen.maxActiveGroupingIndex;
			this.ordinal = screen.ordinal;

            if (screen.groupings != null && !screen.groupings.isEmpty()) {
                this.groupings = new ArrayList<Grouping>(screen.groupings.size());
                for (Grouping grouping : screen.groupings) {
                    this.groupings.add(new Grouping.Builder(grouping, sanitizer).build());
                }
            } else {
                this.groupings = new ArrayList<Grouping>();
            }

//            if (screen.stylesheets != null && !screen.stylesheets.isEmpty()) {
//                this.stylesheets = new ArrayList<File>(screen.stylesheets.size());
//                for (File stylesheet : screen.stylesheets) {
//                    this.stylesheets.add(new File.Builder(stylesheet, sanitizer).build());
//                }
//            } else {
//                this.stylesheets = new ArrayList<File>();
//            }
//
//            if (screen.scripts != null && !screen.scripts.isEmpty()) {
//                this.scripts = new ArrayList<File>(screen.scripts.size());
//                for (File script : screen.scripts) {
//                    this.scripts.add(new File.Builder(script, sanitizer).build());
//                }
//            } else {
//                this.stylesheets = new ArrayList<File>();
//            }

//            if (screen.constraints != null && !screen.constraints.isEmpty()) {
//                this.constraints = new ArrayList<Constraint>(screen.constraints.size());
//                for (Constraint constraint : screen.constraints) {
//                    this.constraints.add(new Constraint.Builder(constraint, sanitizer).build());
//                }
//            } else {
//                this.constraints = new ArrayList<Constraint>();
//            }

            if (includeSections && screen.sections != null && !screen.sections.isEmpty()) {
                this.sections = new ArrayList<Section>(screen.sections.size());
                for (Section section : screen.sections) {
                    this.sections.add(new Section.Builder(section, sanitizer).processDefinitionKey(processDefinitionKey).build());
                }
            } else {
                this.sections = new ArrayList<Section>();
            }
            this.readonly = screen.readonly;
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

        public Builder strategy(DataInjectionStrategy strategy) {
            this.strategy = strategy;
            return this;
        }
		
		public Builder location(String location) {
			this.location = location;
			return this;
		}

        public Builder stylesheet(File stylesheet) {
            if (stylesheet != null)
                this.stylesheets.add(stylesheet);
            return this;
        }

        public Builder script(File script) {
            if (script != null)
                this.scripts.add(script);
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

        public Builder maxActiveGroupingIndex(int maxActiveGroupingIndex) {
            this.maxActiveGroupingIndex = maxActiveGroupingIndex;
            return this;
        }

        public Builder usage(ScreenUsage usage) {
            this.usage = usage;
            return this;
        }

		public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }

        public Builder readonly() {
            this.readonly = true;
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
