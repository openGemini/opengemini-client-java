package io.opengemini.client.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Address configuration for providing service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
