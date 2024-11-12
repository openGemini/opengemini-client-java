package io.opengemini.client.grpc.service;

import io.opengemini.client.api.Point;
import io.opengemini.client.grpc.*;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class WriteService extends ServiceImpl {
    private static final String NEWLINE_DELIMITER = "\n";

    private final VertxWriteServiceGrpc.WriteServiceVertxStub stub;

    public WriteService(RpcClientConnectionManager connectionManager) {
        super(connectionManager);
        this.stub = connectionManager.newStub(VertxWriteServiceGrpc::newVertxStub);
    }

    public CompletableFuture<Void> writeRows(String database, List<Point> points) {

        LongSummaryStatistics stats = points.stream().mapToLong(Point::getTime).summaryStatistics();
        Rows.Builder rowsBuilder = Rows.newBuilder()
                .setMinTime(stats.getMin())
                .setMaxTime(stats.getMax());

        // 如果row的measurement不同的处理方案?
        // rowsBuilder.setMeasurement(....)

        populateBlock(rowsBuilder, points);

        WriteRowsRequest request = WriteRowsRequest
                .newBuilder()
                .setDatabase(Objects.requireNonNull(database))
                .setUsername(getConnectionManager().getConfig().getUsername())
                .setPassword(getConnectionManager().getConfig().getPassword())
                .setRows(rowsBuilder.build())
                .build();
        return writeRows(request);
    }

    private void populateBlock(Rows.Builder builder, List<Point> points) {
        // TODO: Parse points to block

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
