package io.opengemini.client.grpc;

import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import io.opengemini.client.api.RpcClientConfig;
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

        RpcClientConfig config = RpcClientConfig.builder().host("127.0.0.1").port(port).build();

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
