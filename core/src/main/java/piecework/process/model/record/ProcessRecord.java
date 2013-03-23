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
package piecework.process.model.record;

import piecework.common.view.ViewContext;


/**
 * @author James Renfro
 */
public class ProcessRecord implements piecework.process.model.Process {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String processDefinitionKey;
	private String engine;
	private String engineProcessDefinitionKey;
	
	private ProcessRecord(ProcessRecord.Builder builder) {
		this.id = builder.getId();
		this.processDefinitionKey = builder.getProcessDefinitionKey();
		this.engine = builder.getEngine();
		this.engineProcessDefinitionKey = builder.getEngineProcessDefinitionKey();
	}
	
	@Override
	public String getId() {
		return id;
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

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public void setEngineProcessDefinitionKey(String engineProcessDefinitionKey) {
		this.engineProcessDefinitionKey = engineProcessDefinitionKey;
	}

	
	public final static class Builder extends piecework.process.model.builder.ProcessBuilder<ProcessRecord> {
		
		public Builder() {
			super();
		}
		
		public Builder(piecework.process.model.Process process) {
			super(process);
		}
		
		public ProcessRecord build() {
			return new ProcessRecord(this);
		}

		@Override
		public ProcessRecord build(ViewContext context) {
			return new ProcessRecord(this);
		}
	}

}
