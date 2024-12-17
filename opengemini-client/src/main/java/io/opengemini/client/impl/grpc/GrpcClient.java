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

package io.opengemini.client.impl.grpc;

import io.opengemini.client.api.GrpcConfig;
import io.opengemini.client.impl.grpc.service.WriteService;
import io.opengemini.client.impl.grpc.support.RpcClientSupplier;

public class GrpcClient {
    private final GrpcConfig config;
    private final RpcClientConnectionManager connectionManager;

    private final RpcClientSupplier<WriteService> writeClient;

    public static GrpcClient create(final GrpcConfig config) {
        return new GrpcClient(config);
    }

    private GrpcClient(GrpcConfig config) {
        this.config = config;
        this.connectionManager = new RpcClientConnectionManager(config);
        this.writeClient = new RpcClientSupplier<>(() -> {
            try {
                return new WriteService(this.connectionManager);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    public WriteService getWriteClient() {
        return writeClient.get();
    }

    public synchronized void close() {
        writeClient.close();
        connectionManager.close();
    }
}
