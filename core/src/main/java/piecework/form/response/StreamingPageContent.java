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
package piecework.form.response;

import org.htmlcleaner.*;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import piecework.form.HiddenInputNode;
import piecework.model.Content;
import piecework.model.Form;
import piecework.model.Screen;
import piecework.util.ManyMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
public class StreamingPageContent implements StreamingOutput {

    private final Form form;
    private final Content content;

    public StreamingPageContent(Form form, Content content) {
        this.form = form;
        this.content = content;
    }

    public void write(OutputStream output) throws IOException, WebApplicationException {
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode node = cleaner.clean(content.getInputStream());
        node.traverse(new DecoratingVisitor(form));
        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
        serializer.writeToStream(node, output);
    }
}
