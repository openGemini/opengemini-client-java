package io.opengemini.client.grpc;

import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.GrpcConfig;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class RpcClientTest {
    private Vertx vertx;
    private int port;
    private Server server;


    private final WriteServiceGrpc.WriteServiceImplBase serviceImpl = mock(WriteServiceGrpc.WriteServiceImplBase.class, delegatesTo(
            new WriteServiceGrpc.WriteServiceImplBase() {
                @Override
                public void writeRows(WriteRowsRequest request, StreamObserver<WriteRowsResponse> responseObserver) {
                    responseObserver.onNext(WriteRowsResponse.newBuilder().setCode(ResponseCode.Success).build());
                    responseObserver.onCompleted();

                }
            }
    ));

    private RpcClient rpcClient;

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

        rpcClient = RpcClient.create(config);
    }

    @Test
    void testWrite() throws ExecutionException, InterruptedException {
        WriteRowsRequest request = WriteRowsRequest
                .newBuilder()
                .setDatabase("test")
                .setUsername("test")
                .setPassword("test")
                .setRows(Rows.newBuilder().build())
                .build();
        rpcClient.getWriteClient().writeRows(request).get();
    }

    @Test
    void testWriteRows() throws ExecutionException, InterruptedException {
        GrpcConfig config =  GrpcConfig
                .builder()
                .host("127.0.0.1")
                .port(8305)
                .build();
        RpcClient rpcClient  = RpcClient.create(config);
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

        Point point2 = new Point();
        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("a", 2.0);
        fields2.put("b", -2.0);
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("tag1", "222");
        point2.setFields(fields2);
        point2.setTags(tags2);
        point2.setTime((new Date().getTime() + 100) * 1_000_000);
        points.add(point1);
        points.add(point2);


        rpcClient.getWriteClient().writeRows("test", "test1", points).get();
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (rpcClient != null) {
            rpcClient.close();
        }
        if (server != null) {
            server.shutdown();
        }
        vertx.close()
                .onComplete(testContext.succeedingThenComplete());
    }
}
