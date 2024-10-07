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

package io.opengemini.client.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface to access a OpenGemini database provides a set of non-blocking methods that return CompletableFuture.
 */
public interface OpenGeminiAsyncClient extends AutoCloseable {

    /**
     * Create a new database.
     *
     * @param database the name of the new database.
     */
    CompletableFuture<Void> createDatabase(String database);

    /**
     * Drop a database.
     *
     * @param database the name of the database to drop.
     */
    CompletableFuture<Void> dropDatabase(String database);

    /**
     * Show all available databases.
     */
    CompletableFuture<List<String>> showDatabases();

    /**
     * Create a retention policy.
     *
     * @param database  the name of the database.
     * @param rpConfig  the config of the retention policy
     * @param isDefault if the retention policy is the default retention policy for the database or not
     */
    CompletableFuture<Void> createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault);

    /**
     * Show all available retention policies.
     *
     * @param database the name of the database.
     */
    CompletableFuture<List<RetentionPolicy>> showRetentionPolicies(String database);

    /**
     * Drop a retention policy.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy to drop.
     */
    CompletableFuture<Void> dropRetentionPolicy(String database, String retentionPolicy);

    /**
     * Execute a query against a database.
     *
     * @param query the query to execute.
     */
    CompletableFuture<QueryResult> query(Query query);

    /**
     * Write a single point to the database.
     *
     * @param database the name of the database.
     * @param point    the point to write.
     */
    CompletableFuture<Void> write(String database, Point point);

    /**
     * Write points to the database.
     *
     * @param database the name of the database.
     * @param points   the points to write.
     */
    CompletableFuture<Void> write(String database, List<Point> points);

    /**
     * Write a single point to the database.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy.
     * @param point           the point to write.
     */
    CompletableFuture<Void> write(String database, String retentionPolicy, Point point);

    /**
     * Write points to the database.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy.
     * @param points          the points to write.
     */
    CompletableFuture<Void> write(String database, String retentionPolicy, List<Point> points);

    /**
     * Ping the OpenGemini server
     */
    CompletableFuture<Pong> ping();
}
