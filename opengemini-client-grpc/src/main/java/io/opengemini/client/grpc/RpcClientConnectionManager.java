package io.opengemini.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opengemini.client.api.RpcClientConfig;
import io.vertx.grpc.VertxChannelBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static io.vertx.core.Vertx.vertx;

final class RpcClientConnectionManager {
    private final Object locker;
    private final RpcClientConfig config;
    private volatile ManagedChannel managedChannel;
    private final ExecutorService executorService;

    RpcClientConnectionManager(RpcClientConfig config) {
        this(config, null);
    }

    RpcClientConnectionManager(RpcClientConfig config, ManagedChannel managedChannel) {
        this.locker = new Object();
        this.config = config;
        this.managedChannel = managedChannel;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1, r -> {
            final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
            Thread thread = defaultFactory.newThread(r);
            thread.setName("OpenGemini-RpcClient-" + thread.getName());
            thread.setDaemon(false);
            return thread;
        });
    }

    ManagedChannel getChannel() {
        if (managedChannel == null) {
            synchronized (locker) {
                if (managedChannel == null) {
                    managedChannel = defaultChannelBuilder().build();
                }
            }
        }
        return managedChannel;
    }

    ManagedChannelBuilder<?> defaultChannelBuilder() {
        if (config.getTarget() == null) {
            throw new IllegalArgumentException("At least one endpoint should be provided");
        }
        final VertxChannelBuilder channelBuilder = VertxChannelBuilder.forTarget(vertx(), config.getTarget());
        // TODO: more build config properties
        return channelBuilder;
    }
}
