package com.wikia.calabash.auth;

import lombok.Data;

/**
 * @author wikia
 * @since 4/20/2021 2:47 PM
 */
@Data
public class AuthClient {
    private Long id;
    private String name;
    private String clientKey;
    private String clientSecret;
}
