package io.opengemini.client.api;

/**
 * AuthType type of identity authentication.
 */
public enum AuthType {
    /**
     * AuthTypePassword Basic Authentication with the provided username and password.
     */
    PASSWORD,
    /**
     * AuthTypeToken Token Authentication with the provided token.
     */
    TOKEN
}
