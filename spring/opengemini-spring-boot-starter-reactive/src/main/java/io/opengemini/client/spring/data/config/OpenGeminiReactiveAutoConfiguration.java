package io.opengemini.client.spring.data.config;

import io.opengemini.client.spring.data.core.DefaultOpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiProperties;
import io.opengemini.client.spring.data.core.OpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.ReactiveOpenGeminiTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@AutoConfiguration
@ConditionalOnClass({ReactiveOpenGeminiTemplate.class, Flux.class})
@EnableConfigurationProperties(OpenGeminiProperties.class)
public class OpenGeminiReactiveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ReactiveOpenGeminiTemplate.class)
    public ReactiveOpenGeminiTemplate reactiveOpenGeminiTemplate() {
        return new ReactiveOpenGeminiTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(OpenGeminiSerializerFactory.class)
    public OpenGeminiSerializerFactory openGeminiSerializerFactory() {
        return new DefaultOpenGeminiSerializerFactory();
    }
}
