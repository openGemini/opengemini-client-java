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

import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.HttpClientEngine;
import io.opengemini.client.api.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.time.Duration;

class OpenGeminiPropertiesConverterTest {

    @Test
    void toConfiguration_should_right_for_convert_http_config() {
        OpenGeminiProperties.Http http = new OpenGeminiProperties.Http();
        http.setEngine(HttpClientEngine.OkHttp);
        http.setTimeout(Duration.ofSeconds(30));
        http.setConnectTimeout(Duration.ofSeconds(10));

        OpenGeminiProperties properties = new OpenGeminiProperties();
        properties.setHttp(http);

        ObjectProvider<ClientConfigurationBuilderCustomizer> provider =
                new StaticListableBeanFactory().getBeanProvider(ClientConfigurationBuilderCustomizer.class);
        OpenGeminiPropertiesConverter converter = new OpenGeminiPropertiesConverter(properties, provider);

        Configuration configuration = converter.toConfiguration();
        HttpClientConfig httpConfig = configuration.getHttpConfig();

        Assertions.assertEquals(http.getEngine(), httpConfig.engine());
        Assertions.assertEquals(http.getTimeout(), httpConfig.timeout());
        Assertions.assertEquals(http.getConnectTimeout(), httpConfig.connectTimeout());
    }
}
