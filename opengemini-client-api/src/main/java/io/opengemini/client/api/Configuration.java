package io.opengemini.client.api;

import io.github.shoothzj.http.facade.core.TlsConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
    List<Address> addresses;

    AuthConfig authConfig;

    BatchConfig batchConfig;

    boolean gzipEnabled;

    boolean tlsEnabled;

    TlsConfig tlsConfig;

    Duration timeout;

    Duration connectTimeout;
}
