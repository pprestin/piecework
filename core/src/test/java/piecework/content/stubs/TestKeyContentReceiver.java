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

import piecework.content.ContentReceiver;
import piecework.model.*;
import piecework.model.Process;

import java.io.IOException;

/**
 * @author James Renfro
 */
public class TestKeyContentReceiver implements ContentReceiver {

    @Override
    public Content save(Process process, ProcessInstance instance, Content content, Entity principal) throws IOException {
        return new Content.Builder()
                .location("some-key-content-receiver")
                .build();
    }

    @Override
    public String getKey() {
        return "some-key";
    }

}
