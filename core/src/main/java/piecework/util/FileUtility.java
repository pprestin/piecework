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

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author James Renfro
 */
public final class FileUtility {

    public static final boolean isAncestorOf(File parent, File child) throws IOException {
        File canonicalParent = parent.getCanonicalFile();
        File canonicalChild = child.getCanonicalFile();

        File p = canonicalChild.getParentFile();
        while (p != null) {
            if (canonicalParent.equals(p))
                return true;
            p = p.getParentFile();
        }
        return false;
    }

    public static String resolveFilenameFromPath(String path) {
        String filename = path != null ? path : "";
        if (StringUtils.isNotEmpty(filename)) {
            int lastIndexOf = filename.lastIndexOf('/');
            if (lastIndexOf != -1) {
                if ((lastIndexOf+1) < filename.length())
                    filename = filename.substring(lastIndexOf + 1);
                else
                    filename = "";
            }

        }
        return filename;
    }

}
