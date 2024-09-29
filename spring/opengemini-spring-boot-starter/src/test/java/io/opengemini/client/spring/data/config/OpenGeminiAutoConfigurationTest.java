package io.opengemini.client.spring.data.config;

import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.spring.data.core.OpenGeminiProperties;
import io.opengemini.client.spring.data.core.OpenGeminiSerializerFactory;
import io.opengemini.client.spring.data.core.OpenGeminiTemplate;
import io.opengemini.client.spring.data.sample.TestApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@SpringBootTest(classes = TestApplication.class)
public class OpenGeminiAutoConfigurationTest {

    @Autowired
    private OpenGeminiProperties openGeminiProperties;

    @Autowired
    private OpenGeminiTemplate openGeminiTemplate;

    @Autowired
    private OpenGeminiAsyncClient openGeminiAsyncClient;

    @Autowired
    private OpenGeminiSerializerFactory openGeminiSerializerFactory;

    @Test
    public void properties_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiProperties);
        Assertions.assertEquals("localhost:8086", openGeminiProperties.getAddresses().get(0));
    }

    @Test
    public void template_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiTemplate);
    }

    @Test
    public void serializerFactory_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiSerializerFactory);
    }

    @Test
    public void asyncClient_bean_should_be_declared() {
        Assertions.assertNotNull(openGeminiAsyncClient);
    }

}
