package io.opengemini.client.grpc;

import io.opengemini.client.api.RpcClientConfig;
import io.opengemini.client.grpc.service.WriteService;
import io.opengemini.client.grpc.support.RpcClientSupplier;

public class RpcClient {
    private final RpcClientConfig config;
    private final RpcClientConnectionManager connectionManager;

    private final RpcClientSupplier<WriteService> writeClient;

    public static RpcClient create(final RpcClientConfig config) {
        return new RpcClient(config);
    }

    private RpcClient(RpcClientConfig config) {
        this.config = config;
        this.connectionManager = new RpcClientConnectionManager(config);
        this.writeClient = new RpcClientSupplier<>(() -> new WriteService(this.connectionManager));
    }


    public WriteService getWriteClient() {
        return writeClient.get();
    }

    public synchronized void close() {
        writeClient.close();
        connectionManager.close();
    }
}
