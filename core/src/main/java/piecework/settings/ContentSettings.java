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
package piecework.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author James Renfro
 */
@Service
public class ContentSettings {

    @Autowired
    Environment environment;

    private String applicationFilesystemRoot;
    private String baseFilesystemRoot;

    @PostConstruct
    public void init() {
        // This must be a directory below the base file system root
        this.applicationFilesystemRoot = environment.getProperty("application.filesystem.root");
        // This is the root directory below which all process-specific and application content must
        // be located
        this.baseFilesystemRoot = environment.getProperty("base.filesystem.root");
    }

    public String getApplicationFilesystemRoot() {
        return applicationFilesystemRoot;
    }

    public String getBaseFilesystemRoot() {
        return baseFilesystemRoot;
    }
}
