package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;

/**
 * AuthConfig represents the configuration information for authentication.
 */
@Getter
@Setter
public class AuthConfig {
    /**
     * AuthType type of identity authentication.
     */
    private AuthType authType;
    /**
     * Username provided username when used AuthTypePassword.
     */
    private String username;
    /**
     * Password provided password when used AuthTypePassword.
     */
    private String password;
    /**
     * Token provided token when used AuthTypeToken.
     */
    private String token;
}
