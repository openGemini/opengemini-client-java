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

package io.opengemini.client.impl;

import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.BatchConfig;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import org.jetbrains.annotations.NotNull;

public class OpenGeminiClientFactory {
    public static OpenGeminiClient create(@NotNull Configuration configuration) throws OpenGeminiException {
        if (configuration.getAddresses() == null || configuration.getAddresses().isEmpty()) {
            throw new OpenGeminiException("must have at least one address");
        }
        AuthConfig authConfig = configuration.getAuthConfig();
        if (authConfig != null) {
            if (authConfig.getAuthType() == AuthType.TOKEN && (authConfig.getToken() == null
                    || authConfig.getToken().isEmpty())) {
                throw new OpenGeminiException("invalid auth config due to empty token");
            }
            if (authConfig.getAuthType() == AuthType.PASSWORD) {
                if (authConfig.getUsername() == null || authConfig.getUsername().isEmpty()) {
                    throw new OpenGeminiException("invalid auth config due to empty username");
                }
                if (authConfig.getPassword() == null || authConfig.getPassword().length == 0) {
                    throw new OpenGeminiException("invalid auth config due to empty password");
                }
            }
        }
        BatchConfig batchConfig = configuration.getBatchConfig();
        if (batchConfig != null) {
            if (batchConfig.getBatchInterval() <= 0) {
                throw new OpenGeminiException("batch enabled, batch interval must be great than 0");
            }
            if (batchConfig.getBatchSize() <= 0) {
                throw new OpenGeminiException("batch enabled, batch size must be great than 0");
            }
        }
        return new OpenGeminiClient(configuration);
    }
}