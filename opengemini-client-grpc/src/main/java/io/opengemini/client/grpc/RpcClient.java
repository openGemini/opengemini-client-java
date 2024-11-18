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

package io.opengemini.client.grpc;

import io.opengemini.client.api.RpcClientConfig;
import io.opengemini.client.grpc.service.WriteService;
import io.opengemini.client.grpc.support.RpcClientSupplier;

public class RpcClient {
    private final RpcClientConfig config;
    private final RpcClientConnectionManager connectionManager;

    private final RpcClientSupplier<WriteService> writeClient;

    public static RpcClient create(final RpcClientConfig config) {
        return new RpcClient(config);
    }

    private RpcClient(RpcClientConfig config) {
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
