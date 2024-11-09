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

import io.opengemini.client.api.RpConfig;

/**
 * Interface that specified a basic set of OpenGemini operations, implemented by {@link OpenGeminiTemplate}.
 * A useful option for extensibility and testability (as it can be easily mocked or stubbed).
 */
public interface OpenGeminiOperations {

    void createDatabaseIfAbsent(String database);

    void createDatabase(String database);

    boolean isDatabaseExists(String database);

    void dropDatabase(String database);

    void createRetentionPolicyIfAbsent(String database, RpConfig rpConfig, boolean isDefault);

    void createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault);

    boolean isRetentionPolicyExists(String database, String retentionPolicy);

    void dropRetentionPolicy(String database, String retentionPolicy);

    <T> MeasurementOperations<T> opsForMeasurement(Class<T> clazz);

    <T> MeasurementOperations<T> opsForMeasurement(String databaseName,
                                                   String retentionPolicyName,
                                                   String measurementName,
                                                   Class<T> clazz);
}
