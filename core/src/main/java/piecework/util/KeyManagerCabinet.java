/*
 * Copyright 2012 University of Washington
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

import org.apache.commons.lang.StringUtils;
import piecework.security.SecuritySettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author James Renfro
 */
public class KeyManagerCabinet {

	private static final String[] CIPHER_SUITES = { "SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_DES_CBC_MD5", "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5" };
	private static final List<String> CIPHER_SUITES_LIST;
	
	static {
		CIPHER_SUITES_LIST = Arrays.asList(CIPHER_SUITES);
	}
	
	private KeyManager[] keyManagers;
	private TrustManager[] trustManagers;
	
	private KeyManagerCabinet(KeyManager[] keyManagers, TrustManager[] trustManagers) {
		this.keyManagers = keyManagers;
		this.trustManagers = trustManagers;
	}

	public KeyManager[] getKeyManagers() {
		return keyManagers;
	}

	public TrustManager[] getTrustManagers() {
		return trustManagers;
	}

	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		
		private final SecuritySettings securitySettings;
		private String keystoreType;
		
		public Builder(SecuritySettings securitySettings) {
			this.securitySettings = securitySettings;
			this.keystoreType = "JKS";
		}
		
		public Builder keystoreType(String keystoreType) {
			this.keystoreType = keystoreType;
			return this;
		}
		
		public KeyManagerCabinet build() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
			if (keystoreType == null)
	        	keystoreType = "JKS";

            if (StringUtils.isEmpty(securitySettings.getKeystoreFile()))
                return new KeyManagerCabinet(null, null);

	        KeyStore ks = KeyStore.getInstance(keystoreType);
			FileInputStream fis = new FileInputStream(securitySettings.getKeystoreFile());

			try {
				ks.load(fis, securitySettings.getKeystorePassword());
			} finally {
				if (fis != null)
					fis.close();
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, securitySettings.getKeystorePassword());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init((KeyStore)null);
			
			return new KeyManagerCabinet(kmf.getKeyManagers(), tmf.getTrustManagers());
		}
		
	}

}
