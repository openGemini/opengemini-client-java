package io.opengemini.client.jdk;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.OpenGeminiClient;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.SslContextUtil;
import io.opengemini.client.api.TlsConfig;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class OpenGeminiJdkClient implements OpenGeminiClient {

    private final Configuration conf;

    private final List<String> serverUrls = new ArrayList<>();

    private final HttpClient client;

    public OpenGeminiJdkClient(Configuration conf) {
        this.conf = conf;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1);
        String httpPrefix;
        if (conf.isTlsEnabled()) {
            TlsConfig tlsConfig = conf.getTlsConfig();
            builder = builder.sslContext(SslContextUtil.buildSSLContextFromJks(
                    tlsConfig.getKeyStorePath(),
                    tlsConfig.getKeyStorePassword(),
                    tlsConfig.getTrustStorePath(),
                    tlsConfig.getTrustStorePassword(),
                    tlsConfig.isTlsVerificationDisabled()));
            httpPrefix = "https://";
        } else {
            httpPrefix = "http://";
        }
        for (Address address : conf.getAddresses()) {
            this.serverUrls.add(httpPrefix + address.getHost() + ":" + address.getPort());
        }
        this.client = builder.build();
    }

    @Override
    public void ping(int idx) throws Exception {

    }

    @Override
    public QueryResult query(Query query) throws Exception {
        return null;
    }
}
