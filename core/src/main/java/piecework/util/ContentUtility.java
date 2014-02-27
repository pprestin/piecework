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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.poi.util.IOUtils;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import piecework.Constants;
import piecework.content.concrete.RemoteResource;
import piecework.exception.InternalServerError;
import piecework.exception.PieceworkException;
import piecework.model.Content;
import piecework.model.ContentProfile;
import piecework.persistence.ContentProfileProvider;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ContentUtility {

    public static String contentHandlerKey(ContentProfileProvider modelProvider) throws PieceworkException {
        if (modelProvider == null)
            throw new InternalServerError(Constants.ExceptionCodes.system_misconfigured, "Couldn't get content handler key from null data model provider");

        ContentProfile contentProfile = modelProvider.contentProfile();
        if (contentProfile != null && StringUtils.isNotEmpty(contentProfile.getContentHandlerKey()))
            return contentProfile.getContentHandlerKey();

        return null;
    }

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

    public static Content toContent(CloseableHttpClient client, URI uri) {
        if (client == null)
            return null;

        String url = uri.toString();

        return new Content.Builder()
                .contentId(url)
                .location(url)
                .resource(new RemoteResource(client, uri))
                .build();
    }

    public static boolean validateClasspath(String base, String classpath) {
        String normalizedBase = org.springframework.util.StringUtils.cleanPath(base);
        String normalizedPath = org.springframework.util.StringUtils.cleanPath(classpath);
        return classpath.startsWith(base);
    }

    public static boolean validateRemoteLocation(Set<String> acceptableRegularExpressions, URI uri) {
        String location = uri.toString();
        if (acceptableRegularExpressions != null && !acceptableRegularExpressions.isEmpty()) {
            for (String acceptableRegularExpression : acceptableRegularExpressions) {
                Pattern pattern = Pattern.compile(acceptableRegularExpression);
                if (pattern.matcher(location).matches())
                    return true;
            }
        }
        return false;
    }

    public static boolean validateScheme(URI uri, Set<String> validSchemes) throws PieceworkException {
        String scheme = uri.getScheme();
        return StringUtils.isNotEmpty(scheme) && validSchemes != null && validSchemes.contains(scheme);
    }

}
