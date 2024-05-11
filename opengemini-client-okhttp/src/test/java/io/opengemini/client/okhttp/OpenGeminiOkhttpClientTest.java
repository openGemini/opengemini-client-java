package io.opengemini.client.okhttp;

import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.okhttp.dto.BatchPoints;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class OpenGeminiOkhttpClientTest {

    private OpenGeminiClient openGeminiClient;

    @BeforeEach
    void setUp() {
        this.openGeminiClient = OpenGeminiOkhttpClientFactory.connect("http://10.0.0.27:31583");
    }


    @Test
    @Deprecated
    void testDatabase() throws Exception {
        String databaseTestName = "testDatabase_0001";
        this.openGeminiClient.createDatabase(databaseTestName);

        System.out.println("ddd");
        Thread.sleep(3000);
    }


    private Point generalGpsTestPoint(String measurementName, String deviceCode) {
        Point testPoint = new Point();
        testPoint.setMeasurement(measurementName);
        testPoint.setTime(System.currentTimeMillis());
        HashMap<String, String> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        tags.put("deviceCode", deviceCode);

        fields.put("latitude", new Random().nextDouble());
        fields.put("longitude", new Random().nextDouble());
        fields.put("address", "address" + new Random().nextInt());
        fields.put("speed", new Random().nextDouble());
        fields.put("course", new Random().nextDouble());
        testPoint.setTags(tags);
        testPoint.setFields(fields);
        return testPoint;
    }


    /**
     * 实际使用GPS的服务
     */
    @Test
    @SneakyThrows
    void testGpsBatchPoints() {
        String databaseName = "rdwtest";
        //CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(databaseName);
        //createdb.get();
        String measurementName = "gps_device_positions";
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            points.add(generalGpsTestPoint(measurementName, "deviceCode" + i));
        }
        Long start = System.currentTimeMillis();
        BatchPoints.Builder builder = BatchPoints.database(databaseName).points(points);
        openGeminiClient.write(builder.build());
        System.out.println(String.format("使用时间%d", System.currentTimeMillis() - start));

        Thread.sleep(3000);
    }


    /**
     * 实际使用GPS的服务查询
     */
    @Test
    @SneakyThrows
    void testSelectBatchPoints() {
        String databaseName = "rdwtest";
        String measurementName = "gps_device_positions";
        Query selectQuery = new Query("select * from " + measurementName, databaseName, "");
        Long start = System.currentTimeMillis();
        QueryResult queryResult = openGeminiClient.query(selectQuery);
        System.out.println(String.format("使用时间%d", System.currentTimeMillis() - start));
        System.out.println(queryResult);
        Thread.sleep(10000);
    }
}
