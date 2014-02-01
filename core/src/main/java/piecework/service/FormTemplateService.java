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
package piecework.service;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.designer.model.view.IndexView;
import piecework.exception.NotFoundError;
import piecework.model.Explanation;
import piecework.model.Form;
import piecework.model.Report;
import piecework.model.SearchResults;
import piecework.util.UserInterfaceUtility;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class FormTemplateService {

    private static final Logger LOG = Logger.getLogger(FormTemplateService.class);

    @Autowired
    Environment environment;

    private File scriptsDirectory;
    private File templatesDirectory;

    @PostConstruct
    public void init() {
        String scriptsDirectoryPath = environment.getProperty("scripts.directory");
        String templatesDirectoryPath = environment.getProperty("templates.directory");

        this.scriptsDirectory = directory(scriptsDirectoryPath);
        this.templatesDirectory = directory(templatesDirectoryPath);
    }

//    public Resource getExternalScriptResource(Class<?> type, Object t) {
//        return UserInterfaceUtility.externalScriptResource(type, t, scriptsDirectory);
//    }

    public Resource getTemplateResource(Class<?> type, Object t) throws NotFoundError {
        String templateName = UserInterfaceUtility.templateName(type, t);
        return getTemplateResource(templateName);
    }

    public Resource getTemplateResource(String templateName) throws NotFoundError {
        return UserInterfaceUtility.template(templatesDirectory, templateName);
    }

    private static File directory(String path) {
        if (StringUtils.isNotEmpty(path)) {
            File directory = new File(path);
            if (directory.exists() && directory.isDirectory())
                return directory;
        }
        return null;
    }

}
