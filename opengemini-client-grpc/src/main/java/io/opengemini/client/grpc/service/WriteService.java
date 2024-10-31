package io.opengemini.client.grpc.service;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.grpc.ResponseCode;
import io.opengemini.client.grpc.RpcClientConnectionManager;
import io.opengemini.client.grpc.VertxWriteServiceGrpc;
import io.opengemini.client.grpc.WriteRowsRequest;

import java.util.concurrent.CompletableFuture;

public class WriteService extends ServiceImpl {
    private final VertxWriteServiceGrpc.WriteServiceVertxStub stub;

    public WriteService(RpcClientConnectionManager connectionManager) {
        super(connectionManager);
        this.stub = connectionManager.newStub(VertxWriteServiceGrpc::newVertxStub);
    }


    public CompletableFuture<Void> writeRows(WriteRowsRequest writeRowsRequest) {
        return execute(() -> stub.writeRows(writeRowsRequest), writeRowsResponse -> {
            if (ResponseCode.Success == writeRowsResponse.getCode()) {
                return null;
            } else {
                // TODO: Exception ?
                return null;
            }
        });
    }
}
