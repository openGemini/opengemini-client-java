package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;

/**
 * Address configuration for providing service.
 */
@Getter
@Setter
public class Address {
    /**
     * Host service ip or domain.
     */
    private String host;
    /**
     * Port exposed service port.
     */
    private int port;
}
