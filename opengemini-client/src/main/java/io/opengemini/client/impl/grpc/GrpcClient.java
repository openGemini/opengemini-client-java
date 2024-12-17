package io.opengemini.client.impl.grpc;

import io.opengemini.client.api.GrpcConfig;
import io.opengemini.client.impl.grpc.service.WriteGrpcService;
import io.opengemini.client.impl.grpc.support.GrpcServiceSupplier;

public class GrpcClient {
    private final GrpcConfig config;
    private final GrpcClientConnectionManager connectionManager;
    private final GrpcServiceSupplier<WriteGrpcService> writeGrpcService;

    public static GrpcClient create(final GrpcConfig config) {
        return new GrpcClient(config);
    }

    private GrpcClient(GrpcConfig config) {
        this.config = config;
        this.connectionManager = new GrpcClientConnectionManager(config);
        this.writeGrpcService = new GrpcServiceSupplier<>(() -> {
            try {
                return new WriteGrpcService(this.connectionManager);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
