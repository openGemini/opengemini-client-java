package io.opengemini.client.impl.grpc;

import io.opengemini.client.api.GrpcConfig;

public class GrpcClient {
    private final GrpcConfig config;
    private final GrpcClientConnectionManager connectionManager;

    public static GrpcClient create(final GrpcConfig config) {
        return new GrpcClient(config);
    }

    private GrpcClient(GrpcConfig config) {
        this.config = config;
        this.connectionManager = new GrpcClientConnectionManager(config);
    }

}
