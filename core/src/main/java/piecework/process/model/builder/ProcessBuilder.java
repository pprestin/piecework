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

import piecework.common.view.ViewContext;
import piecework.form.model.builder.Builder;

/**
 * @author James Renfro
 */
public abstract class ProcessBuilder<P extends piecework.process.model.Process> extends Builder {

	private String processDefinitionKey;
	private String engine;
	private String engineProcessDefinitionKey;
	
	public ProcessBuilder() {
		super();
	}
	
	public ProcessBuilder(piecework.process.model.Process process) {
		super(process.getId());
		this.processDefinitionKey = process.getProcessDefinitionKey();
		this.engine = process.getEngine();
		this.engineProcessDefinitionKey = process.getEngineProcessDefinitionKey();
	}
	
	public abstract P build();
	
	public abstract P build(ViewContext context);
	
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

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getEngine() {
		return engine;
	}

	public String getEngineProcessDefinitionKey() {
		return engineProcessDefinitionKey;
	}
}
