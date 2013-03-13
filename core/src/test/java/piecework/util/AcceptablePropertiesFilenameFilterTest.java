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
package piecework.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;
import org.springframework.core.env.Environment;

/**
 * @author James Renfro
 */
public class AcceptablePropertiesFilenameFilterTest {

	@Test
	public void test() {
		Environment environment = mock(Environment.class);
		when(environment.acceptsProfiles("dev")).thenReturn(true);
		when(environment.acceptsProfiles("eval")).thenReturn(false);
		when(environment.acceptsProfiles("prod")).thenReturn(false);
		
		FilenameFilter filter = new AcceptablePropertiesFilenameFilter(environment);
		
		assertTrue(filter.accept(new File("somedirectory"), "cluster-dev.properties"));
		assertTrue(filter.accept(new File("somedirectory"), "instance-1-dev.properties"));
		assertTrue(filter.accept(new File("somedirectory"), "ldap.properties"));
		assertFalse(filter.accept(new File("somedirectory"), ".properties"));
		assertFalse(filter.accept(new File("somedirectory"), "-.properties"));
		assertFalse(filter.accept(new File("somedirectory"), "cluster-eval.properties"));
		assertFalse(filter.accept(new File("somedirectory"), "instance-1-eval.properties"));
		assertFalse(filter.accept(new File("somedirectory"), "cluster-prod.properties"));
		assertFalse(filter.accept(new File("somedirectory"), "instance-1-prod.properties"));
	}

}
