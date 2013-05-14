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
package piecework.process.model.view;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import piecework.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ProcessView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ProcessView.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessView implements piecework.process.model.Process {

	private static final long serialVersionUID = 1L;
	
	@XmlAttribute
	@XmlID
	private final String processDefinitionKey;
	
	@XmlElement
	private final String processLabel;
	
	@XmlElement
	private final String processSummary;
	
	@XmlElement
	private final String participantSummary;
	
	@XmlElement
	private final String engine;
	
	@XmlElement
	private final String engineProcessDefinitionKey;
	
	@XmlElement
	private final String uri;
	
	@XmlTransient
	private final String startRequestFormIdentifier;
	
	@XmlTransient
	private final String startResponseFormIdentifier;
	
	@XmlTransient
	private final Map<String, String> taskRequestFormIdentifiers;
	
	@XmlTransient
	private final Map<String, String> taskResponseFormIdentifiers;
	
	private ProcessView() {
		this(new ProcessView.Builder(), new ViewContext());
	}
			
	private ProcessView(ProcessView.Builder builder, ViewContext context) {
		this.processDefinitionKey = builder.getProcessDefinitionKey();
		this.processLabel = builder.getProcessLabel();
		this.processSummary = builder.getProcessSummary();
		this.participantSummary = builder.getParticipantSummary();
		this.engine = builder.getEngine();
		this.engineProcessDefinitionKey = builder.getEngineProcessDefinitionKey();
		this.uri = context != null ? context.getApplicationUri(builder.getId()) : null;
		this.startRequestFormIdentifier = builder.getStartRequestFormIdentifier();
		this.startResponseFormIdentifier = builder.getStartResponseFormIdentifier();
		this.taskRequestFormIdentifiers = builder.getTaskRequestFormIdentifiers();
		this.taskResponseFormIdentifiers = builder.getTaskResponseFormIdentifiers();
	}
	
	@Override
	@JsonIgnore
	public String getId() {
		return null;
	}
		
	@Override
	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	@Override
	public String getEngine() {
		return engine;
	}

	@Override
	public String getEngineProcessDefinitionKey() {
		return engineProcessDefinitionKey;
	}

	public String getUri() {
		return uri;
	}

	public final static class Builder extends piecework.process.model.builder.ProcessBuilder<ProcessView> {
		
		public Builder() {
			super();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public ProcessView.Builder id(String id) {
			super.id(id);
			return this;
		}
		
		public Builder(piecework.process.model.Process process, Sanitizer sanitizer) {
			super(process, sanitizer);
		}
		
		public ProcessView build() {
			return new ProcessView(this, null);
		}
		
		public ProcessView build(ViewContext context) {
			return new ProcessView(this, context);
		}
	}
	
	static class Attributes {
		final static String ID = "id";
	}
	
	public static class Constants {
		public static final String RESOURCE_LABEL = "Process";
		public static final String ROOT_ELEMENT_NAME = "process";
		public static final String TYPE_NAME = "ProcessType";
	}
	
	static class Elements {
		static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
		static final String ENGINE = "engine";
		static final String ENGINE_PROCESS_DEFINITION_KEY = "engineProcessDefinitionKey";
	}

	public String getStartRequestFormIdentifier() {
		return startRequestFormIdentifier;
	}

	public String getStartResponseFormIdentifier() {
		return startResponseFormIdentifier;
	}

	public Map<String, String> getTaskRequestFormIdentifiers() {
		return taskRequestFormIdentifiers;
	}

	public Map<String, String> getTaskResponseFormIdentifiers() {
		return taskResponseFormIdentifiers;
	}

	public String getProcessLabel() {
		return processLabel;
	}

	public String getProcessSummary() {
		return processSummary;
	}

	public String getParticipantSummary() {
		return participantSummary;
	}
	
}
