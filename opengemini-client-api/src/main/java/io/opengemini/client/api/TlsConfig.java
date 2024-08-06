package io.opengemini.client.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TlsConfig {
    public String keyStorePath;

    @ToString.Exclude
    public char[] keyStorePassword;

    public String trustStorePath;

    @ToString.Exclude
    public char[] trustStorePassword;

    public boolean verifyDisabled;

    public boolean hostnameVerifyDisabled;

    public String[] versions;

    public String[] cipherSuites;
}
