/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opengemini.client.spring.data.core;

import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.HttpClientEngine;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

    public Configuration.ConfigurationBuilder toConfigurationBuilder() {
        return Configuration.builder()
                .addresses(addresses.stream().map(this::toAddress).collect(Collectors.toList()))
                .httpConfig(new HttpClientConfig.Builder().engine(HttpClientEngine.OkHttp).build());
    }

    private Address toAddress(String s) {
        String[] strings = StringUtils.split(s, ':');
        return new Address(strings[0], Integer.parseInt(strings[1]));
    }
}
