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
package piecework.util;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import piecework.designer.model.view.IndexView;
import piecework.enumeration.CacheName;
import piecework.exception.NotFoundError;
import piecework.form.FormDisposition;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.ui.UserInterfaceSettings;
import piecework.ui.visitor.StaticResourceAggregatingVisitor;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public class UserInterfaceUtility {
    private static final Set<Class<?>> ACCEPTABLE_TEMPLATE_CLASSES =
            Sets.newHashSet(Explanation.class, Form.class, IndexView.class, Report.class, SearchResults.class);
    private static final Map<String, Class<?>> ACCEPTABLE_TEMPLATE_NAME_MAP;

    static {
        ACCEPTABLE_TEMPLATE_NAME_MAP = new HashMap<String, Class<?>>();
        for (Class<?> cls : ACCEPTABLE_TEMPLATE_CLASSES)
            ACCEPTABLE_TEMPLATE_NAME_MAP.put(cls.getSimpleName(), cls);

        ACCEPTABLE_TEMPLATE_NAME_MAP.put("SearchResults.form", SearchResults.class);
    }

    private static final Logger LOG = Logger.getLogger(UserInterfaceUtility.class);
    private static final String SCRIPTS_CLASSPATH_PREFIX = "META-INF/piecework/scripts/";
    private static final String TEMPLATES_CLASSPATH_PREFIX = "META-INF/piecework/templates/";

//    public static Resource externalScriptResource(Class<?> type, Object t, File scriptsDirectory) {
//        StringBuilder templateNameBuilder = new StringBuilder(type.getSimpleName());
//
//        if (type.equals(SearchResults.class)) {
//            SearchResults results = SearchResults.class.cast(t);
//            templateNameBuilder.append(".").append(results.getResourceName());
//        }
//
//        templateNameBuilder.append(".js");
//
//        String templateName = templateNameBuilder.toString();
//        Resource resource = null;
//        if (scriptsDirectory != null) {
//            resource = new FileSystemResource(new File(scriptsDirectory, templateName));
//
//            if (!resource.exists())
//                resource = new FileSystemResource(new File(scriptsDirectory, "script.js"));
//
//        } else {
//            resource = new ClassPathResource(SCRIPTS_CLASSPATH_PREFIX + templateName);
//
//            if (!resource.exists())
//                resource = new ClassPathResource(SCRIPTS_CLASSPATH_PREFIX + "script.js");
//        }
//
//        return resource;
//    }

    public static String templateName(String id, boolean anonymous) throws NotFoundError {
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

    public static String templateName(Class<?> type, Object t) {
        if (t != null && !type.isInstance(t))
            return null;
        if (!ACCEPTABLE_TEMPLATE_CLASSES.contains(type))
            return null;

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
        return templateNameBuilder.toString();
    }

    public static Resource template(File templatesDirectory, String templateName) throws NotFoundError {
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

    public static Resource resource(CacheName cacheName, Form form, Resource template, ContentRepository contentRepository, ServletContext servletContext, UserInterfaceSettings settings) {
        Process process = form != null ? form.getProcess() : null;
        FormDisposition disposition = form != null ? form.getDisposition() : null;
        boolean isAnonymous = form != null && form.isAnonymous();

        StaticResourceAggregatingVisitor visitor = null;

        CleanerProperties cleanerProperties = new CleanerProperties();
        cleanerProperties.setOmitXmlDeclaration(true);
        HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
        visitor = new StaticResourceAggregatingVisitor(servletContext, process, disposition, settings, contentRepository, isAnonymous);

        InputStream inputStream = null;
        try {
            inputStream = template.getInputStream();
            TagNode node = cleaner.clean(inputStream);
            node.traverse(visitor);
            switch (cacheName) {
                case SCRIPT:
                    return visitor.getScriptResource();
                case STYLESHEET:
                    return visitor.getStylesheetResource();
            }
        } catch (IOException ioe) {
            LOG.error("Unable to read template", ioe);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    public static long resourceSize(Resource resource) {
        long size = 0;
        if (resource.exists()) {
            try {
                size = resource.contentLength();
            } catch (IOException e) {
                LOG.error("Unable to determine size of template for " + resource.getFilename(), e);
            }
        }
        return size;
    }

}
