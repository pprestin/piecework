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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import piecework.form.model.Section;
import piecework.form.model.builder.Builder;
import piecework.form.model.builder.FormBuilder;
import piecework.form.model.builder.SectionBuilder;
import piecework.process.model.Screen;
import piecework.util.LayoutUtil;

/**
 * @author James Renfro
 */
public abstract class ScreenBuilder<S extends Screen> extends Builder {

	private String title;
	private String type;
	private String location;
	private List<SectionBuilder<?>> sections;
	
		
	
}
