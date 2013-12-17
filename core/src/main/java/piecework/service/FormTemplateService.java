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
import piecework.model.SearchResults;

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

    private static final Set<Class<?>> ACCEPTABLE_TEMPLATE_CLASSES =
            Sets.newHashSet(Explanation.class, Form.class, IndexView.class, SearchResults.class);
    private static final Map<String, Class<?>> ACCEPTABLE_TEMPLATE_NAME_MAP;

    static {
        ACCEPTABLE_TEMPLATE_NAME_MAP = new HashMap<String, Class<?>>();
        for (Class<?> cls : ACCEPTABLE_TEMPLATE_CLASSES)
            ACCEPTABLE_TEMPLATE_NAME_MAP.put(cls.getSimpleName(), cls);

        ACCEPTABLE_TEMPLATE_NAME_MAP.put("SearchResults.form", SearchResults.class);
    }

    private static final Logger LOG = Logger.getLogger(FormTemplateService.class);

    @Autowired
    Environment environment;

    private final static String SCRIPTS_CLASSPATH_PREFIX = "META-INF/piecework/scripts/";
    private final static String TEMPLATES_CLASSPATH_PREFIX = "META-INF/piecework/templates/";

    private File scriptsDirectory;
    private File templatesDirectory;


    @PostConstruct
    public void init() {
        String scriptsDirectoryPath = environment.getProperty("scripts.directory");
        String templatesDirectoryPath = environment.getProperty("templates.directory");

        this.scriptsDirectory = directory(scriptsDirectoryPath);
        this.templatesDirectory = directory(templatesDirectoryPath);
    }

    public Resource getExternalScriptResource(Class<?> type, Object t) {
        StringBuilder templateNameBuilder = new StringBuilder(type.getSimpleName());

        if (type.equals(SearchResults.class)) {
            SearchResults results = SearchResults.class.cast(t);
            templateNameBuilder.append(".").append(results.getResourceName());
        }

        templateNameBuilder.append(".js");

        String templateName = templateNameBuilder.toString();
        Resource resource = null;
        if (scriptsDirectory != null) {
            resource = new FileSystemResource(new File(scriptsDirectory, templateName));

            if (!resource.exists())
                resource = new FileSystemResource(new File(scriptsDirectory, "script.js"));

        } else {
            resource = new ClassPathResource(SCRIPTS_CLASSPATH_PREFIX + templateName);

            if (!resource.exists())
                resource = new ClassPathResource(SCRIPTS_CLASSPATH_PREFIX + "script.js");
        }

        return resource;
    }

    public String getTemplateName(String id, boolean anonymous) throws NotFoundError {
        if (StringUtils.isNotEmpty(id)) {
            Class<?> type = ACCEPTABLE_TEMPLATE_NAME_MAP.get(id);
            if (type != null) {
                StringBuilder templateNameBuilder = new StringBuilder(id);
                if (anonymous)
                    templateNameBuilder.append(".anonymous");
                templateNameBuilder.append(".template.html");
                return templateNameBuilder.toString();
            }
        }
        return null;
    }

    public Resource getTemplateResource(Class<?> type, Object t) throws NotFoundError {
        StringBuilder templateNameBuilder = new StringBuilder(type.getSimpleName());

        if (t != null) {
            if (type.equals(SearchResults.class)) {
                SearchResults results = SearchResults.class.cast(t);
                templateNameBuilder.append(".").append(results.getResourceName());
            } else if (type.equals(Form.class)) {
                Form form = Form.class.cast(t);
                if (form.isAnonymous())
                    templateNameBuilder.append(".anonymous");
            }
        }

        templateNameBuilder.append(".template.html");
        String templateName = templateNameBuilder.toString();
        return getTemplateResource(templateName);
    }

    public Resource getTemplateResource(String templateName) throws NotFoundError {
        Resource resource = null;
        if (templatesDirectory != null) {
            File file = new File(templatesDirectory, templateName);
            resource = new FileSystemResource(file);

            if (!resource.exists())
                throw new NotFoundError();

        } else {
            resource = new ClassPathResource(TEMPLATES_CLASSPATH_PREFIX + templateName);

            if (!resource.exists())
                throw new NotFoundError();
        }

        return resource;
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
