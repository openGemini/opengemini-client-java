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

import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.spring.data.core.OpenGeminiProperties;
import io.opengemini.client.spring.data.core.OpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiTemplate;
import io.opengemini.client.spring.data.sample.TestApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

@SpringJUnitConfig
@SpringBootTest(classes = TestApplication.class)
public class OpenGeminiAutoConfigurationTest {

    @Autowired
    private OpenGeminiProperties openGeminiProperties;

    @Autowired
    private OpenGeminiTemplate openGeminiTemplate;

    @Autowired
    private OpenGeminiAsyncClient openGeminiAsyncClient;

    @Autowired
    private OpenGeminiSerializerFactory openGeminiSerializerFactory;

    @Test
    public void properties_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiProperties);
        Assertions.assertEquals("localhost:8086", openGeminiProperties.getAddresses().get(0));
    }

    @Test
    public void template_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiTemplate);
    }

    @Test
    public void serializerFactory_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiSerializerFactory);
    }

    @Test
    public void asyncClient_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiAsyncClient);
    }

    @Test
    void configurationCustomizer_bean_should_be_effective() {
        Configuration conf = (Configuration) ReflectionTestUtils.getField(openGeminiAsyncClient, "conf");

        Assertions.assertNotNull(conf);
        Assertions.assertEquals(Duration.ofSeconds(10), conf.getHttpConfig().connectTimeout());
    }
}
