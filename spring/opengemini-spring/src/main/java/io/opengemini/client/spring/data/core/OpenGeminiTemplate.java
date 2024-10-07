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
import io.opengemini.client.spring.data.annotation.Database;
import io.opengemini.client.spring.data.annotation.Measurement;
import io.opengemini.client.spring.data.annotation.RetentionPolicy;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class that simplifies OpenGemini data access code.
 */
public class OpenGeminiTemplate implements OpenGeminiOperations {

    private final OpenGeminiAsyncClient asyncClient;
    private final OpenGeminiSerializerFactory serializerFactory;
    private final Map<MeasurementOperationsCacheKey, MeasurementOperations<?>> msOperationsMap =
            new ConcurrentHashMap<>();

    public OpenGeminiTemplate(OpenGeminiAsyncClient asyncClient, OpenGeminiSerializerFactory serializerFactory) {
        this.asyncClient = asyncClient;
        this.serializerFactory = serializerFactory;
    }

    @Override
    public <T> MeasurementOperations<T> opsForMeasurement(Class<T> clazz) {
        MeasurementOperationsCacheKey key = MeasurementOperationsCacheKey.of(clazz);
        return getMeasurementOperations(key);
    }

    @Override
    public <T> MeasurementOperations<T> opsForMeasurement(String databaseName,
                                                          String retentionPolicyName,
                                                          String measurementName,
                                                          Class<T> clazz) {
        MeasurementOperationsCacheKey key = new MeasurementOperationsCacheKey(databaseName, retentionPolicyName,
                                                                              measurementName, clazz);
        return getMeasurementOperations(key);
    }

    @SuppressWarnings("unchecked")
    private <T> @NotNull MeasurementOperations<T> getMeasurementOperations(MeasurementOperationsCacheKey key) {
        return (MeasurementOperations<T>) msOperationsMap.computeIfAbsent(key, (k) -> {
            OpenGeminiSerializer<T> serializer = (OpenGeminiSerializer<T>) serializerFactory.getSerializer(
                    k.getClazz());
            return new MeasurementOperationsImpl<>(asyncClient, serializer, k.getDatabaseName(),
                                                   k.getRetentionPolicyName(), k.getMeasurementName());
        });
    }

    @Getter
    @EqualsAndHashCode
    private static class MeasurementOperationsCacheKey {
        private final String databaseName;
        private final String retentionPolicyName;
        private final String measurementName;
        private final Class<?> clazz;

        public MeasurementOperationsCacheKey(String databaseName,
                                             String retentionPolicyName,
                                             String measurementName,
                                             Class<?> clazz) {
            this.databaseName = databaseName;
            this.retentionPolicyName = retentionPolicyName;
            this.measurementName = measurementName;
            this.clazz = clazz;
        }

        public static MeasurementOperationsCacheKey of(Class<?> clazz) {
            Measurement msAnnotation = clazz.getAnnotation(Measurement.class);
            if (msAnnotation == null) {
                throw new IllegalArgumentException("Class " + clazz.getName() + " has no @Measurement annotation");
            }
            RetentionPolicy rpAnnotation = clazz.getAnnotation(RetentionPolicy.class);
            if (rpAnnotation == null) {
                throw new IllegalArgumentException("Class " + clazz.getName() + " has no @RetentionPolicy annotation");
            }
            Database dbAnnotation = clazz.getAnnotation(Database.class);
            if (dbAnnotation == null) {
                throw new IllegalArgumentException("Class " + clazz.getName() + " has no @Database annotation");
            }
            return new MeasurementOperationsCacheKey(dbAnnotation.name(), rpAnnotation.name(), msAnnotation.name(),
                                                     clazz);
        }
    }
}
