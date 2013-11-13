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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import piecework.identity.IdentityDetails;
import piecework.model.SearchResults;
import piecework.model.User;
import piecework.persistence.ContentRepository;
import piecework.ui.OptimizingHtmlProviderVisitor;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class FormTemplateService {

    private static final Logger LOG = Logger.getLogger(FormTemplateService.class);

    @Autowired
    CacheManager cacheManager;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    Environment environment;

    @Autowired
    JacksonJaxbJsonProvider jacksonJaxbJsonProvider;

    private enum ResourceType { SCRIPT, HTML };

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

    public Resource getTemplateResource(Class<?> type, Object t) {
        StringBuilder templateNameBuilder = new StringBuilder(type.getSimpleName());

        if (type.equals(SearchResults.class)) {
            SearchResults results = SearchResults.class.cast(t);
            templateNameBuilder.append(".").append(results.getResourceName());
        }

        templateNameBuilder.append(".template.html");
        String templateName = templateNameBuilder.toString();
        return getTemplateResource(templateName);
    }

    public Resource getTemplateResource(String templateName) {
        Resource resource = null;
        if (templatesDirectory != null) {
            File file = new File(templatesDirectory, templateName);
            resource = new FileSystemResource(file);

            if (!resource.exists())
                resource = new FileSystemResource(new File(templatesDirectory, "Layout.template.html"));

        } else {
            resource = new ClassPathResource(TEMPLATES_CLASSPATH_PREFIX + templateName);

            if (!resource.exists())
                resource = new ClassPathResource(TEMPLATES_CLASSPATH_PREFIX + "Layout.template.html");
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

//    private Resource getResource(ResourceType resourceType, String resourceName) {
//        String classpathPrefix;
//        File directory;
//        if (resourceType == ResourceType.SCRIPT) {
//            classpathPrefix = SCRIPTS_CLASSPATH_PREFIX;
//            directory = scriptsDirectory;
//        } else if (resourceType == ResourceType.HTML) {
//            classpathPrefix = TEMPLATES_CLASSPATH_PREFIX;
//            directory = templatesDirectory;
//        }
//
//        Cache cache = cacheManager.getCache("templateCache");
//        Cache.ValueWrapper wrapper = cache.get(resourceName);
//
//        if (wrapper != null)
//            return (Resource) wrapper.get();
//
//
//
//    }


}
