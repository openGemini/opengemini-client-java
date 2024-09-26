package io.opengemini.client.impl;

import io.github.shoothzj.http.facade.client.HttpClientConfig;
import io.github.shoothzj.http.facade.client.HttpClientEngine;
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
        engines.add(HttpClientEngine.AsyncHttpClient);
        engines.add(HttpClientEngine.JDK);
        engines.add(HttpClientEngine.JDK8);
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
                    .httpConfig(httpConfig)
                    .build();
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
