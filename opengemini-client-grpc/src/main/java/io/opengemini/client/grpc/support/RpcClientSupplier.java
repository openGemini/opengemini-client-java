package io.opengemini.client.grpc.support;

import java.util.Objects;
import java.util.function.Supplier;

public class RpcClientSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private volatile boolean initialized = false;
    private T holder;

    public RpcClientSupplier(Supplier<T> delegate) {
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
                        // TODO: Holder close
                        holder = null;
                    }
                }
                initialized = false;
            }
        }
    }
}
