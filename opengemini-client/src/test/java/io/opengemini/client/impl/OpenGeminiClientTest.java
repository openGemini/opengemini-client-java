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
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.CompressMethod;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Precision;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RetentionPolicy;
import io.opengemini.client.api.RpConfig;
import io.opengemini.client.api.Series;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenGeminiClientTest extends TestBase {

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
            Configuration configuration
                    = Configuration.builder()
                            .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                            .httpConfig(httpConfig)
                            .gzipEnabled(false)
                            .build();
            clients.add(OpenGeminiClientFactory.create(configuration));
        }
        List<CompressMethod> compressMethods = Arrays.asList(CompressMethod.SNAPPY, CompressMethod.GZIP,
                CompressMethod.ZSTD);
        for (CompressMethod compressMethod : compressMethods) {
            HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                    .engine(HttpClientEngine.Async)
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

    private OpenGeminiClient openGeminiClient;

    @BeforeEach
    void setUp() {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(3))
                .build();
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .httpConfig(httpConfig)
                .authConfig(new AuthConfig(AuthType.PASSWORD, "test", "testPwd123@".toCharArray(), null))
                .gzipEnabled(false)
                .build();
        this.openGeminiClient = new OpenGeminiClient(configuration);
    }

    @Test
    void testDatabase() throws Exception {
        String databaseTestName = "testDatabase_0001";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(databaseTestName);
        createdb.get();

        CompletableFuture<List<String>> rstFuture = openGeminiClient.showDatabases();
        List<String> rst = rstFuture.get();
        Assertions.assertTrue(rst.contains(databaseTestName));

        CompletableFuture<Void> dropdb = openGeminiClient.dropDatabase(databaseTestName);
        dropdb.get();
    }

    @Test
    void testShowField() throws Exception {
        String databaseTestName = "database_test_0001";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(databaseTestName);
        createdb.get();

        String measureTestName = "measure_test";
        String rpTestName = "";
        Query createMeasurementQuery = new Query(("CREATE MEASUREMENT %s (tag1 TAG,tag2 TAG,tag3 TAG, "
                + "field1 INT64 FIELD, field2 BOOL, field3 STRING, field4 FLOAT64)")
                .formatted(measureTestName), databaseTestName, rpTestName);
        CompletableFuture<QueryResult> rstFuture = openGeminiClient.query(createMeasurementQuery);
        QueryResult rst = rstFuture.get();
        Assertions.assertEquals(1, rst.getResults().size());

        Query showFieldQuery = new Query("SHOW TAG KEYS FROM %s limit 3 OFFSET 0".formatted(measureTestName),
                databaseTestName, rpTestName);
        CompletableFuture<QueryResult> showRstFuture = openGeminiClient.query(showFieldQuery);
        QueryResult showRst = showRstFuture.get();
        Assertions.assertEquals(1, showRst.getResults().size());
        Series series = showRst.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(series.getName(), measureTestName);
        Assertions.assertEquals(series.getColumns(), Collections.singletonList("tagKey"));
        Assertions.assertEquals(series.getValues(), List.of(
                List.of("tag1"), List.of("tag2"), List.of("tag3")));

        CompletableFuture<Void> dropdb = openGeminiClient.dropDatabase(databaseTestName);
        dropdb.get();
    }

    private Point generalTestPoint(String measurementName, int valueIndex, int fieldCount) {
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

    @SneakyThrows
    @Test
    void testRetentionPolicyNormal() {
        String testRpNameBase = "testRpName";
        ArrayList<RpConfig> rps = new ArrayList<>();
        rps.add(new RpConfig(testRpNameBase + 0, "3d", "", ""));
        rps.add(new RpConfig(testRpNameBase + 1, "3d", "1h", ""));
        rps.add(new RpConfig(testRpNameBase + 2, "3d", "1h", "7h"));
        rps.add(new RpConfig(testRpNameBase + 3, "365d", "", ""));

        String database = "testRpDatabase0001";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(database);
        createdb.get();

        for (int i = 0; i < rps.size(); i++) {
            boolean isDefaultRp = Boolean.FALSE;
            if (i == 4) {
                isDefaultRp = Boolean.TRUE;
            }
            CompletableFuture<Void> createRsp = openGeminiClient.createRetentionPolicy(
                    database, rps.get(i), isDefaultRp);
            createRsp.get();
            Thread.sleep(2000);

            CompletableFuture<List<RetentionPolicy>> showRpRsp = openGeminiClient.showRetentionPolicies(database);
            List<RetentionPolicy> rsp = showRpRsp.get();
            CompletableFuture<Void> dropRsp = openGeminiClient.dropRetentionPolicy(database, rps.get(i).getName());
            dropRsp.get();
            String testRpName = testRpNameBase + i;
            Assertions.assertTrue(rsp.stream().anyMatch(x -> x.getName().equals(testRpName)));
        }

        CompletableFuture<Void> dropdb = openGeminiClient.dropDatabase(database);
        dropdb.get();
    }

    @SneakyThrows
    @Test
    void testRetentionPolicyError() {
        String testRpName = "testRpName";
        RpConfig rp = new RpConfig(testRpName + 0, "d3d", "", "");

        String database = "testRpDatabase0002";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(database);
        createdb.get();

        CompletableFuture<Void> createRsp = openGeminiClient.createRetentionPolicy(
                database, rp, Boolean.FALSE);

        ExecutionException e = Assertions.assertThrows(ExecutionException.class, createRsp::get);
        Assertions.assertInstanceOf(OpenGeminiException.class, e.getCause());
        Assertions.assertTrue(e.getCause().getMessage().contains(
                "syntax error: unexpected IDENT, expecting DURATIONVAL"));

        Thread.sleep(2000);

        CompletableFuture<List<RetentionPolicy>> showRpRsp = openGeminiClient.showRetentionPolicies(database);
        List<RetentionPolicy> rsp = showRpRsp.get();
        CompletableFuture<Void> dropRsp = openGeminiClient.dropRetentionPolicy(database, rp.getName());
        dropRsp.get();
        for (RetentionPolicy retentionPolicy : rsp) {
            Assertions.assertFalse(retentionPolicy.getName().contains(testRpName + 0));
        }

        openGeminiClient.dropDatabase(database).get();
    }

    @SneakyThrows
    @Test
    void testQueryPrecision() {
        String databaseName = "query_precision_0001";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(databaseName);
        createdb.get();

        String measurementName = "query_precision_ms_0001";
        Point testPoint = generalTestPoint(measurementName, 1, 1);

        CompletableFuture<Void> writeRsp = openGeminiClient.write(databaseName, testPoint);
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = openGeminiClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Object timeValue = x.getValues().get(0).get(0);
        Assertions.assertInstanceOf(String.class, timeValue);
        String timeValueStr = (String) timeValue;
        Assertions.assertTrue(timeValueStr.startsWith("20") && timeValueStr.endsWith("Z"));

        selectQuery = new Query("select * from " + measurementName, databaseName, "", Precision.PRECISIONNANOSECOND);
        rst = openGeminiClient.query(selectQuery);
        queryResult = rst.get();

        x = queryResult.getResults().get(0).getSeries().get(0);
        timeValue = x.getValues().get(0).get(0);
        Assertions.assertInstanceOf(Long.class, timeValue);
        long timeValueDouble = (Long) timeValue;
        Assertions.assertTrue(timeValueDouble > 1724778721457052741L);

        CompletableFuture<Void> dropdb = openGeminiClient.dropDatabase(databaseName);
        dropdb.get();
    }

    @Test
    void query_should_throws_exception_when_address_is_wrong() throws IOException {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 28086)))
                .httpConfig(new HttpClientConfig.Builder().build())
                .gzipEnabled(false)
                .build();

        try (OpenGeminiClient wrongClient = new OpenGeminiClient(configuration)) {
            Query showDatabasesQuery = new Query("SHOW DATABASES");
            CompletableFuture<QueryResult> rstFuture = wrongClient.query(showDatabasesQuery);
            Assertions.assertThrows(ExecutionException.class, rstFuture::get);
        }
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void database_lifecycle_for_create_show_drop(OpenGeminiClient client) throws Exception {
        String databaseTestName = "db_test_lifecycle" + httpEngine(client);
        CompletableFuture<Void> createDbFuture = client.createDatabase(databaseTestName);
        createDbFuture.get();

        CompletableFuture<List<String>> showDbFuture = client.showDatabases();
        Assertions.assertTrue(showDbFuture.get().contains(databaseTestName));

        CompletableFuture<Void> dropDbFuture = client.dropDatabase(databaseTestName);
        dropDbFuture.get();
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void query_for_show_tag_keys(OpenGeminiClient client) throws Exception {
        String databaseTestName = "db_test_ms_tag_keys" + httpEngine(client);
        CompletableFuture<Void> createDbFuture = client.createDatabase(databaseTestName);
        createDbFuture.get();

        String measureTestName = "ms_test_tag_keys" + httpEngine(client);

        String command = String.format(Locale.ROOT, ("CREATE MEASUREMENT %s (tag1 TAG,tag2 TAG,tag3 TAG, "
                + "field1 INT64 FIELD, field2 BOOL, field3 STRING, field4 FLOAT64)"), measureTestName);
        Query createMeasurementQuery = new Query(command, databaseTestName, null);
        CompletableFuture<QueryResult> createMsFuture = client.query(createMeasurementQuery);
        QueryResult rst = createMsFuture.get();
        Assertions.assertEquals(1, rst.getResults().size());

        Query showFieldQuery = new Query(
                String.format(Locale.ROOT, "SHOW TAG KEYS FROM %s limit 3 OFFSET 0", measureTestName), databaseTestName,
                null);
        CompletableFuture<QueryResult> showTagKeysFuture = client.query(showFieldQuery);
        QueryResult showRst = showTagKeysFuture.get();

        CompletableFuture<Void> dropDbFuture = client.dropDatabase(databaseTestName);
        dropDbFuture.get();

        Assertions.assertEquals(1, showRst.getResults().size());
        Series series = showRst.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(series.getName(), measureTestName);
        Assertions.assertEquals(series.getColumns(), Collections.singletonList("tagKey"));
        List<List<Object>> values = new ArrayList<>();
        values.add(Collections.singletonList("tag1"));
        values.add(Collections.singletonList("tag2"));
        values.add(Collections.singletonList("tag3"));
        Assertions.assertEquals(series.getValues(), values);
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void retention_policy_lifecycle_for_create_show_drop(OpenGeminiClient client) throws Exception {
        String testRpNameBase = "rp_test_lifecycle" + httpEngine(client);
        ArrayList<RpConfig> rps = new ArrayList<>();
        rps.add(new RpConfig(testRpNameBase + 0, "3d", "", ""));
        rps.add(new RpConfig(testRpNameBase + 1, "3d", "1h", ""));
        rps.add(new RpConfig(testRpNameBase + 2, "3d", "1h", "7h"));
        rps.add(new RpConfig(testRpNameBase + 3, "365d", "", ""));

        String database = "db_test_rp_lifecycle" + httpEngine(client);
        CompletableFuture<Void> createDbFuture = client.createDatabase(database);
        createDbFuture.get();

        for (int i = 0; i < rps.size(); i++) {
            boolean isDefaultRp = Boolean.FALSE;
            if (i == rps.size() - 1) {
                isDefaultRp = Boolean.TRUE;
            }
            CompletableFuture<Void> createRsp = client.createRetentionPolicy(database, rps.get(i),
                    isDefaultRp);
            createRsp.get();
            Thread.sleep(2000);

            CompletableFuture<List<RetentionPolicy>> showRpRsp = client.showRetentionPolicies(database);
            List<RetentionPolicy> rsp = showRpRsp.get();
            CompletableFuture<Void> dropRsp = client.dropRetentionPolicy(database,
                    rps.get(i).getName());
            dropRsp.get();
            String testRpName = testRpNameBase + i;
            Assertions.assertTrue(rsp.stream().anyMatch(x -> x.getName().equals(testRpName)));
        }

        CompletableFuture<Void> dropDbFuture = client.dropDatabase(database);
        dropDbFuture.get();
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void retention_policy_create_failed_for_wrong_duration_param(OpenGeminiClient client) throws Exception {
        String database = "db_test_rp_lifecycle_2" + httpEngine(client);
        CompletableFuture<Void> createDbFuture = client.createDatabase(database);
        createDbFuture.get();

        String testRpName = "rp_test_lifecycle_2" + httpEngine(client);
        RpConfig rpConfig = new RpConfig(testRpName, "d3d", "", "");
        CompletableFuture<Void> createRpFuture = client.createRetentionPolicy(database, rpConfig,
                Boolean.FALSE);

        // todo jdk doesn't throw ExecutionException
        Exception exception = Assertions.assertThrows(Exception.class, createRpFuture::get);
        if (exception instanceof ExecutionException) {
            ExecutionException exp = (ExecutionException) exception;
            if (exp.getCause() instanceof OpenGeminiException) {
                OpenGeminiException e = (OpenGeminiException) exp.getCause();
                Assertions.assertTrue(
                        e.getMessage().contains("syntax error: unexpected IDENT, expecting DURATIONVAL"));
            }
        }
        Thread.sleep(2000);

        CompletableFuture<List<RetentionPolicy>> showRpRsp = client.showRetentionPolicies(database);
        List<RetentionPolicy> rsp = showRpRsp.get();
        CompletableFuture<Void> dropRpFuture = client.dropRetentionPolicy(database, rpConfig.getName());
        dropRpFuture.get();

        Assertions.assertFalse(rsp.stream().anyMatch(retentionPolicy -> retentionPolicy.getName().equals(testRpName)));

        CompletableFuture<Void> dropDbFuture = client.dropDatabase(database);
        dropDbFuture.get();
    }

    @ParameterizedTest
    @MethodSource("clientList")
    void ping(OpenGeminiClient client) throws Exception {
        CompletableFuture<Pong> pingFuture = client.ping();
        Pong pong = pingFuture.get();

        Assertions.assertNotNull(pong.getVersion());
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

    @ParameterizedTest
    @MethodSource("clientList")
    void testQueryPrecision(OpenGeminiClient client) throws Exception {
        String databaseName = "query_precision_0001" + httpEngine(client);
        CompletableFuture<Void> createdb = client.createDatabase(databaseName);
        createdb.get();

        String measurementName = "query_precision_ms_0001" + httpEngine(client);
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

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Object timeValue = x.getValues().get(0).get(0);
        Assertions.assertInstanceOf(String.class, timeValue);
        String timeValueStr = (String) timeValue;
        Assertions.assertTrue(timeValueStr.startsWith("20") && timeValueStr.endsWith("Z"));

        selectQuery = new Query("select * from " + measurementName, databaseName, "", Precision.PRECISIONNANOSECOND);
        rst = client.query(selectQuery);
        queryResult = rst.get();

        x = queryResult.getResults().get(0).getSeries().get(0);
        timeValue = x.getValues().get(0).get(0);
        Assertions.assertInstanceOf(Long.class, timeValue);
        long timeValueDouble = (Long) timeValue;
        Assertions.assertTrue(timeValueDouble > 1724778721457052741L);

        CompletableFuture<Void> dropdb = client.dropDatabase(databaseName);
        dropdb.get();
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
