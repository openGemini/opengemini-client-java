package io.opengemini.client.impl.grpc.service;

import io.opengemini.client.impl.grpc.GrpcClientConnectionManager;
import lombok.Getter;

@Getter
public class GrpcService {
    private final GrpcClientConnectionManager connectionManager;

    protected GrpcService(final GrpcClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
