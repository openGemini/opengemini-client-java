/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opengemini.client.impl.grpc.service;

import com.google.protobuf.ByteString;
import io.opengemini.client.api.Point;
import io.opengemini.client.impl.grpc.RpcClientConnectionManager;
import io.opengemini.client.grpc.VertxWriteServiceGrpc;
import io.opengemini.client.grpc.WriteRequest;
import io.opengemini.client.grpc.Record;
import io.opengemini.client.impl.grpc.record.ColVal;
import io.opengemini.client.impl.grpc.record.Field;
import io.opengemini.client.impl.grpc.support.PointConverter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WriteService extends ServiceImpl {
    private final VertxWriteServiceGrpc.WriteServiceVertxStub stub;

    public WriteService(RpcClientConnectionManager connectionManager) throws Exception {
        super(connectionManager);
        this.stub = connectionManager.newStub(VertxWriteServiceGrpc::newVertxStub);
    }

    public CompletableFuture<Void> writeRows(String database, List<Point> points) {
        Map<String, List<Point>> measurementPoints = points.stream()
                .collect(Collectors.groupingBy(
                        Point::getMeasurement,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparingLong(Point::getTime));
                                    return list;
                                }
                        )
                ));

        List<Record> records = buildRecords(measurementPoints);

        String username = getConnectionManager().getConfig().getUsername();
        String password = getConnectionManager().getConfig().getPassword();


        WriteRequest writeRequest = WriteRequest
                .newBuilder()
                .setDatabase(Objects.requireNonNull(database))
                .setUsername(username == null ? "" : username)
                .setPassword(password == null ? "" : password)
                .addAllRecords(records)
                .build();

        return writeRows(writeRequest);
    }

    private List<Record> buildRecords(Map<String, List<Point>> measurementPoints) {
        List<io.opengemini.client.grpc.Record> records = new ArrayList<>(measurementPoints.size());
        measurementPoints.forEach((measurement, points) -> {
            if (measurement != null && !measurement.isEmpty()) {
                try {
                    records.add(buildRecord(measurement, points));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return records;
    }

    private Record buildRecord(String measurement, List<Point> points) throws IOException {
        List<Field> schemas = PointConverter.extractSchema(points);
        List<ColVal> colVals = PointConverter.extractColVals(points, schemas);
        io.opengemini.client.impl.grpc.record.Record record = new io.opengemini.client.impl.grpc.record.Record();
        record.setSchema(schemas.toArray(new Field[0]));
        record.setColVals(colVals.toArray(new ColVal[0]));

        return Record
                .newBuilder()
                .setMinTime(points.get(0).getTime())
                .setMaxTime(points.get(points.size() - 1).getTime())
                .setMeasurement(measurement)
                .setBlock(ByteString.copyFrom(record.marshal()))
                .build();
    }


    public CompletableFuture<Void> writeRows(WriteRequest writeRequest) {
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        stub.write(writeRequest)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        resultFuture.complete(null);
                    } else {
                        resultFuture.completeExceptionally(ar.cause());
                    }
                });
        return resultFuture;
    }
}
