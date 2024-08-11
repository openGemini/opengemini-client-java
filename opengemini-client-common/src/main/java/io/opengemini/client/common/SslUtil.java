package io.opengemini.client.common;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class SslUtil {

    public static @NotNull KeyStore loadKeyStore(String keyStorePath, char[] keyStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream keyStoreFile = new FileInputStream(keyStorePath)) {
            keyStore.load(keyStoreFile, keyStorePassword);
        }
        return keyStore;
    }


    public static @NotNull KeyManagerFactory initKeyManagerFactory(String keyStorePath, char[] keyStorePassword)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException,
            IOException {
        KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePassword);

        // Set up key manager factory to use the key store
        String defaultKeyAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultKeyAlgorithm);
        keyManagerFactory.init(keyStore, keyStorePassword);
        return keyManagerFactory;
    }

    public static @NotNull TrustManagerFactory initTrustManagerFactory(String trustStorePath, char[] trustStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore = loadKeyStore(trustStorePath, trustStorePassword);

        // Set up trust manager factory to use the trust store
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory;
    }
}
