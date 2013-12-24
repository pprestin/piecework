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

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResource;
import piecework.enumeration.Scheme;
import piecework.model.Content;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.util.PathUtility;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Takes paths from repeated calls to the handle() method and looks up the resources
 * at those paths, then appends their contents to an internal StringBuffer.
 *
 * Calling getStaticResource() retrieves a Resource that includes the aggregated data
 * and can be used to stream or inline it elsewhere.
 *
 * @author James Renfro
 */
public class StaticResourceAggregator {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final Logger LOG = Logger.getLogger(StaticResourceAggregator.class);

    private final ServletContext servletContext;
    private final Process process;
    private final ContentRepository contentRepository;
    private final StringBuffer buffer;
    private final UserInterfaceSettings settings;
    private final String base;

    public StaticResourceAggregator(ServletContext servletContext, Process process, ContentRepository contentRepository, UserInterfaceSettings settings, String base) {
        this.servletContext = servletContext;
        this.process = process;
        this.contentRepository = contentRepository;
        this.buffer = new StringBuffer();
        this.settings = settings;
        this.base = base;
    }

    public Resource getStaticResource() {
        return new DatedByteArrayResource(this.buffer.toString().getBytes(Charset.forName("UTF-8")));
    }

    public String handle(String path) {
        if (StringUtils.isEmpty(path))
            return null;

        BufferedReader reader = null;
        try {
            reader = reader(path);

            if (reader != null) {

                String cleanPath;
                // Strip off query string for the purpose of deciding if it's javascript or css
                int indexOf = path.indexOf('?');
                if (indexOf != -1 && indexOf < path.length())
                    cleanPath = path.substring(0, indexOf);
                else
                    cleanPath = path;

                if (!settings.isDoOptimization() || cleanPath.contains(".min.")) {
                    StringBuilder builder = new StringBuilder();
                    // Don't bother to compress files that are already compressed
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append(NEWLINE);
                    }

                    if (cleanPath.endsWith(".css"))
                        buffer.append(rebaseStylesheetUrls(builder.toString(), path));
                    else
                        buffer.append(builder);

                } else if (cleanPath.endsWith(".js")) {
                    buffer.append(compressJavaScript(reader, new Options(), path)).append(NEWLINE);
                } else if (cleanPath.endsWith(".css")) {
                    buffer.append(rebaseStylesheetUrls(compressStylesheet(reader, new Options()), path)).append(NEWLINE);
                }
                // If we successfully handled this path then return null to indicate that
                // no new path needs to be included
                return null;
            }
        } catch (Exception e) {
            LOG.warn("Unable to include path " + path + " in optimized script", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        // Otherwise, return a recomputed path that will correctly serve the resource
        // externally
        return PathUtility.recomputeStaticPath(path, settings);
    }


    private static String compressStylesheet(Reader in, Options o) {
        StringWriter out = new StringWriter();
        try {
            CssCompressor compressor = new CssCompressor(in);
            in.close();
            in = null;
            compressor.compress(out, o.lineBreakPos);
        } catch (Exception e) {
            LOG.error("Unable to compress css", e);
            try {
                return IOUtils.toString(in);
            } catch (IOException ioe) {
                LOG.error("Unable to output string", ioe);
            }
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        return out.toString();
    }

    private String rebaseStylesheetUrls(String content, String path) {
        int lastSlash = path.lastIndexOf('/');
        lastSlash = path.lastIndexOf('/', lastSlash - 1);
        String rootPath = path.substring(0, lastSlash + 1);

        return content.replaceAll("url\\('\\.\\./", "url('" + PathUtility.recomputeStaticPath(rootPath, settings));
    }

    private String compressJavaScript(Reader in, Options o, String path) {
        StringWriter out = new StringWriter();
        try {
            JavaScriptCompressor compressor = new JavaScriptCompressor(in, new YuiCompressorErrorReporter());
            in.close();
            in = null;
            compressor.compress(out, o.lineBreakPos, o.munge, o.verbose, o.preserveAllSemiColons, o.disableOptimizations);
        } catch (Exception e) {
            LOG.error("Unable to compress javascript", e);
            try {
                in = reader(path);
                return IOUtils.toString(in);
            } catch (Exception ioe) {
                LOG.error("Unable to output string", ioe);
            }
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        return out.toString();
    }

    private BufferedReader reader(String path) throws Exception {
        String fullPath;
        if (StringUtils.isNotEmpty(base)) {
            // Only add the base if we're looking for the resource in the
            // repository
            Scheme scheme = PathUtility.findScheme(path);
            if (scheme == Scheme.REPOSITORY)
                fullPath = base + "/" + path;
            else
                fullPath = path;
        } else {
            fullPath = path;
        }

        if (!PathUtility.checkForStaticPath(path)) {
            Content content = contentRepository.findByLocation(process, base, path);
            if (content != null) {
                return new BufferedReader(new InputStreamReader(content.getInputStream()));
            }
            return null;
        }

        int indexOf = path.indexOf("static/");

        if (indexOf > path.length())
            return null;

        String adjustedPath = path.substring(indexOf);
        ServletContextResource servletContextResource = new ServletContextResource(servletContext, adjustedPath);

        if (!servletContextResource.exists())
            return null;

        return new BufferedReader(new InputStreamReader(servletContextResource.getInputStream()));

//        File file = new File(settings.getAssetsDirectoryPath(), adjustedPath);
//        String absolutePath = file.getAbsolutePath();
//        LOG.debug("Reading from " + absolutePath);
//        if (!file.exists())
//            return null;
//
//        return new BufferedReader(new FileReader(file));
    }

    public static class Options {
        public String charset = "UTF-8";
        public int lineBreakPos = -1;
        public boolean munge = true;
        public boolean verbose = false;
        public boolean preserveAllSemiColons = false;
        public boolean disableOptimizations = false;
    }

    private static class YuiCompressorErrorReporter implements ErrorReporter {
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                LOG.warn(message);
            } else {
                LOG.warn(line + ':' + lineOffset + ':' + message);
            }
        }

        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                LOG.error(message);
            } else {
                LOG.error(line + ':' + lineOffset + ':' + lineSource + ":" + message);
            }
        }

        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }
}
