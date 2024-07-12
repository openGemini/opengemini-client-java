package io.opengemini.client.okhttp;

import io.opengemini.client.api.InsecureTrustManager;
import io.opengemini.client.api.TlsConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

class OkHttpSslContextFactory {
    static OkHttpSslContext createOkHttpSslContext(TlsConfig tlsConfig) {
        try {
            // get key manager for client certificate auth
            KeyManager[] keyManagers = getKeyManagers(tlsConfig.keyStorePath, tlsConfig.keyStorePassword);

            // get trust manager for server certificate auth
            TrustManager[] trustManagers = getTrustManagers(tlsConfig.trustStorePath, tlsConfig.trustStorePassword,
                    tlsConfig.tlsVerifyDisabled);
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            X509TrustManager x509TrustManager = (X509TrustManager) trustManagers[0];
            return new OkHttpSslContext(sslContext.getSocketFactory(), x509TrustManager);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyManager[] getKeyManagers(String keyStorePath,
                                               String keyStorePassword) {
        try {
            if (keyStorePath == null) {
                return null;
            }

            KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    private static TrustManager[] getTrustManagers(String trustStorePath,
                                                   String trustStorePassword,
                                                   boolean disableSslVerify) {
        try {
            if (disableSslVerify) {
                return new TrustManager[]{new InsecureTrustManager()};
            }

            KeyStore trustStore = loadKeyStore(trustStorePath, trustStorePassword);
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyStore loadKeyStore(String keyStorePath,
                                         String password) {
        try (FileInputStream trustStoreFile = new FileInputStream(keyStorePath)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            char[] charArray = password.toCharArray();
            keyStore.load(trustStoreFile, charArray);
            return keyStore;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Getter
    @AllArgsConstructor
    static class OkHttpSslContext {
        SSLSocketFactory sslSocketFactory;
        X509TrustManager x509TrustManager;
    }
}
