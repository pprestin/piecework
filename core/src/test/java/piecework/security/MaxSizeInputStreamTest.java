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
package piecework.security;

import junit.framework.Assert;
import org.junit.Test;
import piecework.exception.MaxSizeExceededException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author James Renfro
 */
public class MaxSizeInputStreamTest {

    @Test(expected = MaxSizeExceededException.class)
    public void verifyExceeds() throws Exception {
        InputStream input = new MaxSizeInputStream(new ByteArrayInputStream("This is a test".getBytes()), 2l);
        input.read();
    }

    @Test
    public void verifyDoesNotExceed() throws Exception {
        InputStream input = new MaxSizeInputStream(new ByteArrayInputStream("This is a test".getBytes()), 200l);
        input.read();
    }

}
