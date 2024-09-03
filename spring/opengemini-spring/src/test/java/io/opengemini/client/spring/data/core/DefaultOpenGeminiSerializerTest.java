package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.Point;
import io.opengemini.client.spring.data.sample.measurement.WeatherFixNameNoCreate;
import io.opengemini.client.spring.data.sample.measurement.WeatherNoAnnotation;
import io.opengemini.client.spring.data.sample.measurement.WeatherTagNotString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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
                        + ".measurement.WeatherTagNotString should have a data type of String", exception.getMessage());
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
}
