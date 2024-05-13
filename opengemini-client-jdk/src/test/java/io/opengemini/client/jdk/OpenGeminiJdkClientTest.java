package io.opengemini.client.jdk;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RetentionPolicy;
import io.opengemini.client.api.RpConfig;
import io.opengemini.client.api.Series;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class OpenGeminiJdkClientTest {

    private OpenGeminiJdkClient openGeminiJdkClient;

    @BeforeEach
    void setUp() {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
                .build();
        this.openGeminiJdkClient = new OpenGeminiJdkClient(configuration);
    }

    @Test
    void queryWithWrongAddress() {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 28086)))
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
                .build();

        OpenGeminiJdkClient wrongClient = new OpenGeminiJdkClient(configuration);
        Query showDatabasesQuery = new Query("SHOW DATABASES");
        CompletableFuture<QueryResult> rstFuture = wrongClient.query(showDatabasesQuery);
        Assertions.assertThrows(ExecutionException.class, rstFuture::get);
    }

    @Test
    void testDatabase() throws Exception {
        String databaseTestName = "testDatabase_0001";
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(databaseTestName);
        createdb.get();

        CompletableFuture<List<String>> rstFuture = openGeminiJdkClient.showDatabases();
        List<String> rst = rstFuture.get();
        Assertions.assertTrue(rst.contains(databaseTestName));

        CompletableFuture<Void> dropdb = openGeminiJdkClient.dropDatabase(databaseTestName);
        dropdb.get();
    }

    @Test
    void testShowField() throws Exception {
        String databaseTestName = "database_test_0001";
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(databaseTestName);
        createdb.get();

        String measureTestName = "measure_test";
        String rpTestName = "";
        Query createMeasurementQuery = new Query(("CREATE MEASUREMENT %s (tag1 TAG,tag2 TAG,tag3 TAG, "
                + "field1 INT64 FIELD, field2 BOOL, field3 STRING, field4 FLOAT64)")
                .formatted(measureTestName), databaseTestName, rpTestName);
        CompletableFuture<QueryResult> rstFuture = openGeminiJdkClient.query(createMeasurementQuery);
        QueryResult rst = rstFuture.get();
        Assertions.assertEquals(1, rst.getResults().size());

        Query showFieldQuery = new Query("SHOW TAG KEYS FROM %s limit 3 OFFSET 0".formatted(measureTestName),
                databaseTestName, rpTestName);
        CompletableFuture<QueryResult> showRstFuture = openGeminiJdkClient.query(showFieldQuery);
        QueryResult showRst = showRstFuture.get();
        Assertions.assertEquals(1, showRst.getResults().size());
        Series series = showRst.getResults().get(0).getSeries().get(0);
        Assertions.assertEquals(series.getName(), measureTestName);
        Assertions.assertEquals(series.getColumns(), Collections.singletonList("tagKey"));
        Assertions.assertEquals(series.getValues(), List.of(
                List.of("tag1"), List.of("tag2"), List.of("tag3")));

        CompletableFuture<Void> dropdb = openGeminiJdkClient.dropDatabase(databaseTestName);
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
    void testWritePoint() {
        String databaseName = "write_database_0001";
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(databaseName);
        createdb.get();

        String measurementName = "write_measurement_0001";
        Point testPoint = generalTestPoint(measurementName, 1, 1);

        CompletableFuture<Void> writeRsp = openGeminiJdkClient.write(databaseName, testPoint);
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = openGeminiJdkClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropdb = openGeminiJdkClient.dropDatabase(databaseName);
        dropdb.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        System.out.println(x);
        Assertions.assertEquals(x.getValues().size(), 1);
        Assertions.assertTrue(x.getValues().get(0).contains("value1"));
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
    }

    @SneakyThrows
    @Test
    void testWritePointMoreFields() {
        String databaseName = "write_database_0002";
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(databaseName);
        createdb.get();

        String measurementName = "write_measurement_0002";
        Point testPoint = generalTestPoint(measurementName, 1, 30);

        CompletableFuture<Void> writeRsp = openGeminiJdkClient.write(databaseName, testPoint);
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = openGeminiJdkClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropdb = openGeminiJdkClient.dropDatabase(databaseName);
        dropdb.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        System.out.println(x);
        Assertions.assertEquals(x.getValues().size(), 1);
        Assertions.assertTrue(x.getValues().get(0).contains("value1"));
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
        Assertions.assertTrue(x.getColumns().contains("field29"));
        Assertions.assertTrue(x.getColumns().contains("tag29"));
    }

    @SneakyThrows
    @Test
    void testWriteBatchPoints() {
        String databaseName = "writePointBatch_database_0001";
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(databaseName);
        createdb.get();

        String measurementName = "writePointBatch_measurement_0001";
        Point testPoint1 = generalTestPoint(measurementName, 1, 1);
        Point testPoint2 = generalTestPoint(measurementName, 2, 1);
        Point testPoint3 = generalTestPoint(measurementName, 3, 1);

        CompletableFuture<Void> writeRsp = openGeminiJdkClient.writeBatch(
                databaseName, Arrays.asList(testPoint1, testPoint2, testPoint3));
        writeRsp.get();
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        CompletableFuture<QueryResult> rst = openGeminiJdkClient.query(selectQuery);
        QueryResult queryResult = rst.get();

        CompletableFuture<Void> dropdb = openGeminiJdkClient.dropDatabase(databaseName);
        dropdb.get();

        Series x = queryResult.getResults().get(0).getSeries().get(0);
        System.out.println(x);
        Assertions.assertEquals(x.getValues().size(), 3);
        Assertions.assertTrue(x.getColumns().contains("field0"));
        Assertions.assertTrue(x.getColumns().contains("tag0"));
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
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(database);
        createdb.get();

        for (int i = 0; i < rps.size(); i++) {
            boolean isDefaultRp = Boolean.FALSE;
            if (i == 4) {
                isDefaultRp = Boolean.TRUE;
            }
            CompletableFuture<Void> createRsp = openGeminiJdkClient.createRetentionPolicy(
                    database, rps.get(i), isDefaultRp);
            createRsp.get();
            Thread.sleep(2000);

            CompletableFuture<List<RetentionPolicy>> showRpRsp = openGeminiJdkClient.showRetentionPolicies(database);
            List<RetentionPolicy> rsp = showRpRsp.get();
            CompletableFuture<Void> dropRsp = openGeminiJdkClient.dropRetentionPolicy(database, rps.get(i).getName());
            dropRsp.get();
            String testRpName = testRpNameBase + i;
            Assertions.assertTrue(rsp.stream().anyMatch(x -> x.getName().equals(testRpName)));
        }

        CompletableFuture<Void> dropdb = openGeminiJdkClient.dropDatabase(database);
        dropdb.get();
    }

    @SneakyThrows
    @Test
    void testRetentionPolicyError() {
        String testRpName = "testRpName";
        RpConfig rp = new RpConfig(testRpName + 0, "d3d", "", "");

        String database = "testRpDatabase0002";
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(database);
        createdb.get();

        CompletableFuture<Void> createRsp = openGeminiJdkClient.createRetentionPolicy(
                database, rp, Boolean.FALSE);

        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> createRsp.get());
        Assertions.assertInstanceOf(OpenGeminiException.class, e.getCause());
        Assertions.assertTrue(e.getCause().getMessage().contains(
                "syntax error: unexpected IDENT, expecting DURATIONVAL"));

        Thread.sleep(2000);

        CompletableFuture<List<RetentionPolicy>> showRpRsp = openGeminiJdkClient.showRetentionPolicies(database);
        List<RetentionPolicy> rsp = showRpRsp.get();
        CompletableFuture<Void> dropRsp = openGeminiJdkClient.dropRetentionPolicy(database, rp.getName());
        dropRsp.get();
        Assertions.assertFalse(rsp.contains(testRpName + 0));

        CompletableFuture<Void> dropdb = openGeminiJdkClient.dropDatabase(database);
        dropdb.get();
    }
}
