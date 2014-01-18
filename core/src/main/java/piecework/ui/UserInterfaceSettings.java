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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author James Renfro
 */
@Service
public class UserInterfaceSettings {

    @Autowired
    Environment environment;

    private String applicationTitle;
    private String applicationUrl;
    private String publicUrl;
    private String assetsUrl;
    private String assetsDirectoryPath;
    private boolean doOptimization;
    private boolean disableResourceCaching;
    private String customStylesheetUrl;

    @PostConstruct
    public void init() {
        this.applicationTitle = environment.getProperty("application.name");
        this.applicationUrl = environment.getProperty("base.application.uri");
        this.publicUrl = environment.getProperty("base.public.uri");
        this.assetsUrl = environment.getProperty("ui.static.urlbase");
        this.disableResourceCaching = environment.getProperty("disable.resource.caching", Boolean.class, Boolean.FALSE);
        this.assetsDirectoryPath = environment.getProperty("assets.directory");
        this.doOptimization = environment.getProperty("javascript.minification", Boolean.class, Boolean.FALSE);
        this.customStylesheetUrl = environment.getProperty("custom.stylesheet.url");
    }

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getAssetsUrl() {
        return assetsUrl;
    }

    public String getAssetsDirectoryPath() {
        return assetsDirectoryPath;
    }

    public String getCustomStylesheetUrl() {
        return customStylesheetUrl;
    }

    public boolean isDoOptimization() {
        return doOptimization;
    }

    public boolean isDisableResourceCaching() {
        return disableResourceCaching;
    }
}
