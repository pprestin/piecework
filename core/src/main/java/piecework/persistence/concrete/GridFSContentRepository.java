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

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import piecework.persistence.ContentRepository;
import piecework.model.Content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author James Renfro
 */
@Service
public class GridFSContentRepository implements ContentRepository {

    @Autowired
    GridFsOperations gridFsOperations;

    @Override
    public Content findByLocation(String location) {
        GridFSDBFile file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(location)));
        return toContent(file);
    }

    @Override
    public List<Content> findByLocationPattern(String locationPattern) throws IOException {
        GridFsResource[] resources = gridFsOperations.getResources(locationPattern);

        if (resources != null && resources.length > 0) {
            List<Content> contents = new ArrayList<Content>(resources.length);
            for (GridFsResource resource : resources) {
                contents.add(toContent(resource));
            }
            return contents;
        }
        return Collections.emptyList();
    }

    @Override
    public Content save(Content content) {
        GridFSFile file = gridFsOperations.store(content.getInputStream(), content.getLocation(), content.getContentType());
        String contentId = file.getId().toString();
        return new Content.Builder(content)
                .contentId(contentId)
                .build();
    }

    private Content toContent(GridFSDBFile file) {
        String fileId = file.getId().toString();

        return new Content.Builder()
                .contentId(fileId)
                .contentType(file.getContentType())
                .location(file.getFilename())
                .inputStream(file.getInputStream())
                .lastModified(file.getUploadDate())
                .length(Long.valueOf(file.getLength()))
                .md5(file.getMD5())
                .build();
    }

    private Content toContent(GridFsResource resource) throws IOException {
        String resourceId = resource.getId().toString();

        return new Content.Builder()
                .contentId(resourceId)
                .contentType(resource.getContentType())
                .location(resource.getFilename())
                .inputStream(resource.getInputStream())
                .lastModified(resource.lastModified())
                .length(Long.valueOf(resource.contentLength()))
                .build();
    }

}
