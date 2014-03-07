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

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import piecework.content.ContentResource;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.security.AccessTracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * @author James Renfro
 */
public class FileSystemContentProviderTest {

    private FileSystemContentProvider contentProvider = new FileSystemContentProvider();

    private Process process;
    private ProcessDeployment deployment;
    private ContentProfileProvider modelProvider;
    private File rootDirectory;
    private File processDirectory;
    private File temporaryFile;
    private File temporaryCssFile;
    private File temporaryJsFile;
    private File temporaryHtmlFile;
    private String temporaryFileName;
    private String testData = "This is some test data to be saved to the file system as file content";
    private User principal;

    @Before
    public void setup() throws IOException {
        deployment = new ProcessDeployment.Builder()
                .deploymentId("123")
                .base("abc")
                .build();
        process = new Process.Builder()
                .processDefinitionKey("TEST")
                .deploy(new ProcessDeploymentVersion(deployment), deployment)
                .build();

        String tempFilesystem = System.getProperty("java.io.tmpdir");
        rootDirectory = new File(tempFilesystem, UUID.randomUUID().toString());
        processDirectory = new File(rootDirectory, "abc");
        processDirectory.mkdirs();
        temporaryFile = File.createTempFile("fsTestGeneric", ".test", processDirectory);
        temporaryCssFile = File.createTempFile("fsTest", ".css", processDirectory);
        temporaryJsFile = File.createTempFile("fsTest", ".js", processDirectory);
        temporaryHtmlFile = File.createTempFile("fsTest", ".html", processDirectory);
        temporaryFileName = temporaryFile.getName();
        FileWriter writer = null;
        try {
            writer = new FileWriter(temporaryFile);
            IOUtils.write(testData, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        contentProvider.setFilesystemRoot(rootDirectory.getAbsolutePath());
        contentProvider.init();

        principal = new User.Builder()
                .userId("testuser")
                .build();

        ContentProfile contentProfile = new ContentProfile.Builder()
                .baseDirectory(processDirectory.getAbsolutePath())
                .build();
        this.modelProvider = new ProcessDeploymentProviderStub(process, deployment, contentProfile, principal);
        contentProvider.accessTracker = Mockito.mock(AccessTracker.class);
    }

    @After
    public void shutdown() {
        if (temporaryFile != null && temporaryFile.exists())
            temporaryFile.delete();
        if (processDirectory != null && processDirectory.exists())
            processDirectory.delete();
        if (rootDirectory != null && rootDirectory.exists())
            rootDirectory.delete();
    }

    @Test
    public void retrieveFileWithoutPrefix() throws Exception {
        String location = temporaryFile.getAbsolutePath();
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
        Assert.assertNull(contentResource);
    }

    @Test
    public void retrieveFileWithoutCorrectPrefix() throws Exception {
        String location = "classpath:abc/" + temporaryFile.getAbsolutePath();
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
        Assert.assertNull(contentResource);
    }

    @Test(expected = ForbiddenError.class)
    public void retrieveFileOutsideApprovedPath() throws Exception {
        File illegalFile = new File(rootDirectory.getParent(), UUID.randomUUID().toString());
        String location = "file:" + illegalFile.getAbsolutePath();
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
        Assert.assertNull(contentResource);
    }

    @Test
    public void retrieveTestFileFromFilesystem() throws Exception {
        String location = "file:" + temporaryFile.getAbsolutePath();
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
        Assert.assertEquals(temporaryFileName, contentResource.getFilename());
        Assert.assertEquals(location, contentResource.getLocation());
        Assert.assertEquals(69l, contentResource.contentLength());
        String actual = IOUtils.toString(contentResource.getInputStream());
        Assert.assertEquals(testData, actual);
    }

    @Test
    public void retrieveCssFileFromFilesystem() throws Exception {
        String location = "file:" + temporaryCssFile.getAbsolutePath();
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
        Assert.assertEquals("text/css", contentResource.contentType());
    }

    @Test
    public void retrieveJsFileFromFilesystem() throws Exception {
        String location = "file:" + temporaryJsFile.getAbsolutePath();
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
        Assert.assertEquals("text/javascript", contentResource.contentType());
    }

    @Test
    public void retrieveHtmlFileFromFilesystem() throws Exception {
        String location = "file:" + temporaryHtmlFile.getAbsolutePath();
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, location);
        Assert.assertEquals("text/html", contentResource.contentType());
    }

    @Test
    public void verifyKey() {
        Assert.assertEquals("default-filesystem", contentProvider.getKey());
    }

    @Test
    public void verifyScheme() {
        Assert.assertEquals(Scheme.FILESYSTEM, contentProvider.getScheme());
    }

}
