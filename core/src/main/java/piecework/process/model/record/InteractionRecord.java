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
package piecework.process.model.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.Sanitizer;
import piecework.common.view.ViewContext;
import piecework.process.model.Interaction;
import piecework.process.model.builder.InteractionBuilder;

/**
 * @author James Renfro
 */
@Document(collection = "interaction")
public class InteractionRecord implements Interaction {
	
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	private String label;
	
	private InteractionRecord() {
		
	}
	
	private InteractionRecord(InteractionRecord.Builder builder) {
		this.id = builder.getId();
		this.label = builder.getLabel();
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public final static class Builder extends InteractionBuilder<InteractionRecord> {
		
		public Builder() {
			super();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public InteractionRecord.Builder id(String id) {
			super.id(id);
			return this;
		}
		
		public Builder(Interaction interaction, Sanitizer sanitizer) {
			super(interaction, sanitizer);
		}
		
		public InteractionRecord build() {
			return new InteractionRecord(this);
		}
		
		public InteractionRecord build(ViewContext context) {
			return new InteractionRecord(this);
		}
		
	}
	
}
