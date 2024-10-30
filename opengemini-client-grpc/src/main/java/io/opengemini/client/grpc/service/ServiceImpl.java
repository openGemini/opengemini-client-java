package io.opengemini.client.grpc.service;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.opengemini.client.grpc.RpcClientConnectionManager;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ServiceImpl {
    private final RpcClientConnectionManager connectionManager;

    protected ServiceImpl(final RpcClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public RpcClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    protected <S, T> CompletableFuture<T> execute(
            Supplier<Future<S>> supplier,
            Function<S, T> resultConvert) {

        return doExecute(supplier, resultConvert);
    }

    protected <S, T> CompletableFuture<T> doExecute(
            Supplier<Future<S>> supplier,
            Function<S, T> resultConvert) {

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .withMaxRetries(0)
                .build();

        return Failsafe
                .with(retryPolicy)
                .with(connectionManager.getExecutorService())
                .getStageAsync(() -> supplier.get().toCompletionStage())
                .thenApply(resultConvert);
    }

}
