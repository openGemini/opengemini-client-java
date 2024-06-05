package io.opengemini.client.api;

import lombok.Getter;

@Getter
public class Pong {

    /**
     * the version of OpenGemini server.
     */
    private final String version;

    public Pong(String version) {
        this.version = version;
    }

}
