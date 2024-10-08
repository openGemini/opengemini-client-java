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

import io.github.openfacade.http.HttpClientEngine;
import io.opengemini.client.api.AuthType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration properties for OpenGemini.
 */
@ConfigurationProperties("spring.opengemini")
@Getter
@Setter
public class OpenGeminiProperties {

    private List<String> addresses = new ArrayList<>(Collections.singletonList("localhost:8086"));

    @NestedConfigurationProperty
    private Auth auth;

    @NestedConfigurationProperty
    private Batch batch;

    private Boolean gzipEnabled;

    @NestedConfigurationProperty
    private Http http = new Http();

    @Getter
    @Setter
    public static class Auth {
        private AuthType type;

        private String username;

        private String password;

        private String token;
    }

    @Getter
    @Setter
    public static class Batch {
        private Integer batchInterval;

        private Integer batchSize;
    }

    @Getter
    @Setter
    public static class Http {
        private HttpClientEngine engine = HttpClientEngine.OkHttp;

        private Duration timeout;

        private Duration connectTimeout;

        private Ssl ssl;

        private OkHttp okHttp;

        @Getter
        @Setter
        public static class Ssl {
            private String keyStoreLocation;

            private String keyStorePassword;

            private String trustStoreLocation;

            private String trustStorePassword;

            private Boolean verifyDisabled;

            private Boolean hostnameVerifyDisabled;

            private String[] versions;

            private String[] cipherSuites;
        }

        @Getter
        @Setter
        public static class OkHttp {
            private Boolean retryOnConnectionFailure;

            private Integer maxIdleConnections;

            private Duration keepAliveDuration;
        }
    }

}
