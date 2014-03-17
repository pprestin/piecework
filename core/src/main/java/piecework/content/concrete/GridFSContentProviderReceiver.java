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
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import piecework.common.UuidGenerator;
import piecework.content.ContentProvider;
import piecework.content.ContentReceiver;
import piecework.content.ContentResource;
import piecework.content.Version;
import piecework.enumeration.AlarmSeverity;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.security.AccessTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public ContentResource checkout(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException {
        return findByLocation(modelProvider, location);
    }

    @Override
    public boolean expire(ContentProfileProvider modelProvider, String location) throws IOException {
        return false;
    }

    @Override
    public ContentResource findByLocation(ContentProfileProvider modelProvider, String location) throws PieceworkException {
        if (StringUtils.isEmpty(location))
            throw new MisconfiguredProcessException("Unable to find content with an empty location");

        String normalized = FilenameUtils.normalize(location);
        String basePath = basePath(modelProvider);

        if (!normalized.startsWith(basePath)) {
            accessTracker.alarm(AlarmSeverity.MINOR, "Attempt to access path " + normalized + " outside of " + basePath + " forbidden", modelProvider.principal());
            throw new ForbiddenError();
        }

        return gridFsContentResource(null, location);
    }

    @Override
    public boolean publish(ProcessInstanceProvider modelProvider) throws PieceworkException, IOException {
        return false;
    }

    @Override
    public boolean release(ContentProfileProvider modelProvider, String location) throws PieceworkException, IOException {
        return false;
    }

    @Override
    public ContentResource replace(ContentProfileProvider modelProvider, ContentResource contentResource, String location) throws PieceworkException, IOException {
        BasicDBObject metadata = new BasicDBObject();
        metadata.put(GridFsContentResource.NAME, contentResource.getName());
        metadata.put(GridFsContentResource.DESCRIPTION, contentResource.getDescription());
        metadata.put(GridFsContentResource.FILENAME, contentResource.getFilename());
        metadata.put(GridFsContentResource.CONTENT_LENGTH, contentResource.contentLength());
        metadata.put(GridFsContentResource.E_TAG, contentResource.eTag());
        metadata.put(GridFsContentResource.LAST_MODIFIED, contentResource.lastModified());
        metadata.put(GridFsContentResource.LAST_MODIFIED_BY, contentResource.lastModifiedBy());

        // GridFS will version automatically, and retrieve the latest version using the uploadDate timestamp
        GridFSFile file = gridFsOperations.store(contentResource.getInputStream(), location, contentResource.contentType(), metadata);
        return gridFsContentResource(file, location);
    }

    @Override
    public ContentResource save(ContentProfileProvider modelProvider, ContentResource contentResource) throws PieceworkException, IOException {
        BasicDBObject metadata = new BasicDBObject();
        metadata.put(GridFsContentResource.NAME, contentResource.getName());
        metadata.put(GridFsContentResource.DESCRIPTION, contentResource.getDescription());
        metadata.put(GridFsContentResource.FILENAME, contentResource.getFilename());
        metadata.put(GridFsContentResource.CONTENT_LENGTH, contentResource.contentLength());
        metadata.put(GridFsContentResource.E_TAG, contentResource.eTag());
        metadata.put(GridFsContentResource.LAST_MODIFIED, contentResource.lastModified());
        metadata.put(GridFsContentResource.LAST_MODIFIED_BY, contentResource.lastModifiedBy());

        String id = uuidGenerator.getNextId();
        String location = basePath(modelProvider) + id;

        GridFSFile file = gridFsOperations.store(contentResource.getInputStream(), location, contentResource.contentType(), metadata);
        return gridFsContentResource(file, location);
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

    private GridFsContentResource gridFsContentResource(GridFSFile current, String location) throws NotFoundError {
        // Retrieve in ascending order, then reverse versions - this allows us to count up in an intuition way,
        // setting version numbers
        List<GridFSDBFile> files = gridFsOperations.find(query(GridFsCriteria.whereFilename().is(location)).with(new Sort(Sort.Direction.ASC, "uploadDate")));
        List<Version> versions = new ArrayList<Version>();
        GridFSFile latest = current;
        if (files != null && !files.isEmpty()) {
            int count = 1;
            for (GridFSDBFile file : files) {
                DBObject dbObject = file.getMetaData();
                Object createDateObj = dbObject != null ? dbObject.get(GridFsContentResource.LAST_MODIFIED) : null;
                Long createDate = createDateObj != null ? Long.class.cast(createDateObj) : Long.valueOf(0);
                Object createdByObj = dbObject != null ? dbObject.get(GridFsContentResource.LAST_MODIFIED_BY) : null;
                String createdBy = createdByObj != null ? createdByObj.toString() : null;

                String versionId = file != null && file.getUploadDate() != null ? file.getId().toString() + "?uploadDate=" + file.getUploadDate().getTime() : null;
                String versionLocation = file != null && file.getUploadDate() != null ? location + "?uploadDate=" + file.getUploadDate().getTime() : null;

                versions.add(new Version("" + count, createdBy, createDate.longValue(), versionId, versionLocation));
                if (current == null)
                    latest = file;
                count++;
            }
        }

        if (latest == null)
            throw new NotFoundError();

        Collections.reverse(versions);
        return new GridFsContentResource(gridFsOperations, latest, location, versions);
    }

}
