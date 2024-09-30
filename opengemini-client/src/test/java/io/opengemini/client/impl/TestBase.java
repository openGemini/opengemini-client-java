package io.opengemini.client.impl;

import io.github.openfacade.http.HttpClientEngine;

class TestBase {
    protected HttpClientEngine httpEngine(OpenGeminiClient client) {
        return client.conf.getHttpConfig().engine();
    }
}
