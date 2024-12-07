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
