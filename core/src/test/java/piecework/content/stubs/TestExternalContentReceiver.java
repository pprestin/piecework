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
package piecework.content.stubs;

import org.apache.commons.lang.NotImplementedException;
import piecework.content.ContentReceiver;
import piecework.model.Content;
import piecework.model.Entity;

import java.io.IOException;

/**
 * @author James Renfro
 */
public class TestExternalContentReceiver implements ContentReceiver {

    @Override
    public Content save(Content content, Entity principal) throws IOException {
        return new Content.Builder()
                .location("some-external-content-receiver")
                .build();
    }

    @Override
    public String getKey() {
        return null;
    }

}
