package io.opengemini.client.impl.grpc;

import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.GrpcConfig;
import io.opengemini.client.grpc.Record;
import io.opengemini.client.grpc.ResponseCode;
import io.opengemini.client.grpc.WriteRequest;
import io.opengemini.client.grpc.WriteResponse;
import io.opengemini.client.grpc.WriteServiceGrpc;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class GrpcClientTest {
    private Vertx vertx;
    private int port;
    private Server server;


    private final WriteServiceGrpc.WriteServiceImplBase serviceImpl = Mockito.mock(WriteServiceGrpc.WriteServiceImplBase.class, delegatesTo(
            new WriteServiceGrpc.WriteServiceImplBase() {
                @Override
                public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
                    responseObserver.onNext(WriteResponse.newBuilder().setCode(ResponseCode.Success).build());
                    responseObserver.onCompleted();
                }
            }
    ));

    private GrpcClient grpcClient;

    @BeforeEach
    public void setUp(VertxTestContext testContext) throws Exception {
        vertx = Vertx.vertx();
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        server = VertxServerBuilder
                .forAddress(vertx, "localhost", port)
                .addService(serviceImpl)
                .build()
                .start();
        testContext.completeNow();

        GrpcConfig config = GrpcConfig.builder().host("127.0.0.1").port(port).build();

        grpcClient = GrpcClient.create(config);
    }

    @Test
    void testWrite() throws ExecutionException, InterruptedException {
        WriteRequest request = WriteRequest
                .newBuilder()
                .setDatabase("test")
                .setUsername("test")
                .setPassword("test")
                .addAllRecords(Collections.singletonList(Record.newBuilder().build()))
                .build();
        grpcClient.getWriteClient().writeRows(request).get();
    }

    @Test
    void testWriteRows() throws ExecutionException, InterruptedException {
        GrpcConfig config = GrpcConfig
                .builder()
                .host("127.0.0.1")
                .port(8305)
                .build();
        GrpcClient grpcClient = GrpcClient.create(config);
        List<Point> points = new ArrayList<>();
        Point point1 = new Point();
        Map<String, Object> fields1 = new HashMap<>();
        fields1.put("a", 1.0);
        fields1.put("b", -1.0);
        fields1.put("c", 0.0);
        point1.setTime(new Date().getTime() * 1_000_000);

        Map<String, String> tags1 = new HashMap<>();
        tags1.put("tag1", "111");

        point1.setFields(fields1);
        point1.setTags(tags1);
        point1.setMeasurement("test1");

        Point point2 = new Point();
        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("a", 2.0);
        fields2.put("b", -2.0);
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("tag1", "222");
        point2.setFields(fields2);
        point2.setTags(tags2);
        point2.setTime((new Date().getTime() + 100) * 1_000_000);
        point2.setMeasurement("test1");
        points.add(point1);
        points.add(point2);


        grpcClient.getWriteClient().writeRows("test", points).get();
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (grpcClient != null) {
            grpcClient.close();
        }
        if (server != null) {
            server.shutdown();
        }
        vertx.close()
                .onComplete(testContext.succeedingThenComplete());
    }
}