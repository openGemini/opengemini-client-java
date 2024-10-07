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

import io.opengemini.client.api.Configuration;

/**
 * Callback interface that can be implemented by beans wishing to customize
 * the {@link Configuration.ConfigurationBuilder}.
 */
@FunctionalInterface
public interface ClientConfigurationBuilderCustomizer {
    /**
     * Customize the {@link Configuration.ConfigurationBuilder}.
     *
     * @param configurationBuilder the configuration builder to customize
     */
    void customize(Configuration.ConfigurationBuilder configurationBuilder);
}
