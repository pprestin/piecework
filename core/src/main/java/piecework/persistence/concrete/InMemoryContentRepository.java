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
package piecework.persistence.concrete;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.bouncycastle.jce.provider.JDKMessageDigest;
import piecework.persistence.ContentRepository;
import piecework.model.Content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class InMemoryContentRepository implements ContentRepository {

    Map<String, Content> contentMap;
    Map<String, Content> contentLocationMap;

    public InMemoryContentRepository() {
        this.contentMap = new Hashtable<String, Content>();
        this.contentLocationMap = new Hashtable<String, Content>();
    }

    @Override
    public Content findByLocation(String location) {
        return contentLocationMap.get(location);
    }

    @Override
    public List<Content> findByLocationPattern(String locationPattern) throws IOException {
        List<Content> contents = new ArrayList<Content>();

        Pattern pattern = Pattern.compile(locationPattern, 0);

        for (Map.Entry<String, Content> entry : contentLocationMap.entrySet()) {
            if (pattern.matcher(entry.getKey()).matches())
                contents.add(entry.getValue());
        }
        return contents;
    }

    @Override
    public Content save(Content content) {
        String contentId = content.getContentId() == null ? UUID.randomUUID().toString() : content.getContentId();

        long contentLength = 0;
        InputStream inputStream = null;
        String md5 = null;

        if (content.getInputStream() != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                contentLength = IOUtils.copy(content.getInputStream(), outputStream);

                byte[] data = outputStream.toByteArray();
                inputStream = new ByteArrayInputStream(data);
                Base64 encoder = new Base64();
                md5 = new String(encoder.encode(JDKMessageDigest.MD5.getInstance("MD5").digest(data)));
            } catch (IOException e) {
                // Don't want to change signature for testing, but also don't want to lose this exception
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                // Ditto
                throw new RuntimeException(e);
            }
        }

        Content stored = new Content.Builder(content)
                .contentId(contentId)
                .inputStream(inputStream)
                .length(contentLength)
                .lastModified(new Date())
                .md5(md5)
                .build();

        contentLocationMap.put(content.getLocation(), stored);
        contentMap.put(contentId, stored);

        return stored;
    }
}
