package io.opengemini.client.spring.data.core;

import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.HttpClientEngine;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration properties for OpenGemini.
 */
@ConfigurationProperties("spring.opengemini")
@Getter
@Setter
public class OpenGeminiProperties {

    private List<String> addresses = new ArrayList<>(Collections.singletonList("localhost:8086"));

    public Configuration toConfiguration() {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder().engine(HttpClientEngine.OkHttp)
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(3))
                .build();
        return Configuration.builder()
                .addresses(addresses.stream().map(this::toAddress).collect(Collectors.toList()))
                .httpConfig(httpConfig)
                .build();
    }

    private Address toAddress(String s) {
        String[] strings = StringUtils.split(s, ':');
        return new Address(strings[0], Integer.parseInt(strings[1]));
    }
}
