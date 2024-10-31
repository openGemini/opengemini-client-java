package io.opengemini.client.grpc.service;

import io.opengemini.client.grpc.*;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class WriteService extends ServiceImpl {
    private static final String NEWLINE_DELIMITER = "\n";

    private final VertxWriteServiceGrpc.WriteServiceVertxStub stub;

    public WriteService(RpcClientConnectionManager connectionManager) {
        super(connectionManager);
        this.stub = connectionManager.newStub(VertxWriteServiceGrpc::newVertxStub);
    }

    public CompletableFuture<Void> writeRows(String database, String lineProtocol, long minTime, long maxTime) {
        Rows.Builder rowsBuilder = Rows.newBuilder()
                .setMinTime(minTime)
                .setMaxTime(maxTime);

        // 如果row的measurement不同的处理方案?
        // rowsBuilder.setMeasurement(....)

        populateBlock(rowsBuilder, lineProtocol);

        WriteRowsRequest request = WriteRowsRequest
                .newBuilder()
                .setDatabase(Objects.requireNonNull(database))
                .setUsername(getConnectionManager().getConfig().getUsername())
                .setPassword(getConnectionManager().getConfig().getPassword())
                .setRows(rowsBuilder.build())
                .build();
        return writeRows(request);
    }

    private void populateBlock(Rows.Builder builder, String lineProtocol) {
        String[] lineProtocols = lineProtocol.split(NEWLINE_DELIMITER);
        // TODO: Parse line protocols
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
