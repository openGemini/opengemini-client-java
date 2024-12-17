package io.opengemini.client.impl.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.stub.AbstractStub;
import io.opengemini.client.api.GrpcConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.grpc.VertxChannelBuilder;
import lombok.Getter;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

public class GrpcClientConnectionManager {
    private final Object locker;
    @Getter
    private final GrpcConfig config;
    private volatile ManagedChannel managedChannel;
    private volatile Vertx vertx;

    GrpcClientConnectionManager(GrpcConfig config) {
        this(config, null);
    }

    GrpcClientConnectionManager(GrpcConfig config, ManagedChannel managedChannel) {
        this.locker = new Object();
        this.config = config;
        this.managedChannel = managedChannel;
    }


    ManagedChannel getChannel() throws Exception {
        if (managedChannel == null) {
            synchronized (locker) {
                if (managedChannel == null) {
                    managedChannel = defaultChannelBuilder().build();
                }
            }
        }
        return managedChannel;
    }

    void close() {
        synchronized (locker) {
            if (managedChannel != null) {
                managedChannel.shutdown();
            }
            if (vertx != null) {
                vertx.close();
            }
        }
    }

    Vertx vertx() {
        if (this.vertx == null) {
            synchronized (locker) {
                if (this.vertx == null) {
                    this.vertx = Vertx.vertx(new VertxOptions().setUseDaemonThread(false));
                }
            }
        }

        return this.vertx;
    }

    ManagedChannelBuilder<?> defaultChannelBuilder() throws Exception {
        final VertxChannelBuilder channelBuilder = VertxChannelBuilder
                .forAddress(vertx(), Objects.requireNonNull(config.getHost()), Objects.requireNonNull(config.getPort()));

        if (config.isUseSSL()) {
            channelBuilder.nettyBuilder().negotiationType(NegotiationType.TLS);
            channelBuilder.nettyBuilder().sslContext(GrpcSslContexts.forClient()
                    .trustManager(new File(config.getCaCertPath()))
                    .keyManager(new File(config.getClientCertPath()), new File(config.getClientKeyPath()))
                    .build());
        } else {
            channelBuilder.usePlaintext();
        }
        return channelBuilder;
    }

    public <T extends AbstractStub<T>> T newStub(Function<ManagedChannel, T> supplier) throws Exception {
        return newStub(supplier, getChannel());
    }

    private <T extends AbstractStub<T>> T newStub(Function<ManagedChannel, T> stubCustomizer, ManagedChannel channel) {
        T stub = stubCustomizer.apply(channel);
        if (config.isWaitForReady()) {
            stub = stub.withWaitForReady();
        }
        return stub;
    }
}
