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

package io.opengemini.client.impl;

import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.HttpClientEngine;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenGeminiWrongAddressTest {
    private final List<OpenGeminiClient> clients = new ArrayList<>();

    protected List<OpenGeminiClient> clientList() throws OpenGeminiException {
        List<HttpClientEngine> engines = new ArrayList<>();
        engines.add(HttpClientEngine.Async);
        engines.add(HttpClientEngine.Java);
        engines.add(HttpClientEngine.Java8);
        engines.add(HttpClientEngine.OkHttp);
        List<OpenGeminiClient> clients = new ArrayList<>();
        for (HttpClientEngine engine : engines) {
            HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                    .engine(engine)
                    .connectTimeout(Duration.ofSeconds(3))
                    .timeout(Duration.ofSeconds(3))
                    .build();
            Configuration configuration = Configuration.builder()
                    .addresses(Collections.singletonList(new Address("127.0.0.1", 28086)))
                    .httpConfig(httpConfig).gzipEnabled(false).build();
            clients.add(OpenGeminiClientFactory.create(configuration));
        }
        return clients;
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void queryWithWrongAddress(OpenGeminiClient client) {
        Query showDatabasesQuery = new Query("SHOW DATABASES");
        CompletableFuture<QueryResult> rstFuture = client.query(showDatabasesQuery);
        Assertions.assertThrows(ExecutionException.class, rstFuture::get);
    }

    @AfterAll
    void closeClients() {
        for (OpenGeminiClient client : clients) {
            try {
                client.close();
            } catch (IOException e) {
                // ignore exception
            }
        }
    }
}
