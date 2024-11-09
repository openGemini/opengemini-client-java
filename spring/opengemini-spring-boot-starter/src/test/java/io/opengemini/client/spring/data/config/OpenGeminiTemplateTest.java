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

package io.opengemini.client.spring.data.config;

import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.RpConfig;
import io.opengemini.client.spring.data.core.MeasurementOperations;
import io.opengemini.client.spring.data.core.OpenGeminiTemplate;
import io.opengemini.client.spring.data.sample.TestApplication;
import io.opengemini.client.spring.data.sample.measurement.WeatherFixNameAutoCreate;
import io.opengemini.client.spring.data.sample.measurement.WeatherFixNameNoCreate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

@SpringJUnitConfig
@SpringBootTest(classes = TestApplication.class)
public class OpenGeminiTemplateTest {

    @Autowired
    private OpenGeminiTemplate openGeminiTemplate;

    @Autowired
    private OpenGeminiAsyncClient openGeminiAsyncClient;

    @Test
    public void write_with_measurement_operations() throws Exception {
        String databaseName = "weather_db";
        openGeminiAsyncClient.createDatabase(databaseName).get();

        String rpName = "weather_rp";
        RpConfig rpConfig = new RpConfig(rpName, "3d", "", "");
        openGeminiAsyncClient.createRetentionPolicy(databaseName, rpConfig, false).get();

        MeasurementOperations<WeatherFixNameNoCreate> measurementOperations = openGeminiTemplate.opsForMeasurement(
                WeatherFixNameNoCreate.class);
        WeatherFixNameNoCreate weatherForWrite = new WeatherFixNameNoCreate();
        weatherForWrite.setLocation("shenzhen");
        weatherForWrite.setTemperature(28.5D);
        weatherForWrite.setTime(System.currentTimeMillis());
        measurementOperations.write(weatherForWrite);

        Thread.sleep(5000);

        String measurementName = "weather_ms";
        Query selectQuery = new Query("select * from " + measurementName, databaseName, rpName);
        List<WeatherFixNameNoCreate> weatherList = measurementOperations.query(selectQuery);

        openGeminiAsyncClient.dropRetentionPolicy(databaseName, rpName).get();
        openGeminiAsyncClient.dropDatabase(databaseName).get();

        Assertions.assertEquals(weatherList.size(), 1);
        WeatherFixNameNoCreate weather1 = weatherList.get(0);
        Assertions.assertEquals(weatherForWrite.getLocation(), weather1.getLocation());
        Assertions.assertEquals(weatherForWrite.getTemperature(), weather1.getTemperature());
        Assertions.assertEquals(weatherForWrite.getTime(), weather1.getTime());
    }

    @Test
    void database_should_auto_created() throws Exception {
        String databaseName = "weather_db_auto_create";
        String rpName = "weather_rp_auto_create";

        Assertions.assertTrue(openGeminiTemplate.isDatabaseExists(databaseName));
        Assertions.assertTrue(openGeminiTemplate.isRetentionPolicyExists(databaseName, rpName));

        MeasurementOperations<WeatherFixNameAutoCreate> measurementOperations = openGeminiTemplate.opsForMeasurement(
                WeatherFixNameAutoCreate.class);
        WeatherFixNameAutoCreate weatherForWrite = new WeatherFixNameAutoCreate();
        weatherForWrite.setLocation("shenzhen");
        weatherForWrite.setTemperature(28.5D);
        weatherForWrite.setTime(System.currentTimeMillis());
        measurementOperations.write(weatherForWrite);

        Thread.sleep(5000);

        String measurementName = "weather_ms";
        Query selectQuery = new Query("select * from " + measurementName, databaseName, rpName);
        List<WeatherFixNameAutoCreate> weatherList = measurementOperations.query(selectQuery);

        openGeminiTemplate.dropRetentionPolicy(databaseName, rpName);
        openGeminiTemplate.dropDatabase(databaseName);

        Assertions.assertEquals(weatherList.size(), 1);
        WeatherFixNameAutoCreate weather1 = weatherList.get(0);
        Assertions.assertEquals(weatherForWrite.getLocation(), weather1.getLocation());
        Assertions.assertEquals(weatherForWrite.getTemperature(), weather1.getTemperature());
        Assertions.assertEquals(weatherForWrite.getTime(), weather1.getTime());
    }
}
