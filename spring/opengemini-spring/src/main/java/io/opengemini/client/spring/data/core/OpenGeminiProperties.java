package io.opengemini.client.spring.data.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration properties for OpenGemini.
 */
@ConfigurationProperties("spring.opengemini")
@Getter
@Setter
public class OpenGeminiProperties {

    private List<String> addresses = new ArrayList<>(Collections.singletonList("localhost:8086"));

}
