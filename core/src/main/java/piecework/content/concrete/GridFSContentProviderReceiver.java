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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import piecework.common.UuidGenerator;
import piecework.content.ContentProvider;
import piecework.content.ContentReceiver;
import piecework.enumeration.AlarmSeverity;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.ContentProfileProvider;
import piecework.security.AccessTracker;
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
    AccessTracker accessTracker;

    @Autowired
    GridFsOperations gridFsOperations;

    @Autowired
    UuidGenerator uuidGenerator;


    @Override
    public boolean expire(ContentProfileProvider modelProvider, String location) throws IOException {
        return false;
    }

    @Override
    public Content findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException {
        if (StringUtils.isEmpty(location))
            throw new MisconfiguredProcessException("Unable to find content with an empty location");

        String normalized = FilenameUtils.normalize(location);
        String basePath = basePath(modelProvider);

        if (!normalized.startsWith(basePath)) {
            accessTracker.alarm(AlarmSeverity.MINOR, "Attempt to access path " + normalized + " outside of " + basePath + " forbidden", modelProvider.principal());
            throw new ForbiddenError();
        }

        GridFSDBFile file = gridFsOperations.findOne(query(GridFsCriteria.whereFilename().is(location)));
        return ContentUtility.toContent(file);
    }

    @Override
    public Content save(ContentProfileProvider modelProvider, Content content) throws IOException {
        BasicDBObject metadata = new BasicDBObject();
        metadata.put("originalFilename", content.getName());

        String id = uuidGenerator.getNextId();
        String path = basePath(modelProvider) + id;

        GridFSFile file = gridFsOperations.store(content.getInputStream(), path, content.getContentType(), metadata);
        String contentId = file.getId().toString();

        return new Content.Builder(content)
                .contentId(contentId)
                .contentType(content.getContentType())
                .location(path)
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

    /*
     * The base path for all storage in the GridFS repository is the process definition key
     */
    private static String basePath(ContentProfileProvider contentProfileProvider) {
        String processDefinitionKey = contentProfileProvider.processDefinitionKey();
        return new StringBuilder("/").append(processDefinitionKey).append("/").toString();
    }

}
