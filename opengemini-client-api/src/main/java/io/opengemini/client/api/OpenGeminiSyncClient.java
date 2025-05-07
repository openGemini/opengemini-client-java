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

/**
// * Interface to access a OpenGemini database provides a set of blocking methods.
 */
public interface OpenGeminiSyncClient extends AutoCloseable {

    /**
     * Create a new database.
     *
     * @param database the name of the new database.
     */
    void createDatabase(String database) throws OpenGeminiException;

    /**
     * Drop a database.
     *
     * @param database the name of the database to drop.
     */
    void dropDatabase(String database) throws OpenGeminiException;

    /**
     * Show all available databases.
     */
    List<String> showDatabases() throws OpenGeminiException;

    /**
     * Create a retention policy.
     *
     * @param database  the name of the database.
     * @param rpConfig  the config of the retention policy
     * @param isDefault if the retention policy is the default retention policy for the database or not
     */
    void createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault) throws OpenGeminiException;

    /**
     * Show all available retention policies.
     *
     * @param database the name of the database.
     */
    List<RetentionPolicy> showRetentionPolicies(String database) throws OpenGeminiException;

    /**
     * Drop a retention policy.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy to drop.
     */
    void dropRetentionPolicy(String database, String retentionPolicy) throws OpenGeminiException;

    /**
     * Execute a query against a database.
     *
     * @param query the query to execute.
     */
    QueryResult query(Query query) throws OpenGeminiException;

    /**
     * Write a single point to the database.
     *
     * @param database the name of the database.
     * @param point    the point to write.
     */
    void write(String database, Point point) throws OpenGeminiException;

    /**
     * Write points to the database.
     *
     * @param database the name of the database.
     * @param points   the points to write.
     */
    void write(String database, List<Point> points) throws OpenGeminiException;

    /**
     * Write a single point to the database.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy.
     * @param point           the point to write.
     */
    void write(String database, String retentionPolicy, Point point) throws OpenGeminiException;

    /**
     * Write points to the database.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy.
     * @param points          the points to write.
     */
    void write(String database, String retentionPolicy, List<Point> points) throws OpenGeminiException;

    /**
     * Ping the OpenGemini server
     */
    Pong ping() throws OpenGeminiException;
}
