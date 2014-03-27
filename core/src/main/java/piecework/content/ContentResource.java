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
package piecework.content;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Abstraction on GridFSFile or GridFSResource -- not a document, but to make GridFS resources
 * look like other model objects for the purpose of keeping the code readable.
 *
 * @author James Renfro
 */
public interface ContentResource extends StreamingOutput, Serializable {

    String getContentId();

    String contentType();

    String getLocation();

    String getName();

    String getFilename();

    String getDescription();

    InputStream getInputStream() throws IOException;

    long contentLength();

    long lastModified();

    String lastModifiedBy();

    String eTag();

    List<Version> versions();

    Map<String, String> getMetadata();

    boolean publish();

}
