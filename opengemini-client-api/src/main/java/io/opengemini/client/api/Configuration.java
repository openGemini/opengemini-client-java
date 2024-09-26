package io.opengemini.client.api;

import io.github.openfacade.http.HttpClientConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    HttpClientConfig httpConfig;
}
