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

import piecework.content.ContentResource;
import piecework.enumeration.DataInjectionStrategy;
import piecework.model.Form;
import piecework.model.Process;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author James Renfro
 */
@Deprecated
public class StreamingPageContent implements StreamingOutput {

    private final Process process;
    private final Form form;
    private final ContentResource contentResource;
    private final DataInjectionStrategy strategy;

    public StreamingPageContent(Process process, Form form, ContentResource contentResource, DataInjectionStrategy strategy) {
        this.process = process;
        this.form = form;
        this.contentResource = contentResource;
        this.strategy = strategy;
    }

    public void write(OutputStream output) throws IOException, WebApplicationException {
//        CleanerProperties cleanerProperties = new CleanerProperties();
//        cleanerProperties.setOmitXmlDeclaration(true);
//        HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
//        TagNode node = cleaner.clean(content.getInputStream());
//        switch (strategy) {
//            case INCLUDE_SCRIPT:
//                node.traverse(new ScriptInjectingVisitor(form));
//                break;
//            case DECORATE_HTML:
//                node.traverse(new DecoratingVisitor(null, process, form, null, null));
//                break;
//        }
//
//        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
//        serializer.writeToStream(node, output);
    }
}
