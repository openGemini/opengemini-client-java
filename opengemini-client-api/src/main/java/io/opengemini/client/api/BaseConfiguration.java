package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
public class BaseConfiguration {
    List<Address> addresses;

    AuthConfig authConfig;

    BatchConfig batchConfig;

    public String keyStorePath;

    @ToString.Exclude
    public String keyStorePassword;

    public String trustStorePath;

    @ToString.Exclude
    public String trustStorePassword;

    public boolean tlsVerificationDisabled;
}
