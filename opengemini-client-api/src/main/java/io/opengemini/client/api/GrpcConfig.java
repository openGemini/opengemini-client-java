package io.opengemini.client.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class GrpcConfig {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private boolean useSSL = false;
    private boolean waitForReady = true;
    private String caCertPath;
    private String clientCertPath;
    private String clientKeyPath;
}
