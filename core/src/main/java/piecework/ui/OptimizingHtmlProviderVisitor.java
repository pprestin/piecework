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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import com.yahoo.platform.yui.compressor.YUICompressor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.TagNode;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import piecework.model.Content;
import piecework.model.User;
import piecework.persistence.ContentRepository;

import java.io.*;
import java.util.Map;

/**
 * @author James Renfro
 */
public class OptimizingHtmlProviderVisitor extends HtmlProviderVisitor {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final Logger LOG = Logger.getLogger(OptimizingHtmlProviderVisitor.class);

    private final StringBuffer buffer;
    private final String assetsDirectoryPath;
    private final ContentRepository contentRepository;
    private final boolean doOptimization;

    public OptimizingHtmlProviderVisitor(Object t, Class<?> type, User user, ObjectMapper objectMapper, Environment environment, ContentRepository contentRepository) {
        super(t, type, user, objectMapper, environment);
        this.buffer = new StringBuffer();
        this.assetsDirectoryPath = environment.getProperty("assets.directory");
        this.contentRepository = contentRepository;
        this.doOptimization = environment.getProperty("javascript.minification", Boolean.class, Boolean.FALSE);
    }

    public ByteArrayResource getScriptResource() {
        return new ByteArrayResource(buffer.toString().getBytes());
    }

    protected void handleBody(String tagName, TagNode tagNode) {

    }

    protected void handleStylesheet(String tagName, TagNode tagNode) {
        super.handleStylesheet(tagName, tagNode);
    }

    protected void handleScript(String tagName, TagNode tagNode) {
        Map<String, String> attributes = tagNode.getAttributes();
        String href = attributes.get("href");
        String src = attributes.get("src");
        String main = attributes.get("data-main");

        handleAttribute("href", href, tagNode);
        handleAttribute("src", src, tagNode);
    }

    public static String compressJavaScript(Reader in, Options o) {
        StringWriter out = new StringWriter();
        try {
            JavaScriptCompressor compressor = new JavaScriptCompressor(in, new YuiCompressorErrorReporter());
            in.close();
            in = null;
            compressor.compress(out, o.lineBreakPos, o.munge, o.verbose, o.preserveAllSemiColons, o.disableOptimizations);
        } catch (Exception e) {
            LOG.error("Unable to compress javascript", e);
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

    private synchronized void handleAttribute(String name, String path, TagNode tagNode) {
        if (StringUtils.isEmpty(path))
            return;

        BufferedReader reader = null;
        try {
            reader = reader(path);

            if (reader != null) {

                if (!doOptimization || path.contains(".min.")) {
                    // Don't bother to compress files that are already compressed
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append(NEWLINE);
                    }
                } else {
                    buffer.append(compressJavaScript(reader, new Options())).append(NEWLINE);
                }
                tagNode.removeFromTree();
            } else {
                tagNode.addAttribute(name, recomputeStaticPath(path, assetsUrl));
            }
        } catch (Exception e) {
            LOG.warn("Unable to include path " + path + " in optimized script", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private BufferedReader reader(String path) throws Exception {
        if (!checkForStaticPath(path)) {
            Content content = contentRepository.findByLocation(path);
            if (content != null) {
                return new BufferedReader(new InputStreamReader(content.getInputStream()));
            }
            return null;
        }

        int indexOf = path.indexOf("static/");

        if (indexOf > path.length())
            return null;

        String adjustedPath = path.substring(indexOf);
        File file = new File(assetsDirectoryPath, adjustedPath);
        String absolutePath = file.getAbsolutePath();
        LOG.debug("Reading from " + absolutePath);
        if (!file.exists())
            return null;

        return new BufferedReader(new FileReader(file));
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
                LOG.error(line + ':' + lineOffset + ':' + message);
            }
        }

        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }

}
