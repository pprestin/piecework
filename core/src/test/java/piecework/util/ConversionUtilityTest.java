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
package piecework.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author James Renfro
 */
public class ConversionUtilityTest {

    @Test
    public void verifyValidBytesConversion() {
        int actual = ConversionUtility.kilobytes("923", 0);
        Assert.assertEquals(923, actual);
    }

    @Test
    public void verifyValidKilobytesConversion() {
        int actual = ConversionUtility.kilobytes("5KB", 0);
        Assert.assertEquals(5, actual);
    }

    @Test
    public void verifyValidMegabytesConversion() {
        int actual = ConversionUtility.kilobytes("251MB", 0);
        Assert.assertEquals(257024, actual);
    }

    @Test
    public void verifyValidGigabytesConversion() {
        int actual = ConversionUtility.kilobytes("14GB", 0);
        Assert.assertEquals(14680064, actual);
    }

    @Test
    public void verifyValidIntegerConversion() {
        int actual = ConversionUtility.integer("12", 0);
        Assert.assertEquals(12, actual);
    }

    @Test
    public void verifyValidNegativeIntegerConversion() {
        int actual = ConversionUtility.integer("-5", 0);
        Assert.assertEquals(-5, actual);
    }

    @Test
    public void verifyInvalidIntegerConversion() {
        int actual = ConversionUtility.integer("asd", 0);
        Assert.assertEquals(0, actual);
    }

}
