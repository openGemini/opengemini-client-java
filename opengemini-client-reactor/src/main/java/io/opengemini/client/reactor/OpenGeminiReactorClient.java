/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opengemini.client.reactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.openfacade.http.TlsConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.common.BaseClient;
import io.opengemini.client.common.JacksonService;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientSecurityUtils;

public class OpenGeminiReactorClient extends BaseClient {
    private final HttpClient client;

    public OpenGeminiReactorClient(@NotNull Configuration conf) {
        super(conf);
        HttpClient client = HttpClient.create();
        client = client.responseTimeout(conf.getHttpConfig().timeout());
        int connectionTimeoutMs = (int) conf.getHttpConfig().connectTimeout().toMillis();
        client = client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMs);

        if (conf.getHttpConfig().tlsConfig() != null) {
            TlsConfig tlsConfig = conf.getHttpConfig().tlsConfig();
            client = client.secure(spec -> {
                SslContext context = SslContextUtil.buildFromJks(
                        tlsConfig.keyStorePath(),
                        tlsConfig.keyStorePassword(),
                        tlsConfig.trustStorePath(),
                        tlsConfig.trustStorePassword(),
                        tlsConfig.verifyDisabled(),
                        tlsConfig.versions(),
                        tlsConfig.cipherSuites());
                if (tlsConfig.hostnameVerifyDisabled()) {
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
