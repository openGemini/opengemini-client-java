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

package io.opengemini.client.grpc;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

public class RpcClientConnectionManager {
    private final Object locker;
    @Getter
    private final GrpcConfig config;
    private volatile ManagedChannel managedChannel;
    private volatile Vertx vertx;

    @Getter
    private final ExecutorService executorService;

    RpcClientConnectionManager(GrpcConfig config) {
        this(config, null);
    }

    RpcClientConnectionManager(GrpcConfig config, ManagedChannel managedChannel) {
        this.locker = new Object();
        this.config = config;
        this.managedChannel = managedChannel;
        // TODO: Parameterized configuration
        this.executorService = Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors() * 2) + 1, r -> {
            final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
            Thread thread = defaultFactory.newThread(r);
            thread.setName("OpenGemini-RpcClient-" + thread.getName());
            thread.setDaemon(false);
            return thread;
        });
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
            if (executorService != null) {
                executorService.shutdown();
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
        // TODO: more build config properties
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
