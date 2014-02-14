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
package piecework.security.data;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.owasp.validator.html.Policy;
import org.springframework.core.io.ClassPathResource;
import piecework.security.data.UserInputSanitizer;

import java.net.URL;

/**
 * @author James Renfro
 */
public class UserInputSanitizerTest {

    private UserInputSanitizer userInputSanitizer;

    @Before
    public void setUp() throws Exception {
        ClassPathResource policyResource = new ClassPathResource("META-INF/piecework/antisamy-piecework-1.4.4.xml");
        URL policyUrl = policyResource.getURL();

        userInputSanitizer = new UserInputSanitizer();
        userInputSanitizer.setAntisamyPolicy(Policy.getInstance(policyUrl));
    }

    @Test
    public void testSanitizeEmailAddress() throws Exception {
        String text = "joe@nowhere.com";
        String sanitized = userInputSanitizer.sanitize(text);
        Assert.assertEquals(text, sanitized);
    }

    @Test
    public void testSanitizeEmailAddressWithName() throws Exception {
        String text = "Joe <\"joe@nowhere.com\">";
        String sanitized = userInputSanitizer.sanitize(text);
        Assert.assertEquals(text, sanitized);
    }

    @Test
    public void testSanitizeBasicHtml() throws Exception {
        String text = "<div>\n" +
                "     To view terms and conditions, please visit <a href=\"http://google.com\">http://google.com</a>\n" +
                "     </div>\n\n" +
                "     <div class=\"noReview\" id=\"termsAndConditionsText\"> \n" +
                "      <strong>No special status:</strong>\n" +
                "      <p>Submission of this form does not confer any &ldquo;special&rdquo; status on an individual/company.\n" +
                "      </p>   \n" +
                "     </div>";
        String sanitized = userInputSanitizer.sanitize(text);
        Assert.assertEquals(text, sanitized);
    }

}
