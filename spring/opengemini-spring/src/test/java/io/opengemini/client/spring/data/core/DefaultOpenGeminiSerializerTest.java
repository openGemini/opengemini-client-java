package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.Series;
import io.opengemini.client.api.SeriesResult;
import io.opengemini.client.spring.data.sample.measurement.WeatherFixNameNoCreate;
import io.opengemini.client.spring.data.sample.measurement.WeatherNoAnnotation;
import io.opengemini.client.spring.data.sample.measurement.WeatherTagNotString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

class DefaultOpenGeminiSerializerTest {

    @Test
    void serializer_should_fail_when_pojo_not_annotated() {
        Executable executable = () -> DefaultOpenGeminiSerializer.of(WeatherNoAnnotation.class);
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(
                "Class io.opengemini.client.spring.data.sample.measurement.WeatherNoAnnotation has no @Measurement "
                        + "annotation", exception.getMessage());
    }

    @Test
    void serializer_should_fail_when_tag_not_string() {
        Executable executable = () -> DefaultOpenGeminiSerializer.of(WeatherTagNotString.class);
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(
                "The location field type annotated with @Tag in Class io.opengemini.client.spring.data.sample"
                        + ".measurement.WeatherTagNotString should have a data type of [class java.lang.String]",
                exception.getMessage());
    }

    @Test
    void serialize_should_success_when_pojo_annotated() throws Exception {
        OpenGeminiSerializer<WeatherFixNameNoCreate> serializer = DefaultOpenGeminiSerializer.of(
                WeatherFixNameNoCreate.class);

        WeatherFixNameNoCreate weather = new WeatherFixNameNoCreate();
        weather.setLocation("shenzhen");
        weather.setTemperature(28.5D);
        weather.setTime(1725371248720L);

        Point point = serializer.serialize(weather);
        Assertions.assertNotNull(point);
        Assertions.assertEquals("testms", point.getMeasurement());
        Assertions.assertEquals("shenzhen", point.getTags().get("Location"));
        Assertions.assertEquals(28.5D, point.getFields().get("Temperature"));
        Assertions.assertEquals(weather.getTime(), point.getTime());
        Assertions.assertEquals("testms,Location=shenzhen Temperature=28.5 1725371248720000000", point.lineProtocol());
    }

    @Test
    void deserialize_should_throw_exception_when_queryResult_has_error() {
        OpenGeminiSerializer<WeatherFixNameNoCreate> serializer = DefaultOpenGeminiSerializer.of(
                WeatherFixNameNoCreate.class);

        QueryResult queryResult = new QueryResult();
        queryResult.setError("some error");

        Assertions.assertThrows(OpenGeminiException.class, () -> serializer.deserialize(queryResult));
    }

    @Test
    void deserialize_should_success_when_queryResult_matches() throws Exception {
        OpenGeminiSerializer<WeatherFixNameNoCreate> serializer = DefaultOpenGeminiSerializer.of(
                WeatherFixNameNoCreate.class);

        Series series = new Series();
        series.setName("testms");
        series.setColumns(Arrays.asList("Location", "Temperature", "time"));
        series.setValues(List.of(Arrays.asList("shenzhen", 28.5D, 1725371248720000000L),
                                 Arrays.asList("shanghai", 26.8D, 1725371248720000000L)));

        SeriesResult seriesResult = new SeriesResult();
        seriesResult.setSeries(List.of(series));

        QueryResult queryResult = new QueryResult();
        queryResult.setResults(List.of(seriesResult));

        List<WeatherFixNameNoCreate> list = serializer.deserialize(queryResult);

        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("shenzhen", list.get(0).getLocation());
        Assertions.assertEquals(28.5D, list.get(0).getTemperature());
        Assertions.assertEquals(1725371248720L, list.get(0).getTime());
        Assertions.assertEquals("shanghai", list.get(1).getLocation());
        Assertions.assertEquals(26.8D, list.get(1).getTemperature());
        Assertions.assertEquals(1725371248720L, list.get(1).getTime());
    }
}
