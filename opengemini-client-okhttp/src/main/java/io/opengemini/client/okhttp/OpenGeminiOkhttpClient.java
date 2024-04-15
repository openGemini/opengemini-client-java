package io.opengemini.client.okhttp;

import io.opengemini.client.api.TlsConfig;
import io.opengemini.client.common.BaseClient;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

import java.util.Collections;

public class OpenGeminiOkhttpClient extends BaseClient {
    private final OkHttpClient okHttpClient;

    public OpenGeminiOkhttpClient(Configuration conf) {
        super(conf);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().connectTimeout(conf.getConnectTimeout())
                .readTimeout(conf.getTimeout()).writeTimeout(conf.getTimeout());
        if (conf.isTlsEnabled()) {
            TlsConfig tlsConfig = conf.getTlsConfig();

            // set tls version and cipher suits
            ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(
                    tlsConfig.tlsVersions).cipherSuites(tlsConfig.tlsCipherSuites).build();
            okHttpClientBuilder.connectionSpecs(Collections.singletonList(connectionSpec));

            // create ssl context from keystore and truststore
            OkHttpSslContextFactory.OkHttpSslContext sslContext = OkHttpSslContextFactory.createOkHttpSslContext(
                    tlsConfig);
            okHttpClientBuilder.sslSocketFactory(sslContext.sslSocketFactory, sslContext.x509TrustManager);

            // override hostnameVerifier to make it always success when hostname verification has been disabled
            if (tlsConfig.tlsHostnameVerifyDisabled) {
                okHttpClientBuilder.hostnameVerifier((s, sslSession) -> true);
            }
        }
        okHttpClient = okHttpClientBuilder.build();
    }
}
