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

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import piecework.designer.model.view.IndexView;
import piecework.enumeration.CacheName;
import piecework.exception.NotFoundError;
import piecework.form.FormDisposition;
import piecework.model.Explanation;
import piecework.model.Form;
import piecework.model.Report;
import piecework.model.SearchResults;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.repository.ContentRepository;
import piecework.settings.UserInterfaceSettings;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class UserInterfaceUtilityTest {

    @Mock
    ContentRepository contentRepository;

    @Mock
    FormDisposition disposition;

    @Mock
    Form form;

    @Mock
    ServletContext servletContext;

    @Mock
    Resource template;

    @Mock
    UserInterfaceSettings settings;

    @Before
    public void setup() {

    }

    @Test
    public void testTemplateNameExplanationAnonymous() throws NotFoundError {
        Assert.assertEquals("Explanation.anonymous.template.html", UserInterfaceUtility.templateName("Explanation", true));
    }

    @Test
    public void testTemplateNameFormAnonymous() throws NotFoundError {
        Assert.assertEquals("Form.anonymous.template.html", UserInterfaceUtility.templateName("Form", true));
    }

    @Test
    public void testTemplateNameIndexViewAnonymous() throws NotFoundError {
        Assert.assertEquals("IndexView.anonymous.template.html", UserInterfaceUtility.templateName("IndexView", true));
    }

    @Test
    public void testTemplateNameReportAnonymous() throws NotFoundError {
        Assert.assertEquals("Report.anonymous.template.html", UserInterfaceUtility.templateName("Report", true));
    }

    @Test
    public void testTemplateNameSearchResultsAnonymous() throws NotFoundError {
        Assert.assertEquals("SearchResults.anonymous.template.html", UserInterfaceUtility.templateName("SearchResults", true));
    }

    @Test
    public void testTemplateNameExplanation() throws NotFoundError {
        Assert.assertEquals("Explanation.template.html", UserInterfaceUtility.templateName("Explanation", false));
    }

    @Test
    public void testTemplateNameForm() throws NotFoundError {
        Assert.assertEquals("Form.template.html", UserInterfaceUtility.templateName("Form", false));
    }

    @Test
    public void testTemplateNameIndexView() throws NotFoundError {
        Assert.assertEquals("IndexView.template.html", UserInterfaceUtility.templateName("IndexView", false));
    }

    @Test
    public void testTemplateNameReport() throws NotFoundError {
        Assert.assertEquals("Report.template.html", UserInterfaceUtility.templateName("Report", false));
    }

    @Test
    public void testTemplateNameSearchResults() throws NotFoundError {
        Assert.assertEquals("SearchResults.template.html", UserInterfaceUtility.templateName("SearchResults", false));
    }

    @Test
    public void testTemplateNameInvalid() throws NotFoundError {
        Assert.assertNull(UserInterfaceUtility.templateName("Invalid", false));
    }

    @Test
    public void testTemplateNameExplanationClass() throws NotFoundError {
        Assert.assertEquals("Explanation.template.html", UserInterfaceUtility.templateName(Explanation.class, null));
    }

    @Test
    public void testTemplateNameFormClass() throws NotFoundError {
        Assert.assertEquals("Form.template.html", UserInterfaceUtility.templateName(Form.class, null));
    }

    @Test
    public void testTemplateNameIndexViewClass() throws NotFoundError {
        Assert.assertEquals("IndexView.template.html", UserInterfaceUtility.templateName(IndexView.class, null));
    }

    @Test
    public void testTemplateNameReportClass() throws NotFoundError {
        Assert.assertEquals("Report.template.html", UserInterfaceUtility.templateName(Report.class, null));
    }

    @Test
    public void testTemplateNameSearchResultsClass() throws NotFoundError {
        SearchResults searchResults = Mockito.mock(SearchResults.class);
        Mockito.doReturn("form")
               .when(searchResults).getResourceName();
        Assert.assertEquals("SearchResults.form.template.html", UserInterfaceUtility.templateName(SearchResults.class, searchResults));
    }

    @Test
    public void testTemplateNameInvalidClass() {
        Assert.assertNull(UserInterfaceUtility.templateName(Test.class, null));
    }

    @Test
    public void testTemplateFromClasspath() throws NotFoundError, IOException {
        String templateName = UserInterfaceUtility.templateName(Form.class, null);
        File templatesDirectory = null;
        Resource resource = UserInterfaceUtility.template(templatesDirectory, templateName);
        Assert.assertNotNull(resource);
        String resourceContent = IOUtils.toString(resource.getInputStream());
        Assert.assertTrue(resourceContent.length() > 10);
        Assert.assertEquals("Form.template.html", resource.getFilename());
        Assert.assertEquals(resourceContent.length(), UserInterfaceUtility.resourceSize(resource));
    }

    @Test
    public void testScriptResource() throws NotFoundError, IOException {
        Mockito.doReturn(getWorkingDirectory())
               .when(settings).getAssetsDirectoryPath();

        ProcessDeploymentProvider modelProvider = null;
        String templateName = UserInterfaceUtility.templateName(Form.class, null);
        File templatesDirectory = null;
        Resource template = UserInterfaceUtility.template(templatesDirectory, templateName);
        Resource resource = UserInterfaceUtility.resource(CacheName.SCRIPT, modelProvider, form, template, contentRepository, servletContext, settings);
        Assert.assertNotNull(resource);
        Assert.assertNotNull(resource.getInputStream());
        Assert.assertTrue(IOUtils.toString(resource.getInputStream()).length() > 10);
    }

    @Test
    public void testScriptResourceForFormWithRemoteDisposition() throws NotFoundError, IOException {
        // Generate a GUID for the host uri to minimize the chances of a random match
        String hostUri = "http://" + UUID.randomUUID().toString() + ".edu";

        Mockito.doReturn(getWorkingDirectory())
                .when(settings).getAssetsDirectoryPath();
        Mockito.doReturn(URI.create(hostUri))
                .when(disposition).getHostUri();
        Mockito.doReturn(FormDisposition.FormDispositionType.REMOTE)
                .when(disposition).getType();
        Mockito.doReturn(disposition)
                .when(form).getDisposition();

        ProcessDeploymentProvider modelProvider = null;
        String templateName = UserInterfaceUtility.templateName(Form.class, null);
        File templatesDirectory = null;
        Resource template = UserInterfaceUtility.template(templatesDirectory, templateName);
        Resource resource = UserInterfaceUtility.resource(CacheName.SCRIPT, modelProvider, form, template, contentRepository, servletContext, settings);
        Assert.assertNotNull(resource);
        Assert.assertNotNull(resource.getInputStream());

        String scriptResourceContent = IOUtils.toString(resource.getInputStream(), "UTF-8");
        Assert.assertTrue(scriptResourceContent.length() > 10);
        Assert.assertTrue(scriptResourceContent.contains(hostUri));
        // TODO: Figure out why the script resource content length is always 4 bytes longer... extra whitespace?
        //Assert.assertEquals(scriptResourceContent.length()+4, UserInterfaceUtility.resourceSize(resource));
    }

    private String getWorkingDirectory() {
        File workingDirectory = new File(".");
        if (workingDirectory.getAbsolutePath().endsWith("core/."))
            workingDirectory = new File(workingDirectory, "../web/src/main/webapp");
        else
            workingDirectory = new File("web/src/main/webapp");
        return workingDirectory.getAbsolutePath();
    }


    @Test
    public void testStylesheetResource() throws NotFoundError, IOException {
        String hostUri = "http://" + UUID.randomUUID().toString() + ".edu";

        Mockito.doReturn(getWorkingDirectory())
                .when(settings).getAssetsDirectoryPath();
        Mockito.doReturn(URI.create(hostUri))
                .when(disposition).getHostUri();
        Mockito.doReturn(FormDisposition.FormDispositionType.REMOTE)
                .when(disposition).getType();
        Mockito.doReturn(disposition)
                .when(form).getDisposition();

        ProcessDeploymentProvider modelProvider = null;
        String templateName = UserInterfaceUtility.templateName(Form.class, null);
        File templatesDirectory = null;
        Resource template = UserInterfaceUtility.template(templatesDirectory, templateName);
        Resource resource = UserInterfaceUtility.resource(CacheName.STYLESHEET, modelProvider, form, template, contentRepository, servletContext, settings);
        Assert.assertNotNull(resource);
        Assert.assertNotNull(resource.getInputStream());

        String scriptResourceContent = IOUtils.toString(resource.getInputStream(), "UTF-8");
        Assert.assertTrue(scriptResourceContent.length() > 10);
        Assert.assertFalse(scriptResourceContent.contains(hostUri));
        Assert.assertEquals(scriptResourceContent.length(), UserInterfaceUtility.resourceSize(resource));
    }

}
