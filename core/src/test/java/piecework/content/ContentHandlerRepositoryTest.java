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
import piecework.content.concrete.ContentHandlerRegistry;
import piecework.content.concrete.GridFSContentProviderReceiver;
import piecework.content.concrete.InMemoryContentProviderReceiver;
import piecework.content.config.ContentConfiguration;
import piecework.content.stubs.*;
import piecework.enumeration.Scheme;
import piecework.model.Content;
import piecework.model.Process;
import piecework.model.User;
import piecework.submission.config.SubmissionConfiguration;

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
    public void testContentProviderByKey() throws IOException {
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY, "some-key");
        Assert.assertEquals(3, contentProviders.size());
        ContentProvider provider = contentProviders.get(0);
        Assert.assertTrue(provider instanceof TestKeyContentProvider);
        Content content = provider.findByPath(null, null, null);
        Assert.assertEquals("some-key-content-provider", content.getLocation());
    }

    @Test
    public void testContentProviderFullByKey() throws IOException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .contentReceiverKey("some-key")
                .build();
        Content content = contentHandlerRepository.findByLocation(process, "some/location");
        Assert.assertEquals("some-key-content-provider", content.getLocation());
    }

    @Test
    public void testContentProviderFullWithoutKey() throws IOException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();
        Content content = contentHandlerRepository.findByLocation(process, "some/location");
        Assert.assertEquals("some-external-content-provider", content.getLocation());
    }

    @Test
    public void testContentReceiver() {
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        Assert.assertTrue(registry.primaryReceiver() instanceof TestExternalContentReceiver);
        Set<ContentReceiver> backupReceivers = registry.backupReceivers();
        Assert.assertTrue(backupReceivers.isEmpty());
    }

    @Test
    public void testContentReceiverFullByKey() throws IOException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .contentReceiverKey("some-key")
                .build();
        Content content = new Content.Builder()
                .build();

        Content stored = contentHandlerRepository.save(process, content, null);
        Assert.assertEquals("some-key-content-receiver", stored.getLocation());
    }

    @Test
    public void testContentReceiverFullWithoutKey() throws IOException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();
        Content content = new Content.Builder()
                .build();

        Content stored = contentHandlerRepository.save(process, content, null);
        Assert.assertEquals("some-external-content-receiver", stored.getLocation());
    }

    @Test
    public void testContentReceiverByKey() throws IOException {
        ContentHandlerRegistry registry = contentHandlerRepository.getContentHandlerRegistry();
        ContentReceiver contentReceiver = registry.contentReceiver("some-key");
        Assert.assertTrue(contentReceiver instanceof TestKeyContentReceiver);
        Content content = new Content.Builder()
                .build();
        User principal = new User.Builder()
                .build();
        Content stored = contentReceiver.save(content, principal);

        Assert.assertEquals("some-key-content-receiver", stored.getLocation());
    }

}
