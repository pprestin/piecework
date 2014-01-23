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
import piecework.model.Action;

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
    private final Action action;

    public FormDisposition(FormDispositionType type, URI uri, String base, String path, DataInjectionStrategy strategy, Action action) {
        this.type = type;
        this.uri = uri;
        this.base = base;
        this.path = path;
        this.strategy = strategy;
        this.action = action;
    }

    public FormDisposition(Action action) {
        this(FormDispositionType.DEFAULT, null, null, null, DataInjectionStrategy.NONE, action);
    }

    public FormDisposition(URI uri, DataInjectionStrategy strategy, Action action) {
        this(FormDispositionType.REMOTE, uri, null, null, strategy, action);
    }

    public FormDisposition(String base, String path, DataInjectionStrategy strategy, Action action) {
        this(FormDispositionType.CUSTOM, null, base, path, strategy, action);
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

    public Action getAction() {
        return action;
    }
}
