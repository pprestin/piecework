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

import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

/**
 * @author James Renfro
 */
public class Base64Utility {

    private static final Logger LOG = Logger.getLogger(Base64Utility.class);

    public static String safeBase64(String link) {
        try {
            String original = DatatypeConverter.printBase64Binary(MessageDigest.getInstance("SHA1").digest(link.getBytes()));
            String safe = original.replaceAll("\\+", "-");
            safe = safe.replaceAll("\\/", "_");
            return safe.replaceAll("=", ",");
        } catch (Exception e) {
            LOG.error("Unable to digest link for content type of url", e);
        }
        return null;
    }

}
