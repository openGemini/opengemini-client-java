package io.opengemini.client.asynchttpclient;

import io.opengemini.client.api.Address;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class OpenGeminiAsyncHttpClientTest {

    private OpenGeminiAsyncHttpClient openGeminiAsyncHttpClient;

    @BeforeEach
    void setUp() {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
                .build();
        this.openGeminiAsyncHttpClient = new OpenGeminiAsyncHttpClient(configuration);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.openGeminiAsyncHttpClient.close();
    }

    @Test
    void query_should_throws_exception_when_address_is_wrong() throws Exception {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 28086)))
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
                .build();

        try (OpenGeminiAsyncHttpClient wrongClient = new OpenGeminiAsyncHttpClient(configuration)) {
            Query showDatabasesQuery = new Query("SHOW DATABASES");
            CompletableFuture<QueryResult> rstFuture = wrongClient.query(showDatabasesQuery);

            Assertions.assertThrows(ExecutionException.class, rstFuture::get);
        }
    }

    @Test
    void database_lifecycle_for_create_show_drop() throws Exception {
        String databaseTestName = "db_test_lifecycle";
        CompletableFuture<Void> createDbFuture = openGeminiAsyncHttpClient.createDatabase(databaseTestName);
        createDbFuture.get();

        CompletableFuture<List<String>> showDbFuture = openGeminiAsyncHttpClient.showDatabases();
        Assertions.assertTrue(showDbFuture.get().contains(databaseTestName));

        CompletableFuture<Void> dropDbFuture = openGeminiAsyncHttpClient.dropDatabase(databaseTestName);
        dropDbFuture.get();
    }

    @Test
    void query_for_show_tag_keys() throws Exception {
        String databaseTestName = "db_test_ms_tag_keys";
        CompletableFuture<Void> createDbFuture = openGeminiAsyncHttpClient.createDatabase(databaseTestName);
        createDbFuture.get();

        String measureTestName = "ms_test_tag_keys";

        String command = String.format(Locale.ROOT, ("CREATE MEASUREMENT %s (tag1 TAG,tag2 TAG,tag3 TAG, "
                + "field1 INT64 FIELD, field2 BOOL, field3 STRING, field4 FLOAT64)"), measureTestName);
        Query createMeasurementQuery = new Query(command, databaseTestName, null);
        CompletableFuture<QueryResult> createMsFuture = openGeminiAsyncHttpClient.query(createMeasurementQuery);
        QueryResult rst = createMsFuture.get();
        Assertions.assertEquals(1, rst.getResults().size());

        Query showFieldQuery = new Query(
                String.format(Locale.ROOT, "SHOW TAG KEYS FROM %s limit 3 OFFSET 0", measureTestName), databaseTestName,
                null);
        CompletableFuture<QueryResult> showTagKeysFuture = openGeminiAsyncHttpClient.query(showFieldQuery);
        QueryResult showRst = showTagKeysFuture.get();

        CompletableFuture<Void> dropDbFuture = openGeminiAsyncHttpClient.dropDatabase(databaseTestName);
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

    @Test
    void write_point_success_and_query_success() throws Exception {
        String databaseName = "db_test_write";
        CompletableFuture<Void> createDbFuture = openGeminiAsyncHttpClient.createDatabase(databaseName);
        createDbFuture.get();

        String measurementName = "ms_test_write";
        Point testPoint = testPoint(measurementName, 1, 1);

        CompletableFuture<Void> writeRsp = openGeminiAsyncHttpClient.write(databaseName, testPoint);
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, null);
        CompletableFuture<QueryResult> rst = openGeminiAsyncHttpClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropDbFuture = openGeminiAsyncHttpClient.dropDatabase(databaseName);
        dropDbFuture.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(x.getValues().size(), 1);
        Assertions.assertTrue(x.getValues().get(0).contains("value1"));
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
    }

    @Test
    void write_point_with_more_fields() throws Exception {
        String databaseName = "db_test_write_more_fields";
        CompletableFuture<Void> createDbFuture = openGeminiAsyncHttpClient.createDatabase(databaseName);
        createDbFuture.get();

        String measurementName = "md_test_write_more_fields";
        Point testPoint = testPoint(measurementName, 1, 30);

        CompletableFuture<Void> writeRsp = openGeminiAsyncHttpClient.write(databaseName, testPoint);
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = openGeminiAsyncHttpClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropDbFuture = openGeminiAsyncHttpClient.dropDatabase(databaseName);
        dropDbFuture.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(x.getValues().size(), 1);
        Assertions.assertTrue(x.getValues().get(0).contains("value1"));
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
        Assertions.assertTrue(x.getColumns().contains("field29"));
        Assertions.assertTrue(x.getColumns().contains("tag29"));
    }

    @Test
    void write_empty_batch_points() throws Exception {
        String databaseName = "db_test_write_batch";

        CompletableFuture<Void> writeRsp = openGeminiAsyncHttpClient.write(databaseName,
                new ArrayList<>());
        writeRsp.get();
    }

    @Test
    void write_batch_points() throws Exception {
        String databaseName = "db_test_write_batch";
        CompletableFuture<Void> createDbFuture = openGeminiAsyncHttpClient.createDatabase(databaseName);
        createDbFuture.get();

        String measurementName = "ms_test_write_batch";
        Point testPoint1 = testPoint(measurementName, 1, 1);
        Point testPoint2 = testPoint(measurementName, 2, 1);
        Point testPoint3 = testPoint(measurementName, 3, 1);

        CompletableFuture<Void> writeRsp = openGeminiAsyncHttpClient.write(databaseName,
                Arrays.asList(testPoint1, testPoint2, testPoint3));
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = openGeminiAsyncHttpClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropDbFuture = openGeminiAsyncHttpClient.dropDatabase(databaseName);
        dropDbFuture.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(x.getValues().size(), 3);
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
    }

    @Test
    void retention_policy_lifecycle_for_create_show_drop() throws Exception {
        String testRpNameBase = "rp_test_lifecycle";
        ArrayList<RpConfig> rps = new ArrayList<>();
        rps.add(new RpConfig(testRpNameBase + 0, "3d", "", ""));
        rps.add(new RpConfig(testRpNameBase + 1, "3d", "1h", ""));
        rps.add(new RpConfig(testRpNameBase + 2, "3d", "1h", "7h"));
        rps.add(new RpConfig(testRpNameBase + 3, "365d", "", ""));

        String database = "db_test_rp_lifecycle";
        CompletableFuture<Void> createDbFuture = openGeminiAsyncHttpClient.createDatabase(database);
        createDbFuture.get();

        for (int i = 0; i < rps.size(); i++) {
            boolean isDefaultRp = Boolean.FALSE;
            if (i == rps.size() - 1) {
                isDefaultRp = Boolean.TRUE;
            }
            CompletableFuture<Void> createRsp = openGeminiAsyncHttpClient.createRetentionPolicy(database, rps.get(i),
                    isDefaultRp);
            createRsp.get();
            Thread.sleep(2000);

            CompletableFuture<List<RetentionPolicy>> showRpRsp = openGeminiAsyncHttpClient.showRetentionPolicies(
                    database);
            List<RetentionPolicy> rsp = showRpRsp.get();
            CompletableFuture<Void> dropRsp = openGeminiAsyncHttpClient.dropRetentionPolicy(database,
                    rps.get(i).getName());
            dropRsp.get();
            String testRpName = testRpNameBase + i;
            Assertions.assertTrue(rsp.stream().anyMatch(x -> x.getName().equals(testRpName)));
        }

        CompletableFuture<Void> dropDbFuture = openGeminiAsyncHttpClient.dropDatabase(database);
        dropDbFuture.get();
    }

    @Test
    void retention_policy_create_failed_for_wrong_duration_param() throws Exception {
        String database = "db_test_rp_lifecycle_2";
        CompletableFuture<Void> createDbFuture = openGeminiAsyncHttpClient.createDatabase(database);
        createDbFuture.get();

        String testRpName = "rp_test_lifecycle_2";
        RpConfig rpConfig = new RpConfig(testRpName, "d3d", "", "");
        CompletableFuture<Void> createRpFuture = openGeminiAsyncHttpClient.createRetentionPolicy(database, rpConfig,
                Boolean.FALSE);

        ExecutionException e = Assertions.assertThrows(ExecutionException.class, createRpFuture::get);
        Assertions.assertInstanceOf(OpenGeminiException.class, e.getCause());
        Assertions.assertTrue(
                e.getCause().getMessage().contains("syntax error: unexpected IDENT, expecting DURATIONVAL"));
        Thread.sleep(2000);

        CompletableFuture<List<RetentionPolicy>> showRpRsp = openGeminiAsyncHttpClient.showRetentionPolicies(database);
        List<RetentionPolicy> rsp = showRpRsp.get();
        CompletableFuture<Void> dropRpFuture = openGeminiAsyncHttpClient.dropRetentionPolicy(database,
                rpConfig.getName());
        dropRpFuture.get();

        Assertions.assertFalse(rsp.stream().anyMatch(retentionPolicy -> retentionPolicy.getName().equals(testRpName)));

        CompletableFuture<Void> dropDbFuture = openGeminiAsyncHttpClient.dropDatabase(database);
        dropDbFuture.get();
    }

    @Test
    void ping() throws Exception {
        CompletableFuture<Pong> pingFuture = openGeminiAsyncHttpClient.ping();
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

    @SneakyThrows
    @Test
    void testQueryPrecision() {
        String databaseName = "query_precision_0001";
        CompletableFuture<Void> createdb = openGeminiAsyncHttpClient.createDatabase(databaseName);
        createdb.get();

        String measurementName = "query_precision_ms_0001";
        Point testPoint1 = testPoint(measurementName, 1, 1);
        Point testPoint2 = testPoint(measurementName, 2, 1);
        Point testPoint3 = testPoint(measurementName, 3, 1);

        CompletableFuture<Void> writeRsp = openGeminiAsyncHttpClient.write(databaseName,
                Arrays.asList(testPoint1, testPoint2, testPoint3));
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = openGeminiAsyncHttpClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        Object timeValue = x.getValues().get(0).get(0);
        Assertions.assertInstanceOf(String.class, timeValue);
        String timeValueStr = (String) timeValue;
        Assertions.assertTrue(timeValueStr.startsWith("20") && timeValueStr.endsWith("Z"));

        selectQuery = new Query("select * from " + measurementName, databaseName, "", Precision.PRECISIONNANOSECOND);
        rst = openGeminiAsyncHttpClient.query(selectQuery);
        queryResult = rst.get();

        x = queryResult.getResults().get(0).getSeries().get(0);
        timeValue = x.getValues().get(0).get(0);
        Assertions.assertInstanceOf(Long.class, timeValue);
        long timeValueDouble = (Long) timeValue;
        Assertions.assertTrue(timeValueDouble > 1724778721457052741L);

        CompletableFuture<Void> dropdb = openGeminiAsyncHttpClient.dropDatabase(databaseName);
        dropdb.get();
    }
}
