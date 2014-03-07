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
package piecework.content;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.content.concrete.BasicContentResource;
import piecework.content.concrete.ContentHandlerRegistry;
import piecework.content.concrete.InMemoryContentProviderReceiver;
import piecework.content.config.ContentConfiguration;
import piecework.content.stubs.*;
import piecework.enumeration.Scheme;
import piecework.exception.PieceworkException;
import piecework.model.ContentProfile;
import piecework.model.User;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ContentConfiguration.class})
public class ContentHandlerRepositoryTest {

    @Autowired
    ContentHandlerRepository contentHandlerRepository;

    @Test
    public void applicationContext() {
        Assert.assertTrue(true);
    }

    @Test
    public void testContentProviderOnePrimaryOneBackup() {
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY);
        Assert.assertEquals(2, contentProviders.size());
        ContentProvider first = contentProviders.get(0);
        ContentProvider second = contentProviders.get(1);
        Assert.assertTrue(first instanceof TestExternalContentProvider);
        Assert.assertTrue(second instanceof InMemoryContentProviderReceiver);
    }

    @Test
    public void testContentProviderOnePrimaryNoBackups() {
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY);
        Assert.assertEquals(2, contentProviders.size());
        ContentProvider first = contentProviders.get(0);
        ContentProvider second = contentProviders.get(1);
        Assert.assertTrue(first instanceof TestExternalContentProvider);
        Assert.assertTrue(second instanceof InMemoryContentProviderReceiver);
    }

    @Test
    public void testContentProviderTwoPrimaryNoBackup() {
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY);
        Assert.assertEquals(2, contentProviders.size());
    }

    @Test
    public void testContentProviderByKey() throws PieceworkException, IOException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .contentHandlerKey("some-key")
                .build();
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY, "some-key");
        Assert.assertEquals(3, contentProviders.size());
        ContentProvider provider = contentProviders.get(0);
        Assert.assertTrue(provider instanceof TestKeyContentProvider);
        ContentResource contentResource = provider.findByLocation(modelProvider, null);
        Assert.assertEquals("some-key-content-provider", contentResource.getLocation());
    }

    @Test
    public void testContentProviderFullByKey() throws PieceworkException, IOException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .contentHandlerKey("some-key")
                .build();
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        ContentResource contentResource = contentHandlerRepository.findByLocation(modelProvider, "some/location");
        Assert.assertEquals("some-key-content-provider", contentResource.getLocation());
    }

    @Test
    public void testContentProviderFullWithoutKey() throws PieceworkException, IOException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .build();
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        ContentResource contentResource = contentHandlerRepository.findByLocation(modelProvider, "some/location");
        Assert.assertEquals("some-external-content-provider", contentResource.getLocation());
    }

    @Test
    public void testContentReceiver() {
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        Assert.assertTrue(registry.primaryReceiver() instanceof TestExternalContentReceiver);
        Set<ContentReceiver> backupReceivers = registry.backupReceivers();
        Assert.assertTrue(backupReceivers.isEmpty());
    }

    @Test
    public void testContentReceiverSaveByKey() throws PieceworkException, IOException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .contentHandlerKey("some-key")
                .build();
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);

        ContentResource contentResource = new BasicContentResource.Builder()
                .build();

        ContentResource stored = contentHandlerRepository.save(modelProvider, contentResource);
        Assert.assertEquals("some-key-content-receiver", stored.getLocation());
    }

    @Test
    public void testContentReceiverSaveWithoutKey() throws PieceworkException, IOException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .build();
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        ContentResource contentResource = new BasicContentResource.Builder()
                .build();

        ContentResource stored = contentHandlerRepository.save(modelProvider, contentResource);
        Assert.assertEquals("some-external-content-receiver", stored.getLocation());
    }

    @Test
    public void testContentReceiverLookupSaveAndExpireByKey() throws PieceworkException, IOException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .contentHandlerKey("some-key")
                .build();
        ContentProfileProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);

        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        ContentReceiver contentReceiver = registry.contentReceiver("some-key");
        Assert.assertTrue(contentReceiver instanceof TestKeyContentReceiver);
        ContentResource contentResource = new BasicContentResource.Builder()
                .build();
        User principal = new User.Builder()
                .build();
        ContentResource stored = contentReceiver.save(modelProvider, contentResource);

        Assert.assertEquals("some-key-content-receiver", stored.getLocation());

        contentReceiver.expire(modelProvider, stored.getLocation());
    }

}
