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
package piecework.process.concrete;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import piecework.process.ProcessEngineRuntimeFacade;
import piecework.process.model.ProcessInstance;

/**
 * @author James Renfro
 */
@Service
public class ProcessEngineRuntimeConcreteFacade implements ProcessEngineRuntimeFacade {

	@Override
	public ProcessInstance start(final String processDefinitionKey, final String processBusinessKey, final Map<String, ?> data) {
		final String processInstanceId = UUID.randomUUID().toString();
		// FIXME: Stubbed out, fill in later
		return new ProcessInstance() {

			@Override
			public String getProcessDefinitionKey() {
				return processDefinitionKey;
			}

			@Override
			public String getProcessInstanceId() {
				return processInstanceId;
			}
		};
	}

}
