package io.opengemini.client.spring.data.config;

import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.impl.OpenGeminiClientFactory;
import io.opengemini.client.spring.data.core.ClientConfigurationBuilderCustomizer;
import io.opengemini.client.spring.data.core.DefaultOpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiProperties;
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
    public OpenGeminiAsyncClient openGeminiAsyncClient(OpenGeminiProperties openGeminiProperties,
                                                       ObjectProvider<ClientConfigurationBuilderCustomizer> customizers)
            throws OpenGeminiException {
        Configuration.ConfigurationBuilder configurationBuilder = openGeminiProperties.toConfigurationBuilder();
        customizers.orderedStream().forEach((customizer) -> customizer.customize(configurationBuilder));
        return OpenGeminiClientFactory.create(configurationBuilder.build());
    }

    @Bean
    @ConditionalOnMissingBean(OpenGeminiSerializerFactory.class)
    public OpenGeminiSerializerFactory openGeminiSerializerFactory() {
        return new DefaultOpenGeminiSerializerFactory();
    }

}
