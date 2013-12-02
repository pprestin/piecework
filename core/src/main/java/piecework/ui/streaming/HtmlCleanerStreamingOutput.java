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

import org.htmlcleaner.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author James Renfro
 */
public class HtmlCleanerStreamingOutput implements StreamingOutput {

    private final InputStream inputStream;
    private final TagNodeVisitor visitor;

    public HtmlCleanerStreamingOutput(InputStream inputStream, TagNodeVisitor visitor) {
        this.inputStream = inputStream;
        this.visitor = visitor;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        CleanerProperties cleanerProperties = new CleanerProperties();
        cleanerProperties.setOmitXmlDeclaration(true);
        HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
        TagNode node = cleaner.clean(inputStream);
        node.traverse(visitor);
        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
        serializer.writeToStream(node, output);
    }
}
