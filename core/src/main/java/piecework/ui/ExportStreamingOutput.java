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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.persistence.IteratingDataProvider;
import piecework.persistence.concrete.ExportInstanceProvider;

/**
 * @author James Renfro
 */
public class ExportStreamingOutput implements StreamingOutput {

    private static final Logger LOG = Logger.getLogger(ExportStreamingOutput.class);

    private final ExportInstanceProvider provider;

    public ExportStreamingOutput(ExportInstanceProvider provider) {
        this.provider = provider;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(output, true);
            writer.println(provider.getHeader());
            while (provider.hasNext()) {
                List<String> rows = provider.next();
                for (String row : rows) {
                    writer.print(row);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new WebApplicationException(e);
        } finally {
            if (writer != null)
                writer.close();
        }

    }

}
