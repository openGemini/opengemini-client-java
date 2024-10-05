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
