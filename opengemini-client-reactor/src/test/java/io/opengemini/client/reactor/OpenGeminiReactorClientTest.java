package io.opengemini.client.reactor;

import io.github.shoothzj.http.facade.client.HttpClientConfig;
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
