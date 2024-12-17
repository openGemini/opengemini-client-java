package io.opengemini.client.impl.grpc.service;

import io.opengemini.client.api.Point;
import io.opengemini.client.impl.grpc.GrpcClientConnectionManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WriteGrpcService extends GrpcService {
    public WriteGrpcService(GrpcClientConnectionManager connectionManager) {
        super(connectionManager);
    }

    public CompletableFuture<Void> writeRows(String database, List<Point> points) {
        return null;
    }
}
