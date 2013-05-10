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
package piecework.process.model.builder;

import piecework.Sanitizer;
import piecework.common.view.ViewContext;
import piecework.form.model.builder.Builder;
import piecework.process.model.Interaction;

/**
 * @author James Renfro
 */
public abstract class InteractionBuilder<I extends Interaction> extends Builder {

	private String label;
//	private List<ScreenBuilder<?>> screenBuilders;
	
	public InteractionBuilder() {
		super();
	}
	
	public InteractionBuilder(Interaction interaction, Sanitizer sanitizer) {
		super(interaction.getId());
		this.label = sanitizer.sanitize(interaction.getLabel());
	}
	
	public abstract I build();
	
	public abstract I build(ViewContext context);
	
	public InteractionBuilder<I> label(String label) {
		this.label = label;
		return this;
	}

	public String getLabel() {
		return label;
	}
	
//	public List<Screen> getScreens() {
//		List<Screen> screens = new ArrayList<Screen>();
//		
//		
//	}
	
}
