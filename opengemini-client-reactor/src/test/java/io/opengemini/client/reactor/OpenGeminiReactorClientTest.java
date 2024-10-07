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

import io.github.openfacade.http.HttpClientConfig;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

class OpenGeminiReactorClientTest {
    private OpenGeminiReactorClient reactorClient;

    @BeforeEach
    void setUp() {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(3))
                .build();
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .httpConfig(httpConfig)
                .build();
        this.reactorClient = new OpenGeminiReactorClient(configuration);
    }

    @Test
    void queryWithWrongAddress() {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(3))
                .build();
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 28086)))
                .httpConfig(httpConfig)
                .build();

        OpenGeminiReactorClient wrongClient = new OpenGeminiReactorClient(configuration);
        Query showDatabasesQuery = new Query("show databases");
        Mono<QueryResult> result = wrongClient.query(showDatabasesQuery);
        Assertions.assertThrows(RuntimeException.class, result::block);
    }

    @Test
    void testShowDatabases() {
        Query showDatabasesQuery = new Query("show databases");
        Mono<QueryResult> result = reactorClient.query(showDatabasesQuery);
        Assertions.assertEquals(1, result.block().getResults().size());
    }
}
