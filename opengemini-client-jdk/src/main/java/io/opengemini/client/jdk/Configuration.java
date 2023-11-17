package io.opengemini.client.jdk;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
public class Configuration {
    public String keyStorePath;

    @ToString.Exclude
    public String keyStorePassword;

    public String trustStorePath;

    @ToString.Exclude
    public String trustStorePassword;

    public boolean disableSslVerify;
}
