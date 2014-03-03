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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import piecework.content.ContentProvider;
import piecework.content.ContentReceiver;
import piecework.content.ContentResource;
import piecework.enumeration.Scheme;
import piecework.exception.PieceworkException;
import piecework.persistence.ContentProfileProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class InMemoryContentProviderReceiver implements ContentProvider, ContentReceiver {

    private Map<String, ContentResource> contentMap;
    private Map<String, ContentResource> contentLocationMap;

    public InMemoryContentProviderReceiver() {
        this.contentMap = new Hashtable<String, ContentResource>();
        this.contentLocationMap = new Hashtable<String, ContentResource>();
    }

    @Override
    public boolean expire(ContentProfileProvider modelProvider, String location) throws IOException {
        return false;
    }

    @Override
    public synchronized ContentResource findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException {
        return contentLocationMap.get(location);
    }

    @Override
    public synchronized Scheme getScheme() {
        return Scheme.REPOSITORY;
    }

    @Override
    public synchronized ContentResource save(ContentProfileProvider modelProvider, ContentResource contentResource) throws PieceworkException, IOException {
        String contentId = contentResource.getContentId() == null ? UUID.randomUUID().toString() : contentResource.getContentId();

        long contentLength = 0;
        InputStream inputStream = null;
        String md5 = null;

        if (contentResource.getInputStream() != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                contentLength = IOUtils.copy(contentResource.getInputStream(), outputStream);

                byte[] data = outputStream.toByteArray();
                inputStream = new ByteArrayInputStream(data);
                Base64 encoder = new Base64();
                md5 = new String(encoder.encode(MessageDigest.getInstance("MD5").digest(data)));
            } catch (IOException e) {
                // Don't want to change signature for testing, but also don't want to lose this exception
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                // Ditto
                throw new RuntimeException(e);
            }
        }

        ContentResource stored = new BasicContentResource.Builder()
                .contentId(contentId)
                .inputStream(inputStream)
                .length(contentLength)
                .lastModified(new Date())
//                .md5(md5)
                .build();

        contentLocationMap.put(contentResource.getLocation(), stored);
        contentMap.put(contentId, stored);

        return stored;
    }

    public List<ContentResource> findByLocationPattern(String locationPattern) throws IOException {
        List<ContentResource> contentResources = new ArrayList<ContentResource>();

        if (locationPattern.contains("*"))
            locationPattern = locationPattern.replace("*", ".*");

        Pattern pattern = Pattern.compile(locationPattern, 0);

        for (Map.Entry<String, ContentResource> entry : contentLocationMap.entrySet()) {
            if (pattern.matcher(entry.getKey()).matches())
                contentResources.add(entry.getValue());
        }
        return contentResources;
    }

    @Override
    public String getKey() {
        return "default-memory";
    }
}
