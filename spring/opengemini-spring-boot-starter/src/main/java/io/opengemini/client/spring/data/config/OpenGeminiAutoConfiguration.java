package io.opengemini.client.spring.data.config;

import io.opengemini.client.spring.data.core.DefaultOpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiProperties;
import io.opengemini.client.spring.data.core.OpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiTemplate;
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
    @ConditionalOnMissingBean(name = "openGeminiTemplate")
    public OpenGeminiTemplate openGeminiTemplate() {
        return new OpenGeminiTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(name = "openGeminiSerializerFactory")
    public OpenGeminiSerializerFactory openGeminiSerializerFactory() {
        return new DefaultOpenGeminiSerializerFactory();
    }

}
