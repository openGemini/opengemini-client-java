/*
 * Copyright 2025 openGemini Authors
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

package io.opengemini.client.interceptor;

import io.github.openfacade.http.HttpResponse;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.Write;

import java.util.concurrent.CompletableFuture;

/**
 * Interceptor interface for OpenGemini client operations.
 * Allows custom logic to be executed before and after query and write operations.
 */
public interface Interceptor {

    /**
     * Executes before a query operation.
     *
     * @param query the query to be executed
     * @return a CompletableFuture that completes when the interceptor logic is done
     */
    CompletableFuture<Void> queryBefore(Query query);

    /**
     * Executes after a query operation.
     *
     * @param query the query that was executed
     * @param response the HTTP response from the query
     * @return a CompletableFuture that completes when the interceptor logic is done
     */
    CompletableFuture<Void> queryAfter(Query query, HttpResponse response);

    /**
     * Executes before a write operation.
     *
     * @param write the write operation to be executed
     * @return a CompletableFuture that completes when the interceptor logic is done
     */
    CompletableFuture<Void> writeBefore(Write write);

    /**
     * Executes after a write operation.
     *
     * @param write the write operation that was executed
     * @param response the HTTP response from the write
     * @return a CompletableFuture that completes when the interceptor logic is done
     */
    CompletableFuture<Void> writeAfter(Write write, HttpResponse response);
}
