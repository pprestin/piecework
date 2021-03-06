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

import piecework.content.ContentProvider;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.enumeration.Scheme;
import piecework.exception.PieceworkException;
import piecework.persistence.ContentProfileProvider;

/**
 * @author James Renfro
 */
public class TestKeyContentProvider implements ContentProvider {

    @Override
    public ContentResource findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException {
        return new BasicContentResource.Builder()
                .location("some-key-content-provider")
                .build();
    }

    @Override
    public Scheme getScheme() {
        return Scheme.REPOSITORY;
    }

    @Override
    public String getKey() {
        return "some-key";
    }

}
