package io.opengemini.client.impl.grpc.support;

import io.opengemini.client.impl.grpc.service.GrpcService;

import java.util.Objects;
import java.util.function.Supplier;

public class GrpcServiceSupplier<T extends GrpcService> implements Supplier<T> {
    private final Supplier<T> delegate;
    private volatile boolean initialized = false;
    private T holder;

    public GrpcServiceSupplier(Supplier<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    T t = delegate.get();
                    holder = t;
                    initialized = true;
                    return t;
                }
            }
        }
        return holder;
    }

    public void close() {
        if (initialized) {
            synchronized (this) {
                if (initialized) {
                    if (holder != null) {
                        holder = null;
                    }
                }
                initialized = false;
            }
        }
    }
}