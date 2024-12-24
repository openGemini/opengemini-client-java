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

package io.opengemini.client.common;

import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RetentionPolicy;
import io.opengemini.client.api.RpConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public abstract class BaseAsyncClient extends BaseClient implements OpenGeminiAsyncClient {

    public BaseAsyncClient(Configuration conf) {
        super(conf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> createDatabase(String database) {
        String command = CommandFactory.createDatabase(database);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> dropDatabase(String database) {
        String command = CommandFactory.dropDatabase(database);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<String>> showDatabases() {
        String command = CommandFactory.showDatabases();
        Query query = new Query(command);
        return executeQuery(query).thenApply(ResultMapper::toDatabases);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault) {
        String command = CommandFactory.createRetentionPolicy(database, rpConfig, isDefault);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<RetentionPolicy>> showRetentionPolicies(String database) {
        if (StringUtils.isBlank(database)) {
            return null;
        }

        String command = CommandFactory.showRetentionPolicies(database);
        Query query = new Query(command);
        query.setDatabase(database);
        return executeQuery(query).thenApply(ResultMapper::toRetentionPolicies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> dropRetentionPolicy(String database, String retentionPolicy) {
        String command = CommandFactory.dropRetentionPolicy(database, retentionPolicy);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<QueryResult> query(Query query) {
        return executeQuery(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> write(String database, Point point) {
        return write(database, null, point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> write(String database, List<Point> points) {
        return write(database, null, points);
    }

    @Override
    public CompletableFuture<Void> write(String database, String retentionPolicy, Point point) {
        String body = point.lineProtocol();
        if (StringUtils.isEmpty(body)) {
            return CompletableFuture.completedFuture(null);
        }
        return executeWrite(database, retentionPolicy, body);
    }

    @Override
    public CompletableFuture<Void> write(String database, String retentionPolicy, List<Point> points) {
        if (points.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        StringJoiner sj = new StringJoiner("\n");
        for (Point point : points) {
            String lineProtocol = point.lineProtocol();
            if (StringUtils.isEmpty(lineProtocol)) {
                continue;
            }
            sj.add(lineProtocol);
        }
        String body = sj.toString();
        if (StringUtils.isEmpty(body)) {
            return CompletableFuture.completedFuture(null);
        }
        return executeWrite(database, retentionPolicy, body);
    }

    @Override
    public CompletableFuture<Void> writeByGrpc(String database, List<Point> points) {
        if (points.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return executeWriteByGrpc(database, points);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Pong> ping() {
        return executePing();
    }

    /**
     * The implementation class needs to implement this method to execute a GET query call.
     *
     * @param query the query to execute.
     */
    protected abstract CompletableFuture<QueryResult> executeQuery(Query query);

    /**
     * The implementation class needs to implement this method to execute a POST query call.
     *
     * @param query the query to execute.
     */
    protected abstract CompletableFuture<QueryResult> executePostQuery(Query query);

    /**
     * The implementation class needs to implement this method to execute a write call.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy.
     * @param lineProtocol    the line protocol string to write.
     */
    protected abstract CompletableFuture<Void> executeWrite(String database,
                                                            String retentionPolicy,
                                                            String lineProtocol);

    /**
     * The implementation class needs to implement this method to execute a write operation via an RPC call.
     *
     * @param database the name of the database.
     * @param points   the points to write.
     */
    protected abstract CompletableFuture<Void> executeWriteByGrpc(String database,
                                                                  List<Point> points);

    /**
     * The implementation class needs to implement this method to execute a ping call.
     */
    protected abstract CompletableFuture<Pong> executePing();

}
