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
package piecework.decorator;

import piecework.model.Container;
import piecework.model.Field;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.Map;

/**
 * @author James Renfro
 */
public class ContainerDecorator {

    private final Sanitizer sanitizer;
    private final Container container;
    private final Map<String, Field> fieldMap;

    public ContainerDecorator(Container container, Map<String, Field> fieldMap) {
        this.container = container;
        this.sanitizer = new PassthroughSanitizer();
        this.fieldMap = fieldMap;
    }

    public Container decorate() {
        return decorate(container);
    }

    private Container decorate(Container container) {
        Container.Builder builder = new Container.Builder(container, sanitizer, fieldMap);
        return builder.build();
    }

}
