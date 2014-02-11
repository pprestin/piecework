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

import org.apache.commons.lang.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import piecework.content.ContentProvider;
import piecework.content.ContentProviderVoter;
import piecework.content.ContentReceiver;
import piecework.content.ContentReceiverVoter;
import piecework.enumeration.ContentHandlerPriority;
import piecework.enumeration.Scheme;
import piecework.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
public class ContentHandlerRegistryTest {

    @Test
    public void testContentProviderOnePrimaryOneBackup() {
        ContentHandlerRegistry registry = new ContentHandlerRegistry(new TestContentProviderVoter(), null);
        registry.registerProviders(new InMemoryContentProviderReceiver(), new TestExternalContentProvider(), new GridFSContentProviderReceiver());
        registry.init();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY);
        Assert.assertEquals(2, contentProviders.size());
        ContentProvider first = contentProviders.get(0);
        ContentProvider second = contentProviders.get(1);
        Assert.assertTrue(first instanceof TestExternalContentProvider);
        Assert.assertTrue(second instanceof InMemoryContentProviderReceiver);
    }

    @Test
    public void testContentProviderOnePrimaryNoBackups() {
        ContentHandlerRegistry registry = new ContentHandlerRegistry(new TestContentProviderVoter(), null);
        registry.registerProviders(new TestExternalContentProvider(), new GridFSContentProviderReceiver());
        registry.init();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY);
        Assert.assertEquals(1, contentProviders.size());
        ContentProvider first = contentProviders.get(0);
        Assert.assertTrue(first instanceof TestExternalContentProvider);
    }

    @Test
    public void testContentProviderNoPrimaryOneBackup() {
        ContentHandlerRegistry registry = new ContentHandlerRegistry(new TestContentProviderVoter(), null);
        registry.registerProviders(new InMemoryContentProviderReceiver(), new GridFSContentProviderReceiver());
        registry.init();
        List<ContentProvider> contentProviders = registry.providers(Scheme.REPOSITORY);
        Assert.assertEquals(0, contentProviders.size());
    }

    @Test
    public void testContentReceiver() {
        ContentHandlerRegistry registry = new ContentHandlerRegistry(null, new TestContentReceiverVoter());
        registry.registerReceivers(new InMemoryContentProviderReceiver(), new GridFSContentProviderReceiver(), new TestExternalContentReceiver());
        registry.init();
        Assert.assertTrue(registry.primaryReceiver() instanceof TestExternalContentReceiver);
        Set<ContentReceiver> backupReceivers = registry.backupReceivers();
        Assert.assertEquals(1, backupReceivers.size());
        ContentReceiver backupReceiver = backupReceivers.iterator().next();
        Assert.assertTrue(backupReceiver instanceof GridFSContentProviderReceiver);
    }

    @Test(expected = RuntimeException.class)
    public void testUninitializedRegistry() {
        ContentHandlerRegistry registry = new ContentHandlerRegistry(new TestContentProviderVoter(), null);
        registry.registerProviders(new InMemoryContentProviderReceiver(), new GridFSContentProviderReceiver());
        registry.providers(Scheme.REPOSITORY);
    }


    public class TestExternalContentProvider implements ContentProvider {

        @Override
        public Content findByPath(piecework.model.Process process, String base, String location) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public Scheme getScheme() {
            return Scheme.REPOSITORY;
        }

        @Override
        public String getKey() {
            return null;
        }
    }

    public class TestExternalContentReceiver implements ContentReceiver {

        @Override
        public Content save(Content content) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public String getKey() {
            return null;
        }

    }

    public class TestContentProviderVoter implements ContentProviderVoter {

        @Override
        public <P extends ContentProvider> ContentHandlerPriority vote(P provider) {
            if (provider instanceof TestExternalContentProvider)
                return ContentHandlerPriority.PRIMARY;
            if (provider instanceof InMemoryContentProviderReceiver)
                return ContentHandlerPriority.BACKUP;
            return ContentHandlerPriority.IGNORE;
        }

    }

    public class TestContentReceiverVoter implements ContentReceiverVoter {
        @Override
        public <R extends ContentReceiver> ContentHandlerPriority vote(R receiver) {
            if (receiver instanceof TestExternalContentReceiver)
                return ContentHandlerPriority.PRIMARY;
            if (receiver instanceof GridFSContentProviderReceiver)
                return ContentHandlerPriority.BACKUP;
            return ContentHandlerPriority.IGNORE;
        }
    }

}
