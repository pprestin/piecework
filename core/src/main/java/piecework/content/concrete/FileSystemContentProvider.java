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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import piecework.content.ContentProvider;
import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.FileUtility;

import javax.annotation.PostConstruct;
import java.io.*;

/**
 * @author James Renfro
 */
@Service
public class FileSystemContentProvider implements ContentProvider {

    @Autowired
    private Environment environment;

    private java.io.File root;

    @PostConstruct
    public void init() {
        String filesystemRoot = environment.getProperty("base.filesystem.root", ".");
        this.root = new java.io.File(filesystemRoot);
    }

    @Override
    public Content findByPath(Process process, String base, String location, Entity principal) throws IOException {
        if (!location.startsWith("file:"))
            return null;

        java.io.File file = new java.io.File(location.substring("file:".length()));

        if (!FileUtility.isAncestorOf(root, file))
            throw new IOException("Cannot retrieve a file that is located outside of the content file system");

        FileSystemResource resource = new FileSystemResource(file);

        String contentType = null;
        if (location.endsWith(".css"))
            contentType = "text/css";
        else if (location.endsWith(".js"))
            contentType = "application/json";
        else if (location.endsWith(".html"))
            contentType = "text/html";

        return new Content.Builder().resource(resource).contentType(contentType).location(location).filename(file.getPath()).build();

    }

    @Override
    public Scheme getScheme() {
        return Scheme.FILESYSTEM;
    }

   @Override
   public String getKey() {
       return "default-filesystem";
   }

}
