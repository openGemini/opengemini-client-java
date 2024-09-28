package io.opengemini.client.spring.data.core;

import io.opengemini.client.spring.data.sample.measurement.WeatherFixNameNoCreate;
import io.opengemini.client.spring.data.sample.measurement.WeatherNoAnnotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class DefaultOpenGeminiSerializerFactoryTest {

    private OpenGeminiSerializerFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultOpenGeminiSerializerFactory();
    }

    @Test
    void serializer_should_fail_when_pojo_not_annotated() {
        Executable executable = () -> factory.getSerializer(WeatherNoAnnotation.class);
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(
                "Class io.opengemini.client.spring.data.sample.measurement.WeatherNoAnnotation has no @Measurement "
                        + "annotation", exception.getMessage());
    }

    @Test
    void getSerializer_should_success_when_pojo_annotated() {
        OpenGeminiSerializer<WeatherFixNameNoCreate> serializer1 = factory.getSerializer(WeatherFixNameNoCreate.class);
        OpenGeminiSerializer<WeatherFixNameNoCreate> serializer2 = factory.getSerializer(WeatherFixNameNoCreate.class);

        Assertions.assertNotNull(serializer1);
        Assertions.assertNotNull(serializer2);
        Assertions.assertSame(serializer1, serializer2);
    }
}
