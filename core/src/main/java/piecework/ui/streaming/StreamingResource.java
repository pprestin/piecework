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
package piecework.ui.streaming;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import piecework.model.Attachment;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

/**
 * @author James Renfro
 */
public class StreamingResource implements StreamingOutput {

    private final Attachment attachment;
    private final Resource content;

    public StreamingResource(Resource content) {
        this(null, content);
    }

    public StreamingResource(Attachment attachment, Resource content) {
        this.attachment = attachment;
        this.content = content;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        InputStream input = null;
        Reader reader = null;
        try {
            if (content != null) {
                input = content.getInputStream();
                IOUtils.copy(input, output);
            } else if (attachment != null) {
                reader = new StringReader(attachment.getDescription());
                IOUtils.copy(reader, output);
            }
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public Resource getContent() {
        return content;
    }
}
