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
