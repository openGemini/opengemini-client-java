package io.opengemini.client.okhttp;

import io.opengemini.client.api.BaseConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class Configuration extends BaseConfiguration {

    private ConnectionPoolConfig connectionPoolConfig;

}
