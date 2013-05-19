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
package piecework.form;

import org.htmlcleaner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import piecework.model.Form;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class PageRepository {

    @Autowired
    GridFsTemplate gridFsTemplate;

    public Response getPageResponse(Form form, String location) {
        GridFsResource resource = gridFsTemplate.getResource(location);
        String contentType = resource.getContentType();
        return Response.ok(new StreamingPageContent(form, resource), contentType).build();
    }

    public class StreamingPageContent implements StreamingOutput {

        private final Form form;
        private final GridFsResource resource;

        public StreamingPageContent(Form form, GridFsResource resource) {
            this.form = form;
            this.resource = resource;
        }

        public void write(OutputStream output) throws IOException, WebApplicationException {
            HtmlCleaner cleaner = new HtmlCleaner();
            TagNode node = cleaner.clean(resource.getInputStream());

            final URL siteUrl = null;
            node.traverse(new TagNodeVisitor() {
                public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
                    if (htmlNode instanceof TagNode) {
                        TagNode tag = (TagNode) htmlNode;
                        String tagName = tag.getName();
                        if ("form".equals(tagName)) {
                            String id = tag.getAttributeByName("id");

                            if (id == null || id.equalsIgnoreCase("main-form")) {
                                Map<String, String> attributes = new HashMap<String, String>();
                                attributes.put("action", form.getUri());
                                attributes.put("method", "POST");
                                attributes.put("enctype", "multipart/form-data");
                                tag.setAttributes(attributes);
                            }

                            tag.addChild(new HiddenInputNode("PROCESS_FORM_SUBMISSION_TOKEN", form.getRequestId()));
                        }
                    }
                    // tells visitor to continue traversing the DOM tree
                    return true;
                }
            });

            SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
            serializer.writeToStream(node, output);
        }
    }

}
