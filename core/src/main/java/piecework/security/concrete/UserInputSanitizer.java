/*
 * Copyright 2010 University of Washington
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
package piecework.security.concrete;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.security.Sanitizer;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class UserInputSanitizer implements Sanitizer {

	private static final Logger LOG = org.apache.log4j.Logger.getLogger(UserInputSanitizer.class);
	
	@Autowired
	Policy antisamyPolicy;
		
	/**
	 * Ensure that the passed string is stripped of any potential scripting
	 */
	public String sanitize(String tainted) {
		AntiSamy as = new AntiSamy();
		
		if (tainted != null) {
			try {
				CleanResults cr = as.scan(tainted, antisamyPolicy);
				String clean = cr.getCleanHTML();
				logErrors(cr);
				if (clean != null) {
					return StringEscapeUtils.unescapeXml(clean);
				}
			} catch (ScanException se) {
				LOG.error("Caught a scan exception", se);
			} catch (PolicyException pe) {
				LOG.error("Caught a scan exception", pe);
			}
		}
		
		return null;
	}

	/**
	 * Ensure that the passed map messages and keys are stripped of any potential scripting
	 */
	public Map<String, List<String>> sanitize(final Map<String, List<String>> tainted) {
		if (tainted == null)
			return null;
		
		ManyMap<String, String> clean = new ManyMap<String, String>();
		
		AntiSamy as = new AntiSamy();
		
		for (Entry<String, List<String>> taintedEntry : tainted.entrySet()) {
			try {
				CleanResults cr = as.scan(taintedEntry.getKey(), antisamyPolicy);
				String key = cr.getCleanHTML();
				logErrors(cr);
				List<String> values = new LinkedList<String>();
				List<String> taintedValues = taintedEntry.getValue();
				if (taintedValues != null) {
					for (String taintedValue : taintedValues) {
						cr = as.scan(taintedValue, antisamyPolicy);
						logErrors(cr);
						String value = cr.getCleanHTML();
						if (value != null) {
							String unescapedValue = StringEscapeUtils.unescapeXml(value);
							values.add(unescapedValue);
						}
					}
				}
				clean.put(key, values);
			} catch (ScanException se) {
				LOG.error("Caught a scan exception", se);
			} catch (PolicyException pe) {
				LOG.error("Caught a scan exception", pe);
			}
		}

		return clean;
	}
	
	private void logErrors(CleanResults cr) {
		if (cr.getNumberOfErrors() > 0) {
			StringBuilder builder = new StringBuilder();
			builder.append("Received suspicious user input:\n");			
		
			@SuppressWarnings("unchecked")
			List<String> errorMessages = cr.getErrorMessages();
			if (errorMessages != null) {
				for (String errorMessage : errorMessages) {
					builder.append("\t").append(errorMessage).append("\n");
				}
			}
			LOG.warn(builder.toString());
		}
	}

    public void setAntisamyPolicy(Policy antisamyPolicy) {
        this.antisamyPolicy = antisamyPolicy;
    }
}
