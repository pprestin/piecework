/*
 * Copyright 2012 University of Washington
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
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Process.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Process.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "process")
public class Process implements Serializable {

	private static final long serialVersionUID = -4257025274360046038L;

	@XmlAttribute
	@XmlID
	@Id
	private final String processDefinitionKey;
	
	@XmlElement
	private final String processDefinitionLabel;

    @XmlElement
    private final String processInstanceLabelTemplate;
	
	@XmlElement
	private final String processSummary;
	
	@XmlElement
	private final String participantSummary;
	
	@XmlElement
	private final String engine;
	
	@XmlElement
	private final String engineProcessDefinitionKey;

    @XmlElement
    private final String initiationStatus;

    @XmlElement
    private final String cancellationStatus;

    @XmlElement
    private final String completionStatus;

    @XmlElement
    private final String suspensionStatus;

    @XmlAttribute
    @Transient
    private final String link;

	@XmlAttribute
    @Transient
	private final String uri;
	
	@XmlElementWrapper(name="interactions")
	@XmlElementRef
	@DBRef
	private final List<Interaction> interactions;

    @XmlElementWrapper(name="sections")
    @XmlElementRef
    @DBRef
    private final List<Section> sections;

    @XmlElementWrapper(name="notifications")
    @XmlElementRef
    @DBRef
    private final List<Notification> notifications;

    @XmlElement
    @DBRef
    private final Screen defaultScreen;

    @XmlAttribute
    private final boolean isAnonymousSubmissionAllowed;

	@XmlTransient
	@JsonIgnore
	private final boolean isDeleted;
	
	private Process() {
		this(new Process.Builder(), new ViewContext());
	}
			
	@SuppressWarnings("unchecked")
	private Process(Process.Builder builder, ViewContext context) {
		this.processDefinitionKey = builder.processDefinitionKey;
		this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabelTemplate = builder.processInstanceLabelTemplate;
		this.processSummary = builder.processSummary;
		this.participantSummary = builder.participantSummary;
		this.engine = builder.engine;
		this.engineProcessDefinitionKey = builder.engineProcessDefinitionKey;
        this.initiationStatus = builder.initiationStatus;
        this.cancellationStatus = builder.cancellationStatus;
        this.completionStatus = builder.completionStatus;
        this.suspensionStatus = builder.suspensionStatus;
        this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey) : null;
		this.uri = context != null ? context.getServiceUri(builder.processDefinitionKey) : null;
		this.interactions = (List<Interaction>) (builder.interactions != null ? Collections.unmodifiableList(builder.interactions) : Collections.emptyList());
        this.sections = Collections.unmodifiableList(builder.sections);
        this.notifications = (List<Notification>) (builder.notifications != null ? Collections.unmodifiableList(builder.notifications) : Collections.emptyList());
        this.defaultScreen = builder.defaultScreen;
        this.isAnonymousSubmissionAllowed = builder.isAnonymousSubmissionAllowed;
        this.isDeleted = builder.isDeleted;
	}
	
	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getProcessDefinitionLabel() {
		return processDefinitionLabel;
	}

    public String getProcessInstanceLabelTemplate() {
        return processInstanceLabelTemplate;
    }

    public String getLink() {
        return link;
    }

    public String getProcessSummary() {
		return processSummary;
	}

	public String getParticipantSummary() {
		return participantSummary;
	}

	public String getEngine() {
		return engine;
	}

	public String getEngineProcessDefinitionKey() {
		return engineProcessDefinitionKey;
	}

    public String getCancellationStatus() {
        return cancellationStatus;
    }

    public String getSuspensionStatus() {
        return suspensionStatus;
    }

    public String getUri() {
		return uri;
	}

    public String getInitiationStatus() {
        return initiationStatus;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public List<Interaction> getInteractions() {
		return interactions;
	}

    public List<Section> getSections() {
        return sections;
    }

    @JsonIgnore
    public Map<String, Section> getSectionMap() {
        Map<String, Section> sectionMap = new HashMap<String, Section>();
        if (sections != null) {
            for (Section section : sections) {
                if (section == null)
                    continue;

                sectionMap.put(section.getSectionId(), section);
            }
        }
        return sectionMap;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public Screen getDefaultScreen() {
        return defaultScreen;
    }

    public boolean isAnonymousSubmissionAllowed() {
        return isAnonymousSubmissionAllowed;
    }

    @JsonIgnore
	public boolean isDeleted() {
		return isDeleted;
	}

	@XmlTransient
	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isEmpty(processDefinitionLabel) && StringUtils.isEmpty(processSummary) && StringUtils.isEmpty(participantSummary) && StringUtils.isEmpty(engine) && StringUtils.isEmpty(engineProcessDefinitionKey) && (interactions == null || interactions.isEmpty());
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Process process = (Process) o;

        if (!processDefinitionKey.equals(process.processDefinitionKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return processDefinitionKey.hashCode();
    }

    public final static class Builder {
		
		private String processDefinitionKey;		
		private String processDefinitionLabel;
        private String processInstanceLabelTemplate;
		private String processSummary;		
		private String participantSummary;		
		private String engine;		
		private String engineProcessDefinitionKey;
        private String initiationStatus;
        private String cancellationStatus;
        private String completionStatus;
        private String suspensionStatus;
		private List<Interaction> interactions;
        private List<Section> sections;
        private List<Notification> notifications;
        private Screen defaultScreen;
        private boolean isAnonymousSubmissionAllowed;
		private boolean isDeleted;
		
		public Builder() {
			super();
            this.interactions = new ArrayList<Interaction>();
            this.sections = new ArrayList<Section>();
            this.notifications = new ArrayList<Notification>();
		}
				
		public Builder(piecework.model.Process process, Sanitizer sanitizer, boolean includeDetails) {
			this.processDefinitionKey = sanitizer.sanitize(process.processDefinitionKey);
			this.processDefinitionLabel = sanitizer.sanitize(process.processDefinitionLabel);
            this.processInstanceLabelTemplate = sanitizer.sanitize(process.processInstanceLabelTemplate);
			this.processSummary = sanitizer.sanitize(process.processSummary);
			this.participantSummary = sanitizer.sanitize(process.participantSummary);
			this.engine = sanitizer.sanitize(process.engine);
			this.engineProcessDefinitionKey = sanitizer.sanitize(process.engineProcessDefinitionKey);
		    this.initiationStatus = sanitizer.sanitize(process.initiationStatus);
            this.cancellationStatus = sanitizer.sanitize(process.cancellationStatus);
            this.completionStatus = sanitizer.sanitize(process.completionStatus);
            this.suspensionStatus = sanitizer.sanitize(process.suspensionStatus);
            this.defaultScreen = process.defaultScreen != null ? new Screen.Builder(process.defaultScreen, sanitizer).processDefinitionKey(processDefinitionKey).build() : null;
			if (includeDetails && process.interactions != null && !process.interactions.isEmpty()) {
				this.interactions = new ArrayList<Interaction>(process.interactions.size());
				for (Interaction interaction : process.interactions) {
					this.interactions.add(new Interaction.Builder(interaction, sanitizer).processDefinitionKey(processDefinitionKey).build());
				}
			} else {
                this.interactions = new ArrayList<Interaction>();
            }
            if (includeDetails && process.notifications != null && !process.notifications.isEmpty()) {
                this.notifications = new ArrayList<Notification>(process.notifications.size());
                for (Notification notification : process.notifications) {
                    if (notification != null)
                        this.notifications.add(new Notification.Builder(notification, sanitizer).build());
                }
            } else {
                this.notifications = new ArrayList<Notification>();
            }
            if (includeDetails && process.sections != null && !process.sections.isEmpty()) {
                this.sections = new ArrayList<Section>(process.sections.size());
                for (Section section : process.sections) {
                    this.sections.add(new Section.Builder(section, sanitizer).processDefinitionKey(processDefinitionKey).build());
                }
            } else {
                this.sections = new ArrayList<Section>();
            }
            this.isAnonymousSubmissionAllowed = process.isAnonymousSubmissionAllowed;
            this.isDeleted = process.isDeleted;
		}

		public Process build() {
			return new Process(this, null);
		}
		
		public Process build(ViewContext context) {
			return new Process(this, context);
		}

		public Builder processDefinitionKey(String processDefinitionKey) {
			this.processDefinitionKey = processDefinitionKey;
			return this;
		}
		
		public Builder processDefinitionLabel(String processDefinitionLabel) {
			this.processDefinitionLabel = processDefinitionLabel;
			return this;
		}

        public Builder processInstanceLabelTemplate(String processInstanceLabelTemplate) {
            this.processInstanceLabelTemplate = processInstanceLabelTemplate;
            return this;
        }
		
		public Builder processSummary(String processSummary) {
			this.processSummary = processSummary;
			return this;
		}
		
		public Builder participantSummary(String participantSummary) {
			this.participantSummary = participantSummary;
			return this;
		}
		
		public Builder engine(String engine) {
			this.engine = engine;
			return this;
		}
		
		public Builder engineProcessDefinitionKey(String engineProcessDefinitionKey) {
			this.engineProcessDefinitionKey = engineProcessDefinitionKey;
			return this;
		}

        public Builder cancellationStatus(String cancellationStatus) {
            this.cancellationStatus = cancellationStatus;
            return this;
        }

        public Builder completionStatus(String completionStatus) {
            this.completionStatus = completionStatus;
            return this;
        }

        public Builder initiationStatus(String initiationStatus) {
            this.initiationStatus = initiationStatus;
            return this;
        }

        public Builder suspensionStatus(String suspensionStatus) {
            this.suspensionStatus = suspensionStatus;
            return this;
        }
		
		public Builder interaction(Interaction interaction) {
			if (this.interactions == null)
				this.interactions = new ArrayList<Interaction>();
			this.interactions.add(interaction);
			return this;
		}
		
		public Builder interactions(List<Interaction> interactions) {
			this.interactions = interactions;
			return this;
		}

        public Builder section(Section section) {
            if (this.sections == null)
                this.sections = new ArrayList<Section>();
            this.sections.add(section);
            return this;
        }

        public Builder notification(Notification notification) {
            if (this.notifications == null)
                this.notifications = new ArrayList<Notification>();
            this.notifications.add(notification);
            return this;
        }

        public Builder notifications(List<Notification> notifications) {
            this.notifications = notifications;
            return this;
        }

        public Builder defaultScreen(Screen defaultScreen) {
            this.defaultScreen = defaultScreen;
            return this;
        }

        public Builder allowAnonymousSubmission() {
            this.isAnonymousSubmissionAllowed = true;
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

        public Builder clearInteractions() {
            this.interactions = new ArrayList<Interaction>();
            return this;
        }

        public Builder clearNotifications() {
            this.notifications = new ArrayList<Notification>();
            return this;
        }

        public Builder clearSections() {
            this.sections = new ArrayList<Section>();
            return this;
        }
	}
	
	public static class Constants {
		public static final String RESOURCE_LABEL = "Process";
		public static final String ROOT_ELEMENT_NAME = "process";
		public static final String TYPE_NAME = "ProcessType";
	}
}
