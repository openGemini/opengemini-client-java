package io.opengemini.client.spring.data.config;

import io.opengemini.client.spring.data.core.ReactiveOpenGeminiTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@AutoConfiguration(after = OpenGeminiAutoConfiguration.class)
@ConditionalOnClass({ReactiveOpenGeminiTemplate.class, Flux.class})
public class OpenGeminiReactiveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "reactiveOpenGeminiTemplate")
    public ReactiveOpenGeminiTemplate reactiveOpenGeminiTemplate() {
        return new ReactiveOpenGeminiTemplate();
    }

}
