package io.opengemini.client.api;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseConfiguration {
    List<Address> addresses;

    AuthConfig authConfig;

    BatchConfig batchConfig;

    TlsConfig tlsConfig;

    public int timeout = 30;

    public int connectTimeout = 10;

    public boolean tlsEnabled;
}
