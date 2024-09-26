package io.opengemini.client.okhttp;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class ConnectionPoolConfig {

    private int maxIdleConnections = 5;

    private long keepAliveDuration = 5;

    private TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;

    public static ConnectionPoolConfig of(int maxIdleConnections, long keepAliveDuration, TimeUnit keepAliveTimeUnit) {
        ConnectionPoolConfig config = new ConnectionPoolConfig();
        config.setMaxIdleConnections(maxIdleConnections);
        config.setKeepAliveDuration(keepAliveDuration);
        config.setKeepAliveTimeUnit(keepAliveTimeUnit);
        return config;
    }

}
