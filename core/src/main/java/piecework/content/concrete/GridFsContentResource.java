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

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import piecework.content.ContentResource;
import piecework.content.Version;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author James Renfro
 */
public class GridFsContentResource implements ContentResource {

    public final static String NAME = "name";
    public final static String FILENAME = "originalFileName";
    public final static String CONTENT_LENGTH = "contentLength";
    public final static String DESCRIPTION = "description";
    public final static String E_TAG = "eTag";
    public final static String LAST_MODIFIED = "lastModified";
    public final static String LAST_MODIFIED_BY = "lastModifiedBy";

    private final GridFSFile file;
    private final GridFsOperations gridFsOperations;
    private final String location;
    private final Date uploadDate;
    private final DBObject metadata;
    private final List<Version> versions;

    public GridFsContentResource(GridFsOperations gridFsOperations, GridFSFile file, String location, Date uploadDate, List<Version> versions) {
        this.gridFsOperations = gridFsOperations;
        this.file = file;
        this.location = location;
        this.uploadDate = uploadDate;
        this.metadata = file.getMetaData();
        this.versions = versions;
    }

    @Override
    public String getContentId() {
        return file.getId().toString();
    }

    @Override
    public String contentType() {
        return file.getContentType();
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getName() {
        return metadata != null ? String.class.cast(metadata.get(NAME)) : null;
    }

    @Override
    public String getFilename() {
        return metadata != null ? String.class.cast(metadata.get(FILENAME)) : null;
    }

    @Override
    public String getDescription() {
        return metadata != null ? String.class.cast(metadata.get(DESCRIPTION)) : null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Query query = query(GridFsCriteria.whereFilename().is(location));
        if (uploadDate != null)
            query.addCriteria(GridFsCriteria.where("uploadDate").is(uploadDate));
        GridFSDBFile file = gridFsOperations.findOne(query);
        return file.getInputStream();
    }

    @Override
    public long contentLength() {
        return file.getLength();
    }

    @Override
    public long lastModified() {
        return file.getUploadDate() != null ? file.getUploadDate().getTime() : 0l;
    }

    @Override
    public String lastModifiedBy() {
        return metadata != null ? String.class.cast(metadata.get(LAST_MODIFIED_BY)) : null;
    }

    @Override
    public String eTag() {
        return metadata != null ? String.class.cast(metadata.get(E_TAG)) : null;
    }

    @Override
    public boolean publish() {
        return false;
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public List<Version> versions() {
        return versions;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        InputStream input = getInputStream();
        try {
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
