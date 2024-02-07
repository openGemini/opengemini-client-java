package io.opengemini.client.reactor;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Arrays;

public class SslContextUtil {
    public static SslContext buildFromJks(
            String keyStorePath, String keyStorePassword,
            String trustStorePath, String trustStorePassword,
            boolean disableSslVerify, String[] tlsProtocols, String[] tlsCiphers) {

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

            if (keyStorePath != null && keyStorePassword != null) {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                try (FileInputStream keyStoreInputStream = new FileInputStream(keyStorePath)) {
                    keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
                }
                String defaultKeyAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultKeyAlgorithm);
                keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
                sslContextBuilder.keyManager(keyManagerFactory);
            }

            if (disableSslVerify) {
                sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            } else if (trustStorePath != null && trustStorePassword != null) {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                try (FileInputStream trustStoreInputStream = new FileInputStream(trustStorePath)) {
                    trustStore.load(trustStoreInputStream, trustStorePassword.toCharArray());
                }
                String defaultTrustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(defaultTrustAlgorithm);
                trustManagerFactory.init(trustStore);
                sslContextBuilder.trustManager(trustManagerFactory);
            }

            if (tlsProtocols != null) {
                sslContextBuilder.protocols(tlsProtocols);
            }

            if (tlsCiphers != null) {
                sslContextBuilder.ciphers(Arrays.asList(tlsCiphers));
            }

            return sslContextBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error setting up SSL configuration", e);
        }
    }
}
