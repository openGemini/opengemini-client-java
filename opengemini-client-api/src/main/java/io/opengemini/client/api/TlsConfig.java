package io.opengemini.client.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TlsConfig {
    public String keyStorePath;

    @ToString.Exclude
    public char[] keyStorePassword;

    public String trustStorePath;

    @ToString.Exclude
    public char[] trustStorePassword;

    public boolean tlsVerifyDisabled;

    public boolean tlsHostnameVerifyDisabled;

    public String[] tlsVersions;

    public String[] tlsCipherSuites;
}
