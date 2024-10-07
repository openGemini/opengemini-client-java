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

package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.api.Query;
import lombok.SneakyThrows;

import java.util.List;

public class MeasurementOperationsImpl<T> implements MeasurementOperations<T> {

    private final OpenGeminiAsyncClient asyncClient;
    private final OpenGeminiSerializer<T> serializer;
    private final String databaseName;
    private final String retentionPolicyName;
    private final String measurementName;

    public MeasurementOperationsImpl(OpenGeminiAsyncClient asyncClient,
                                     OpenGeminiSerializer<T> serializer,
                                     String databaseName,
                                     String retentionPolicyName,
                                     String measurementName) {
        this.asyncClient = asyncClient;
        this.serializer = serializer;
        this.databaseName = databaseName;
        this.retentionPolicyName = retentionPolicyName;
        this.measurementName = measurementName;
    }

    @SneakyThrows
    @Override
    public void write(T t) {
        asyncClient.write(databaseName, retentionPolicyName, serializer.serialize(measurementName, t)).get();
    }

    @SneakyThrows
    @Override
    public void write(List<T> list) {
        asyncClient.write(databaseName, retentionPolicyName, serializer.serialize(measurementName, list)).get();
    }

    @SneakyThrows
    @Override
    public List<T> query(Query query) {
        return serializer.deserialize(measurementName, asyncClient.query(query).get());
    }
}
