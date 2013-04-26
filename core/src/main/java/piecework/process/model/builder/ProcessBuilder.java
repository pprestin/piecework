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
package piecework.process.model.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import piecework.common.view.ViewContext;
import piecework.form.model.builder.Builder;

/**
 * @author James Renfro
 */
public abstract class ProcessBuilder<P extends piecework.process.model.Process> extends Builder {

	private String processLabel;
	private String processSummary;
	private String processDefinitionKey;
	private String engine;
	private String engineProcessDefinitionKey;
	private String startRequestFormIdentifier;
	private String startResponseFormIdentifier;
	private Map<String, String> taskRequestFormIdentifiers;
	private Map<String, String> taskResponseFormIdentifiers;
	
	public ProcessBuilder() {
		super();
	}
	
	public ProcessBuilder(piecework.process.model.Process process) {
		super(process.getId());
		this.processLabel = process.getProcessLabel();
		this.processDefinitionKey = process.getProcessDefinitionKey();
		this.engine = process.getEngine();
		this.engineProcessDefinitionKey = process.getEngineProcessDefinitionKey();
	}
	
	public abstract P build();
	
	public abstract P build(ViewContext context);
	
	public ProcessBuilder<P> processLabel(String processLabel) {
		this.processLabel = processLabel;
		return this;
	}
	
	public ProcessBuilder<P> processSummary(String processSummary) {
		this.processSummary = processSummary;
		return this;
	}
	
	public ProcessBuilder<P> processDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
		return this;
	}
	
	public ProcessBuilder<P> engine(String engine) {
		this.engine = engine;
		return this;
	}
	
	public ProcessBuilder<P> engineProcessDefinitionKey(String engineProcessDefinitionKey) {
		this.engineProcessDefinitionKey = engineProcessDefinitionKey;
		return this;
	}
	
	public ProcessBuilder<P> startRequestFormIdentifier(String startRequestFormIdentifier) {
		this.startRequestFormIdentifier = startRequestFormIdentifier;
		return this;
	}
	
	public ProcessBuilder<P> startResponseFormIdentifier(String startResponseFormIdentifier) {
		this.startResponseFormIdentifier = startResponseFormIdentifier;
		return this;
	}
	
	public ProcessBuilder<P> taskRequestFormIdentifier(String taskDefinitionKey, String formIdentifier) {
		if (this.taskRequestFormIdentifiers == null)
			this.taskRequestFormIdentifiers = new HashMap<String, String>();
		this.taskRequestFormIdentifiers.put(taskDefinitionKey, formIdentifier);
		return this;
	}
	
	public ProcessBuilder<P> taskResponseFormIdentifier(String taskDefinitionKey, String formIdentifier) {
		if (this.taskResponseFormIdentifiers == null)
			this.taskResponseFormIdentifiers = new HashMap<String, String>();
		this.taskResponseFormIdentifiers.put(taskDefinitionKey, formIdentifier);
		return this;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getEngine() {
		return engine;
	}

	public String getEngineProcessDefinitionKey() {
		return engineProcessDefinitionKey;
	}

	public String getStartRequestFormIdentifier() {
		return startRequestFormIdentifier;
	}

	public String getStartResponseFormIdentifier() {
		return startResponseFormIdentifier;
	}

	public Map<String, String> getTaskRequestFormIdentifiers() {
		if (taskRequestFormIdentifiers != null && !taskRequestFormIdentifiers.isEmpty())
			return Collections.unmodifiableMap(taskRequestFormIdentifiers);
		return null;
	}

	public Map<String, String> getTaskResponseFormIdentifiers() {
		if (taskResponseFormIdentifiers != null && !taskResponseFormIdentifiers.isEmpty())
			return Collections.unmodifiableMap(taskResponseFormIdentifiers);
		return null;
	}

	public String getProcessLabel() {
		return processLabel;
	}

	public String getProcessSummary() {
		return processSummary;
	}
}
