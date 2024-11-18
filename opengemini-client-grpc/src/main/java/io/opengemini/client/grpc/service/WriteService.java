package io.opengemini.client.grpc.service;

import com.google.protobuf.ByteString;
import io.opengemini.client.api.Point;
import io.opengemini.client.grpc.*;
import io.opengemini.client.grpc.record.ColVal;
import io.opengemini.client.grpc.record.Field;
import io.opengemini.client.grpc.record.Record;
import io.opengemini.client.grpc.support.PointConverter;

import java.io.IOException;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class WriteService extends ServiceImpl {
    private static final String NEWLINE_DELIMITER = "\n";

    private final VertxWriteServiceGrpc.WriteServiceVertxStub stub;

    public WriteService(RpcClientConnectionManager connectionManager) throws Exception {
        super(connectionManager);
        this.stub = connectionManager.newStub(VertxWriteServiceGrpc::newVertxStub);
    }

    public CompletableFuture<Void> writeRows(String database, String measurement, List<Point> points) {

        LongSummaryStatistics stats = points.stream().mapToLong(Point::getTime).summaryStatistics();
        Rows.Builder rowsBuilder = Rows.newBuilder()
                .setMinTime(stats.getMin())
                .setMaxTime(stats.getMax());

        // 如果row的measurement不同的处理方案?
        // rowsBuilder.setMeasurement(....)
        rowsBuilder.setMeasurement(measurement);
        populateBlock(rowsBuilder, points);

        String username = getConnectionManager().getConfig().getUsername();
        String password = getConnectionManager().getConfig().getPassword();

        WriteRowsRequest request = WriteRowsRequest
                .newBuilder()
                .setDatabase(Objects.requireNonNull(database))
                .setUsername(username == null ? "" : username)
                .setPassword(password == null ? "" : password)
                .setRows(rowsBuilder.build())
                .build();
        return writeRows(request);
    }

    private void populateBlock(Rows.Builder builder, List<Point> points) {

        List<Field> schemas = PointConverter.extractSchema(points);
        List<ColVal> colVals = PointConverter.extractColVals(points, schemas);

        Record record = new Record();
        record.setSchema(schemas.toArray(new Field[0]));
        record.setColVals(colVals.toArray(new ColVal[0]));

        try {
            builder.setBlock(ByteString.copyFrom(record.marshal()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
