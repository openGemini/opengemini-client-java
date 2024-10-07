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
