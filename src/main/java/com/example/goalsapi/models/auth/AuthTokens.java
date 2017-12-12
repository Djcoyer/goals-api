package com.example.goalsapi.models.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthTokens {
    private String id_token;
    private String access_token;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String refresh_token;
    private long expires_in;

    public AuthTokens() {}

    public AuthTokens(String idToken, String accessToken, String refreshToken, long expiresIn) {
        this.id_token= idToken;
        this.access_token = accessToken;
        this.refresh_token = refreshToken;
        this.expires_in = expiresIn;
    }
}
