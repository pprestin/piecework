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
package piecework.ui;

import org.apache.commons.io.IOUtils;
import piecework.model.Attachment;
import piecework.model.Content;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * @author James Renfro
 */
public class StreamingAttachmentContent implements StreamingOutput {

    private final Attachment attachment;
    private final Streamable content;

    public StreamingAttachmentContent(Streamable content) {
        this(null, content);
    }

    public StreamingAttachmentContent(Attachment attachment, Streamable content) {
        this.attachment = attachment;
        this.content = content;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        if (content != null)
            IOUtils.copy(content.getInputStream(), output);
        else if (attachment != null)
            IOUtils.copy(new StringReader(attachment.getDescription()), output);
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public Streamable getContent() {
        return content;
    }
}
