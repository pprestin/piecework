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

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import piecework.content.ContentProvider;
import piecework.content.ContentReceiver;
import piecework.enumeration.Scheme;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ContentUtility;

import java.io.IOException;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author James Renfro
 */
@Service
public class GridFSContentProviderReceiver implements ContentProvider, ContentReceiver {

    private static final Logger LOG = Logger.getLogger(GridFSContentProviderReceiver.class);

    @Autowired
    GridFsOperations gridFsOperations;

    @Override
    public Content findByPath(piecework.model.Process process, String base, String location) {
        GridFSDBFile file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(location)));
        return ContentUtility.toContent(file);
    }

    @Override
    public Content save(Process process, ProcessInstance instance, Content content, Entity principal) throws IOException {
        BasicDBObject metadata = new BasicDBObject();
        metadata.put("originalFilename", content.getName());

        GridFSFile file = gridFsOperations.store(content.getInputStream(), content.getLocation(), content.getContentType(), metadata);
        String contentId = file.getId().toString();

        return new Content.Builder(content)
                .contentId(contentId)
                .length(file.getLength())
                .lastModified(file.getUploadDate())
                .md5(file.getMD5())
                .build();
    }

    @Override
    public Scheme getScheme() {
        return Scheme.REPOSITORY;
    }

    @Override
    public String getKey() {
        return "default-gridfs";
    }

}
