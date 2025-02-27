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

package io.opengemini.client.impl;

import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.HttpClientEngine;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.CompressMethod;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RpConfig;
import io.opengemini.client.api.Series;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenGeminiClientWriteTest extends TestBase {
    private final List<OpenGeminiClient> clients = new ArrayList<>();

    protected List<OpenGeminiClient> clientList() throws OpenGeminiException {
        List<HttpClientEngine> engines = new ArrayList<>();
        engines.add(HttpClientEngine.Async);
        engines.add(HttpClientEngine.Java);
        engines.add(HttpClientEngine.Java8);
        engines.add(HttpClientEngine.OkHttp);
        List<OpenGeminiClient> clients = new ArrayList<>();
        for (HttpClientEngine engine : engines) {
            HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                    .engine(engine)
                    .connectTimeout(Duration.ofSeconds(3))
                    .timeout(Duration.ofSeconds(3))
                    .build();
            Configuration configuration =
                Configuration.builder()
                             .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                             .httpConfig(httpConfig)
                             .gzipEnabled(false)
                             .build();
            clients.add(OpenGeminiClientFactory.create(configuration));
        }

        List<CompressMethod> compressMethods = Arrays.asList(CompressMethod.SNAPPY);
        for (CompressMethod compressMethod : compressMethods) {
            HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                    .engine(HttpClientEngine.AsyncHttpClient)
                    .connectTimeout(Duration.ofSeconds(3))
                    .timeout(Duration.ofSeconds(3))
                    .build();
            Configuration configuration = Configuration.builder()
                    .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                    .httpConfig(httpConfig)
                    .compressMethod(compressMethod)
                    .build();
            clients.add(OpenGeminiClientFactory.create(configuration));
        }
        return clients;
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void write_point_success_and_query_success(OpenGeminiClient client) throws Exception {
        String databaseName = "db_test_write" + httpEngine(client);
        CompletableFuture<Void> createDbFuture = client.createDatabase(databaseName);
        createDbFuture.get();

        String measurementName = "ms_test_write" + httpEngine(client);
        Point testPoint = testPoint(measurementName, 1, 1);

        CompletableFuture<Void> writeRsp = client.write(databaseName, testPoint);
        writeRsp.get();
        Thread.sleep(5000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, null);
        CompletableFuture<QueryResult> rst = client.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropDbFuture = client.dropDatabase(databaseName);
        dropDbFuture.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(x.getValues().size(), 1);
        Assertions.assertTrue(x.getValues().get(0).contains("value1"));
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void write_point_with_more_fields(OpenGeminiClient client) throws Exception {
        String databaseName = "db_test_write_more_fields" + httpEngine(client);
        CompletableFuture<Void> createDbFuture = client.createDatabase(databaseName);
        createDbFuture.get();

        String measurementName = "md_test_write_more_fields" + httpEngine(client);
        Point testPoint = testPoint(measurementName, 1, 30);

        CompletableFuture<Void> writeRsp = client.write(databaseName, testPoint);
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName,
                databaseName, "");
        CompletableFuture<QueryResult> rst = client.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropDbFuture = client.dropDatabase(databaseName);
        dropDbFuture.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(x.getValues().size(), 1);
        Assertions.assertTrue(x.getValues().get(0).contains("value1"));
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
        Assertions.assertTrue(x.getColumns().contains("field29"));
        Assertions.assertTrue(x.getColumns().contains("tag29"));
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void write_empty_batch_points(OpenGeminiClient client) throws Exception {
        String databaseName = "db_test_write_batch" + httpEngine(client);

        CompletableFuture<Void> writeRsp = client.write(databaseName,
                new ArrayList<>());
        writeRsp.get();
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void write_batch_points(OpenGeminiClient client) throws Exception {
        String databaseName = "db_test_write_batch" + httpEngine(client);
        CompletableFuture<Void> createDbFuture = client.createDatabase(databaseName);
        createDbFuture.get();

        String measurementName = "ms_test_write_batch" + httpEngine(client);
        Point testPoint1 = testPoint(measurementName, 1, 1);
        Point testPoint2 = testPoint(measurementName, 2, 1);
        Point testPoint3 = testPoint(measurementName, 3, 1);

        CompletableFuture<Void> writeRsp = client.write(databaseName,
                Arrays.asList(testPoint1, testPoint2, testPoint3));
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = client.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropDbFuture = client.dropDatabase(databaseName);
        dropDbFuture.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(x.getValues().size(), 3);
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void write_batch_points_with_rp(OpenGeminiClient client) throws Exception {
        String databaseName = "db_test_write_batch" + httpEngine(client);
        client.createDatabase(databaseName).get();

        String rpName = "rp_test_write_batch" + httpEngine(client);
        client.createRetentionPolicy(databaseName, new RpConfig(rpName, "3d", "", ""), false).get();

        String measurementName = "ms_test_write_batch" + httpEngine(client);
        Point testPoint1 = testPoint(measurementName, 1, 1);
        Point testPoint2 = testPoint(measurementName, 2, 1);
        Point testPoint3 = testPoint(measurementName, 3, 1);

        client.write(databaseName, rpName, Arrays.asList(testPoint1, testPoint2, testPoint3)).get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, rpName);
        CompletableFuture<QueryResult> rst = client.query(selectQuery);
        QueryResult queryResult = rst.get();

        client.dropRetentionPolicy(databaseName, rpName).get();
        client.dropDatabase(databaseName).get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(x.getValues().size(), 3);
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
    }

    private static Point testPoint(String measurementName, int valueIndex, int fieldCount) {
        Point testPoint = new Point();
        testPoint.setMeasurement(measurementName);
        HashMap<String, String> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        for (int i = 0; i < fieldCount; i++) {
            tags.put("tag" + i, "value" + valueIndex);
            fields.put("field" + i, "value" + valueIndex);
        }
        testPoint.setTags(tags);
        testPoint.setFields(fields);
        return testPoint;
    }

    @AfterAll
    void closeClients() {
        for (OpenGeminiClient client : clients) {
            try {
                client.close();
            } catch (IOException e) {
                // ignore exception
            }
        }
    }
}
