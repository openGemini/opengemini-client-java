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
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.impl.OpenGeminiClientFactory;
import io.opengemini.client.spring.data.core.ClientConfigurationBuilderCustomizer;
import io.opengemini.client.spring.data.core.DefaultOpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiProperties;
import io.opengemini.client.spring.data.core.OpenGeminiPropertiesConverter;
import io.opengemini.client.spring.data.core.OpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OpenGeminiTemplate.class)
@EnableConfigurationProperties(OpenGeminiProperties.class)
public class OpenGeminiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OpenGeminiTemplate.class)
    public OpenGeminiTemplate openGeminiTemplate(OpenGeminiAsyncClient openGeminiAsyncClient,
                                                 OpenGeminiSerializerFactory openGeminiSerializerFactory) {
        return new OpenGeminiTemplate(openGeminiAsyncClient, openGeminiSerializerFactory);
    }

    @Bean
    @ConditionalOnMissingBean(OpenGeminiAsyncClient.class)
    public OpenGeminiAsyncClient openGeminiAsyncClient(OpenGeminiProperties properties,
                                                       ObjectProvider<ClientConfigurationBuilderCustomizer> customizers)
            throws OpenGeminiException {
        OpenGeminiPropertiesConverter converter = new OpenGeminiPropertiesConverter(properties, customizers);
        return OpenGeminiClientFactory.create(converter.toConfiguration());
    }

    @Bean
    @ConditionalOnMissingBean(OpenGeminiSerializerFactory.class)
    public OpenGeminiSerializerFactory openGeminiSerializerFactory() {
        return new DefaultOpenGeminiSerializerFactory();
    }

}
