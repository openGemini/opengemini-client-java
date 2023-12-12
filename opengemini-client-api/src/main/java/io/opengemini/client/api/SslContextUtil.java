package io.opengemini.client.api;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

public class SslContextUtil {

    public static SSLContext buildSSLContextFromJks(String keyStorePath,
                                              String keyStorePassword,
                                              String trustStorePath,
                                              String trustStorePassword,
                                              boolean disableSslVerify) {
        try {
            // Load the key store
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream keyStoreFile = new FileInputStream(keyStorePath)) {
                keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
            }

            // Set up key manager factory to use our key store
            String defaultKeyAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultKeyAlgorithm);
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            // Load the trust store, if specified
            TrustManagerFactory trustManagerFactory = null;
            if (trustStorePath != null) {
                KeyStore trustStore = KeyStore.getInstance("JKS");
                try (FileInputStream trustStoreFile = new FileInputStream(trustStorePath)) {
                    trustStore.load(trustStoreFile, trustStorePassword.toCharArray());
                }

                // Set up trust manager factory to use our trust store
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
            }

            // Set up SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManager[] trustManagers;
            if (disableSslVerify) {
                trustManagers = new TrustManager[] { new InsecureTrustManager() };
            } else if (trustManagerFactory != null) {
                trustManagers = trustManagerFactory.getTrustManagers();
            } else {
                trustManagers = null;
            }

            // Set up SSL parameters
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, new SecureRandom());

            if (disableSslVerify) {
                sslContext.getDefaultSSLParameters().setEndpointIdentificationAlgorithm(null);
            }

            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
