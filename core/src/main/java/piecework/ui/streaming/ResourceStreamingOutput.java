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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author James Renfro
 */
public class ResourceStreamingOutput implements StreamingOutput {

    protected final Resource resource;

    public ResourceStreamingOutput(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        IOUtils.copy(resource.getInputStream(), output);
    }

}
