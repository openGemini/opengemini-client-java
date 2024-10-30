package io.opengemini.client.grpc;

import io.opengemini.client.api.RpcClientConfig;
import io.opengemini.client.grpc.support.RpcClientSupplier;

public class RpcClient {
    private final RpcClientConfig config;



    public static RpcClient create(final RpcClientConfig config) {
        return new RpcClient(config);
    }

    private RpcClient(RpcClientConfig config) {
        this.config = config;
    }
}
