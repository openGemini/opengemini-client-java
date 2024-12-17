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

package io.opengemini.client.spring.data.core;

import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.TlsConfig;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.BatchConfig;
import io.opengemini.client.api.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpenGeminiPropertiesConverter {
    private final OpenGeminiProperties properties;

    private final ObjectProvider<ClientConfigurationBuilderCustomizer> customizers;

    public OpenGeminiPropertiesConverter(OpenGeminiProperties properties,
                                         ObjectProvider<ClientConfigurationBuilderCustomizer> customizers) {
        this.properties = properties;
        this.customizers = customizers;
    }

    public Configuration toConfiguration() {
        Configuration.ConfigurationBuilder builder = Configuration.builder();
        builder.addresses(properties.getAddresses().stream().map(this::toAddress).collect(Collectors.toList()));
        Optional.ofNullable(properties.getAuth()).map(this::toAuthConfig).ifPresent(builder::authConfig);
        Optional.ofNullable(properties.getBatch()).map(this::toBatchConfig).ifPresent(builder::batchConfig);
        Optional.ofNullable(properties.getGzipEnabled()).ifPresent(builder::gzipEnabled);
        builder.httpConfig(toHttpClientConfig(properties.getHttp()));

        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder.build();
    }

    private Address toAddress(String address) {
        String[] strings = StringUtils.split(address, ':');
        return new Address(strings[0], Integer.parseInt(strings[1]));
    }

    @NotNull
    private AuthConfig toAuthConfig(OpenGeminiProperties.Auth auth) {
        AuthConfig authConfig = new AuthConfig();
        authConfig.setAuthType(auth.getType());
        authConfig.setUsername(auth.getUsername());
        if (StringUtils.isNotBlank(auth.getPassword())) {
            authConfig.setPassword(auth.getPassword().toCharArray());
        }
        authConfig.setToken(auth.getToken());
        return authConfig;
    }

    @NotNull
    public BatchConfig toBatchConfig(OpenGeminiProperties.Batch batch) {
        BatchConfig batchConfig = new BatchConfig();
        Optional.ofNullable(batch.getBatchInterval()).ifPresent(batchConfig::setBatchInterval);
        Optional.ofNullable(batch.getBatchSize()).ifPresent(batchConfig::setBatchSize);
        return batchConfig;
    }

    @NotNull
    public HttpClientConfig toHttpClientConfig(OpenGeminiProperties.Http http) {
        HttpClientConfig.Builder builder = new HttpClientConfig.Builder();
        builder.engine(http.getEngine());
        builder.timeout(http.getTimeout());
        builder.connectTimeout(http.getConnectTimeout());
        Optional.ofNullable(http.getSsl()).map(this::toTlsConfig).ifPresent(builder::tlsConfig);
        Optional.ofNullable(http.getOkHttp()).map(this::toOkHttpConfig).ifPresent(builder::okHttpConfig);
        return builder.build();
    }

    @NotNull
    public TlsConfig toTlsConfig(OpenGeminiProperties.Http.Ssl ssl) {
        TlsConfig.Builder builder = new TlsConfig.Builder();
        String keyStoreLocation = ssl.getKeyStoreLocation();
        if (StringUtils.isNotBlank(keyStoreLocation)) {
            char[] password = Optional.ofNullable(ssl.getKeyStorePassword())
                    .filter(StringUtils::isNotBlank)
                    .map(String::toCharArray)
                    .orElse(null);
            builder.keyStore(keyStoreLocation, password);
        }
        String trustStoreLocation = ssl.getTrustStoreLocation();
        if (StringUtils.isNotBlank(trustStoreLocation)) {
            char[] password = Optional.ofNullable(ssl.getTrustStorePassword())
                    .filter(StringUtils::isNotBlank)
                    .map(String::toCharArray)
                    .orElse(null);
            builder.keyStore(trustStoreLocation, password);
        }
        Optional.ofNullable(ssl.getVerifyDisabled()).ifPresent(builder::verifyDisabled);
        Optional.ofNullable(ssl.getHostnameVerifyDisabled()).ifPresent(builder::hostnameVerifyDisabled);
        builder.versions(ssl.getVersions());
        builder.cipherSuites(ssl.getCipherSuites());
        return builder.build();
    }

    @NotNull
    public HttpClientConfig.OkHttpConfig toOkHttpConfig(OpenGeminiProperties.Http.OkHttp okHttp) {
        HttpClientConfig.OkHttpConfig okHttpConfig = new HttpClientConfig.OkHttpConfig();
        Optional.ofNullable(okHttp.getRetryOnConnectionFailure()).ifPresent(okHttpConfig::setRetryOnConnectionFailure);
        Integer maxIdleConnections = okHttp.getMaxIdleConnections();
        Duration keepAliveDuration = okHttp.getKeepAliveDuration();
        if (maxIdleConnections != null && maxIdleConnections > 0 && keepAliveDuration != null) {
            HttpClientConfig.OkHttpConfig.ConnectionPoolConfig poolConfig =
                    new HttpClientConfig.OkHttpConfig.ConnectionPoolConfig();
            poolConfig.setMaxIdleConnections(maxIdleConnections);
            poolConfig.setKeepAliveDuration(keepAliveDuration);
            okHttpConfig.setConnectionPoolConfig(poolConfig);
        }
        return okHttpConfig;
    }
}
