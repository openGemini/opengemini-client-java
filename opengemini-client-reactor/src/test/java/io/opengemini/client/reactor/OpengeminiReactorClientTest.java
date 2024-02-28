package io.opengemini.client.reactor;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

class OpengeminiReactorClientTest {
    private OpenGeminiReactorClient reactorClient;

    @BeforeEach
    void setUp() {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
                .build();
        this.reactorClient = new OpenGeminiReactorClient(configuration);
    }

    @Test
    void queryWithWrongAddress() {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 28086)))
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
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
