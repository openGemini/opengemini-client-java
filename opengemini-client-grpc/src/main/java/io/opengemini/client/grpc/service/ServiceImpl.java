// Copyright 2022 Huawei Cloud Computing Technologies Co., Ltd.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.opengemini.client.grpc.service;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.opengemini.client.grpc.RpcClientConnectionManager;
import io.vertx.core.Future;

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
