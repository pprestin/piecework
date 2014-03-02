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
package piecework.content.concrete;

import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;

/**
 * @author James Renfro
 */
public class DatedByteArrayContentResource extends AbstractContentResource<ByteArrayResource> {

    private long lastModified;

    public DatedByteArrayContentResource(ByteArrayResource resource) {
        super(null, resource);
        this.lastModified = System.currentTimeMillis();
    }

    public long lastModified() {
        return lastModified;
    }

    @Override
    protected String getPrefix() {
        return null;
    }

    @Override
    public String contentType() {
        return null;
    }

    @Override
    public String getLocation() {
        return null;
    }

}
