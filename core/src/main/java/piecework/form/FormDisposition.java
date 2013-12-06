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

import piecework.enumeration.DataInjectionStrategy;

import java.net.URI;

/**
 * @author James Renfro
 */
public class FormDisposition {

    public enum FormDispositionType { DEFAULT, CUSTOM, REMOTE };

    private final FormDispositionType type;
    private final URI uri;
    private final String base;
    private final String path;
    private final DataInjectionStrategy strategy;

    public FormDisposition(FormDispositionType type, URI uri, String base, String path, DataInjectionStrategy strategy) {
        this.type = type;
        this.uri = uri;
        this.base = base;
        this.path = path;
        this.strategy = strategy;
    }

    public FormDisposition() {
        this(FormDispositionType.DEFAULT, null, null, null, DataInjectionStrategy.NONE);
    }

    public FormDisposition(URI uri) {
        this(FormDispositionType.REMOTE, uri, null, null, DataInjectionStrategy.INCLUDE_SCRIPT);
    }

    public FormDisposition(String base, String path, DataInjectionStrategy strategy) {
        this(FormDispositionType.CUSTOM, null, base, path, strategy);
    }

    public FormDispositionType getType() {
        return type;
    }

    public URI getUri() {
        return uri;
    }

    public String getBase() {
        return base;
    }

    public String getPath() {
        return path;
    }

    public DataInjectionStrategy getStrategy() {
        return strategy;
    }
}
