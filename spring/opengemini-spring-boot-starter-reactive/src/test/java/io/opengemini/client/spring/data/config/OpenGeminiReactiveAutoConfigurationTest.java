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

import io.opengemini.client.spring.data.core.OpenGeminiProperties;
import io.opengemini.client.spring.data.core.OpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.ReactiveOpenGeminiTemplate;
import io.opengemini.client.spring.data.sample.TestReactiveApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@SpringBootTest(classes = TestReactiveApplication.class)
public class OpenGeminiReactiveAutoConfigurationTest {

    @Autowired
    private OpenGeminiProperties openGeminiProperties;

    @Autowired
    private ReactiveOpenGeminiTemplate reactiveOpenGeminiTemplate;

    @Autowired
    private OpenGeminiSerializerFactory openGeminiSerializerFactory;

    @Test
    public void properties_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiProperties);
        Assertions.assertEquals("localhost:8086", openGeminiProperties.getAddresses().get(0));
    }

    @Test
    public void reactive_template_bean_should_be_declared() {
        Assertions.assertNotNull(reactiveOpenGeminiTemplate);
    }

    @Test
    public void serializerFactory_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiSerializerFactory);
    }

}
