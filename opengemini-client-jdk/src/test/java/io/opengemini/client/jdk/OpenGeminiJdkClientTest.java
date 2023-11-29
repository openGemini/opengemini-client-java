package io.opengemini.client.jdk;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class OpenGeminiJdkClientTest {

    private OpenGeminiJdkClient openGeminiJdkClient;

    @BeforeEach
    void setUp() {
        Configuration configuration = Configuration.builder().
                addresses(Collections.singletonList(new Address("127.0.0.1", 8086))).
                build();
        this.openGeminiJdkClient = new OpenGeminiJdkClient(configuration);
    }

    @Test
    void queryWithWrongAddress() throws Exception {
        Configuration configuration = Configuration.builder().
                addresses(Collections.singletonList(new Address("127.0.0.1", 28086))).
                build();

        OpenGeminiJdkClient openGeminiJdkClientConfused = new OpenGeminiJdkClient(configuration);
        Query showDatabasesQuery = new Query("show databases");
        CompletableFuture<QueryResult> rstFuture = openGeminiJdkClientConfused.query(showDatabasesQuery);
        Assertions.assertThrows(ExecutionException.class, rstFuture::get);
    }

    @Test
    void testShowDatabases() throws Exception {
        Query showDatabasesQuery = new Query("show databases");
        CompletableFuture<QueryResult> rstFuture = openGeminiJdkClient.query(showDatabasesQuery);
        QueryResult rst = rstFuture.get();
        Assertions.assertEquals(1, rst.getResults().size());
    }
}
