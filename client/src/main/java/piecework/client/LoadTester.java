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
package piecework.client;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import piecework.config.ApplicationConfiguration;
import piecework.security.SecuritySettings;
import piecework.util.KeyManagerCabinet;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author James Renfro
 */
public class LoadTester {

    private final HttpClient client;

    public LoadTester(KeyStore keystore, SecuritySettings securitySettings) {
        ClientConnectionManager cm;
        try {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(
                    new Scheme("https", 8443, new SSLSocketFactory(keystore, new String(securitySettings.getKeystorePassword()))));

            cm = new PoolingClientConnectionManager(schemeRegistry);
        } catch (Exception e) {
            cm = new BasicClientConnectionManager();
        }
        this.client = new DefaultHttpClient(cm);
    }

    public void retrieveAllTasks() throws Exception {
        HttpGet get = new HttpGet("https://localhost:8443/piecework/secure/form.json");
        HttpResponse response = client.execute(get);
        System.out.println(response.getStatusLine());
    }

    public static final void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
//        context.setEnvironment(environment);

        KeyManagerCabinet cabinet = context.getBean(KeyManagerCabinet.class);
        SecuritySettings securitySettings = context.getBean(SecuritySettings.class);

        LoadTester loadTester = new LoadTester(cabinet.getKeystore(), securitySettings);
        loadTester.retrieveAllTasks();
    }

}
