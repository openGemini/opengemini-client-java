package io.opengemini.client.asynchttpclient;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.common.BaseAsyncClient;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

public class OpenGeminiAsyncHttpClient extends BaseAsyncClient {

    public OpenGeminiAsyncHttpClient(Configuration conf) {
        super(conf);
    }

    @SneakyThrows
    @Override
    protected CompletableFuture<QueryResult> executeQuery(Query query) {
        throw new OpenGeminiException("not support yet");
    }

    @SneakyThrows
    @Override
    protected CompletableFuture<QueryResult> executePostQuery(Query query) {
        throw new OpenGeminiException("not support yet");
    }

    @SneakyThrows
    @Override
    protected CompletableFuture<Void> executeWrite(String database, String lineProtocol) {
        throw new OpenGeminiException("not support yet");
    }

    @Override
    public void close() {
        // not support yet
    }
}
