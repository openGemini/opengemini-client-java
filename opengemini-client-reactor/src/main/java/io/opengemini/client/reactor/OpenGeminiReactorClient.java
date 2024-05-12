package io.opengemini.client.reactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.TlsConfig;
import io.opengemini.client.common.BaseClient;
import io.opengemini.client.common.JacksonService;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientSecurityUtils;

public class OpenGeminiReactorClient extends BaseClient {
    private final HttpClient client;

    public OpenGeminiReactorClient(Configuration conf) {
        super(conf);
        HttpClient client = HttpClient.create();
        client = client.responseTimeout(conf.getTimeout());
        int connectionTimeoutMs = (int) conf.getConnectTimeout().toMillis();
        client = client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMs);

        if (conf.isTlsEnabled()) {
            TlsConfig tlsConfig = conf.getTlsConfig();
            client = client.secure(spec -> {
                SslContext context = SslContextUtil.buildFromJks(
                        tlsConfig.getKeyStorePath(),
                        tlsConfig.getKeyStorePassword(),
                        tlsConfig.getTrustStorePath(),
                        tlsConfig.getTrustStorePassword(),
                        tlsConfig.isTlsVerifyDisabled(),
                        tlsConfig.getTlsVersions(),
                        tlsConfig.getTlsCipherSuites());
                if (tlsConfig.isTlsHostnameVerifyDisabled()) {
                    spec.sslContext(context)
                            .handlerConfigurator(HttpClientSecurityUtils.HOSTNAME_VERIFICATION_CONFIGURER);
                } else {
                    spec.sslContext(context);
                }
            });
        }
        this.client = client;
    }

    Mono<QueryResult> query(Query query) {
        String queryUrl = getQueryUrl(query);
        return get(queryUrl, QueryResult.class);
    }

    public <T> Mono<T> get(String path, Class<T> type) {
        return handleResponse(client.get().uri(nextUrlPrefix() + path))
                .flatMap(jsonString -> {
                    try {
                        return Mono.just(JacksonService.toObject(jsonString, type));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    public Mono<String> get(String path) {
        return handleResponse(client.get().uri(nextUrlPrefix() + path));
    }

    public <T> Mono<T> post(String path, String requestBody, Class<T> type) {
        return handleResponse(client.post()
                .uri(nextUrlPrefix() + path)
                .send(ByteBufMono.fromString(Mono.just(requestBody))))
                .flatMap(jsonString -> {
                    try {
                        return Mono.just(JacksonService.toObject(jsonString, type));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    public Mono<String> post(String path, String requestBody) {
        return handleResponse(client.post()
                .uri(nextUrlPrefix() + path)
                .send(ByteBufMono.fromString(Mono.just(requestBody))));
    }

    public <T> Mono<T> put(String path, String requestBody, Class<T> type) {
        return handleResponse(client.put()
                .uri(nextUrlPrefix() + path)
                .send(ByteBufMono.fromString(Mono.just(requestBody))))
                .flatMap(jsonString -> {
                    try {
                        return Mono.just(JacksonService.toObject(jsonString, type));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    public Mono<String> put(String path, String requestBody) {
        return handleResponse(client.put()
                .uri(nextUrlPrefix() + path)
                .send(ByteBufMono.fromString(Mono.just(requestBody))));
    }

    public <T> Mono<T> delete(String path, Class<T> type) {
        return handleResponse(client.delete().uri(nextUrlPrefix() + path))
                .flatMap(jsonString -> {
                    try {
                        return Mono.just(JacksonService.toObject(jsonString, type));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    public Mono<String> delete(String path) {
        return handleResponse(client.delete().uri(nextUrlPrefix() + path));
    }

    private Mono<String> handleResponse(HttpClient.ResponseReceiver<?> responseReceiver) {
        return responseReceiver.responseSingle((response, content) -> {
            int code = response.status().code();
            if (code >= 200 && code < 300) {
                return content.asString();
            } else {
                return content.asString()
                        .flatMap(body -> Mono.error(new OpenGeminiException(body, code)));
            }
        });
    }
}
