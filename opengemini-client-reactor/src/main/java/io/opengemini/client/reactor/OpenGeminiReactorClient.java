package io.opengemini.client.reactor;

import io.netty.handler.ssl.SslContext;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.TlsConfig;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientSecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OpenGeminiReactorClient {
    private final HttpClient client;

    private final List<String> serverUrls = new ArrayList<>();

    private final AtomicInteger prevIndex = new AtomicInteger(-1);

    public OpenGeminiReactorClient(Configuration conf) {
        HttpClient client = HttpClient.create();

        String httpPrefix;
        if (conf.isTlsEnabled()) {
            TlsConfig tlsConfig = conf.getTlsConfig();
            client = client.secure(spec -> {
                SslContext context = SslContextUtil.buildFromJks(
                        tlsConfig.getKeyStorePath(),
                        tlsConfig.getKeyStorePassword(),
                        tlsConfig.getTrustStorePath(),
                        tlsConfig.getTrustStorePassword(),
                        tlsConfig.isTlsVerificationDisabled(),
                        tlsConfig.getTlsVersions(),
                        tlsConfig.getTlsCipherSuites());
                if (tlsConfig.isTlsHostnameVerificationDisabled()) {
                    spec.sslContext(context)
                            .handlerConfigurator(HttpClientSecurityUtils.HOSTNAME_VERIFICATION_CONFIGURER);
                } else {
                    spec.sslContext(context);
                }
            });
            httpPrefix = "https://";
        } else {
            httpPrefix = "http://";
        }
        for (Address address : conf.getAddresses()) {
            this.serverUrls.add(httpPrefix + address.getHost() + ":" + address.getPort());
        }
        this.client = client;
    }
}
