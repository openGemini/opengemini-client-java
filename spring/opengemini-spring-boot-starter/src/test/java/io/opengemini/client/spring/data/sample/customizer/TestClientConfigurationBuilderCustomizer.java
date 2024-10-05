package io.opengemini.client.spring.data.sample.customizer;

import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.HttpClientEngine;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.spring.data.core.ClientConfigurationBuilderCustomizer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TestClientConfigurationBuilderCustomizer implements ClientConfigurationBuilderCustomizer {
    @Override
    public void customize(Configuration.ConfigurationBuilder configurationBuilder) {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .engine(HttpClientEngine.OkHttp)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        configurationBuilder.httpConfig(httpConfig);
    }
}
