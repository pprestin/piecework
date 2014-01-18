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

import java.io.File;
import java.io.IOException;

/**
 * @author James Renfro
 */
public class FileUtilityTest {

    @Test
    public void happyPathSuccess() throws IOException {
        File a = new File("/a/file/system/path");
        File b = new File("/a/file/system/path/ok");

        Assert.assertTrue(FileUtility.isAncestorOf(a, b));
    }

    @Test
    public void upDownPathSuccess() throws IOException {
        File a = new File("/a/file/system/path");
        File b = new File("/a/file/system/path/ok/../yes");

        Assert.assertTrue(FileUtility.isAncestorOf(a, b));
    }

    @Test
    public void upDownAndBackPathSuccess() throws IOException {
        File a = new File("/a/file/system/path");
        File b = new File("/a/file/system/path/ok/../../../../file/system/path/blah");

        Assert.assertTrue(FileUtility.isAncestorOf(a, b));
    }

    @Test
    public void happyPathFailure() throws IOException {
        File a = new File("/a/file/system/path");
        File b = new File("/a/file/system");

        Assert.assertFalse(FileUtility.isAncestorOf(a, b));
    }

    @Test
    public void crazyPathFailure() throws IOException {
        File a = new File("/a/file/system/path");
        File b = new File("http://what");

        Assert.assertFalse(FileUtility.isAncestorOf(a, b));
    }

    @Test
    public void upDownPathFailure() throws IOException {
        File a = new File("/a/file/system/path");
        File b = new File("/a/file/system/path/../nope");

        Assert.assertFalse(FileUtility.isAncestorOf(a, b));
    }

}
