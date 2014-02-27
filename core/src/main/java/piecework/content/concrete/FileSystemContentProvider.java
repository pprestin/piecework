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
package piecework.content.concrete;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.content.ContentProvider;
import piecework.enumeration.AlarmSeverity;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.ContentProfileProvider;
import piecework.security.AccessTracker;
import piecework.util.FileUtility;

import javax.annotation.PostConstruct;
import java.io.*;
import java.io.File;

/**
 * @author James Renfro
 */
@Service
public class FileSystemContentProvider implements ContentProvider {

    private static final Logger LOG = Logger.getLogger(FileSystemContentProvider.class);

    @Autowired
    AccessTracker accessTracker;

    @Autowired
    private Environment environment;

    private File root;

    private String filesystemRoot;

    @PostConstruct
    public void init() {
        if (environment != null && !StringUtils.isEmpty(filesystemRoot))
            filesystemRoot = environment.getProperty("base.filesystem.root", ".");

        if (StringUtils.isNotEmpty(filesystemRoot))
            this.root = new java.io.File(filesystemRoot);
    }

    @Override
    public Content findByLocation(ContentProfileProvider modelProvider, String rawPath) throws PieceworkException {
        if (!rawPath.startsWith("file:")) {
            LOG.error("Should not be looking for a file resource without the file prefix");
            return null;
        }

        String path = rawPath.substring("file:".length());

        ContentProfile contentProfile = modelProvider.contentProfile();
        // Show never use FileSystemContentProvider unless the content profile explicitly
        // specifies a base path
        if (contentProfile == null || StringUtils.isEmpty(contentProfile.getBaseDirectory()))
            return null;

        File file = new File(path);

        try {
            // Check that base directory is a descendent of Piecework's filesystem root
            File baseDirectory = new File(contentProfile.getBaseDirectory());
            boolean isBaseDirectoryDescendent = FileUtility.isAncestorOf(root, baseDirectory);
            boolean isFileDescendent = FileUtility.isAncestorOf(baseDirectory, file);
            boolean isFileReadAllowed = isBaseDirectoryDescendent && isFileDescendent;
            if (! isFileReadAllowed) {
                accessTracker.alarm(AlarmSeverity.MINOR, "Attempt to access file " + file.getAbsolutePath() + " outside of " + root.getAbsolutePath() + " forbidden", modelProvider.principal());
                throw new ForbiddenError();
            }
        } catch (IOException ioe) {
            LOG.error("Unable to determine if file is within approved limits of file system", ioe);
            throw new InternalServerError(Constants.ExceptionCodes.system_misconfigured, ioe.getMessage());
        }

        FileSystemResource resource = new FileSystemResource(file);

        String filename = file.getName();
        String contentType = null;
        if (filename.endsWith(".css"))
            contentType = "text/css";
        else if (filename.endsWith(".js"))
            contentType = "text/javascript";
        else if (filename.endsWith(".html"))
            contentType = "text/html";

        String location = "file:" + file.getAbsolutePath();

        try {
            return new Content.Builder()
                    .resource(resource)
                    .length(resource.contentLength())
                    .contentType(contentType)
                    .location(location)
                    .filename(file.getName())
                    .build();

        } catch (IOException ioe) {
            LOG.error("Unable to determine content length of file: " + file.getAbsolutePath(), ioe);
            throw new InternalServerError(Constants.ExceptionCodes.system_misconfigured, ioe.getMessage());
        }
    }

    @Override
    public Scheme getScheme() {
        return Scheme.FILESYSTEM;
    }

    @Override
    public String getKey() {
        return "default-filesystem";
    }

    public void setFilesystemRoot(String filesystemRoot) {
        this.filesystemRoot = filesystemRoot;
    }
}
