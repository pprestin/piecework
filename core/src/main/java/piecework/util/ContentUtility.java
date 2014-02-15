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

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.poi.util.IOUtils;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import piecework.model.Content;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

/**
 * @author James Renfro
 */
public class ContentUtility {

    public static Content toContent(GridFsResource resource) throws IOException {
        String resourceId = resource.getId().toString();

        return new Content.Builder()
                .contentId(resourceId)
                .contentType(resource.getContentType())
                .filename(resource.getFilename())
                .location(resource.getFilename())
                .inputStream(resource.getInputStream())
                .lastModified(resource.lastModified())
                .length(Long.valueOf(resource.contentLength()))
                .build();
    }

    public static Content toContent(GridFSDBFile file) {
        if (file == null)
            return null;

        String fileId = file.getId().toString();
        DBObject metadata = file.getMetaData();
        String originalFileName = metadata != null ? String.class.cast(metadata.get("originalFilename")) : null;

        return new Content.Builder()
                .contentId(fileId)
                .contentType(file.getContentType())
                .filename(originalFileName)
                .location(file.getFilename())
                .inputStream(file.getInputStream())
                .lastModified(file.getUploadDate())
                .length(Long.valueOf(file.getLength()))
                .md5(file.getMD5())
                .build();
    }

    public static Content toContent(URI uri, HttpEntity entity, Date lastModified, String eTag) throws IOException {
        if (entity == null)
            return null;

        String url = uri.toString();
        String originalFileName = FileUtility.resolveFilenameFromPath(uri.getPath());
        String contentType = entity.getContentType() != null ? entity.getContentType().getValue() : "application/octet-stream";

        InputStream inputStream = entity.getContent();

        if (inputStream != null) {
            inputStream = new BufferedInputStream(inputStream);
//            inputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
        }

        return new Content.Builder()
                .contentId(url)
                .contentType(contentType)
                .filename(originalFileName)
                .location(url)
                .inputStream(inputStream)
                .lastModified(lastModified)
                .length(entity.getContentLength())
                .md5(eTag)
                .build();
    }

}
