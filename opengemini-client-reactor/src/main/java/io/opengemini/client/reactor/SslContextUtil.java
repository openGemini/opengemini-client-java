package io.opengemini.client.reactor;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.opengemini.client.common.SslUtil;

import java.util.Arrays;

public class SslContextUtil {
    public static SslContext buildFromJks(String keyStorePath,
                                          char[] keyStorePassword,
                                          String trustStorePath,
                                          char[] trustStorePassword,
                                          boolean disableSslVerify,
                                          String[] tlsProtocols,
                                          String[] tlsCiphers) {

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

            if (keyStorePath != null && keyStorePassword != null) {
                sslContextBuilder.keyManager(SslUtil.initKeyManagerFactory(keyStorePath, keyStorePassword));
            }

            if (disableSslVerify) {
                sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            } else if (trustStorePath != null && trustStorePassword != null) {
                sslContextBuilder.trustManager(SslUtil.initTrustManagerFactory(trustStorePath, trustStorePassword));
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
