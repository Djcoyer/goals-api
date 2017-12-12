package com.example.goalsapi.models.auth.management;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResult {
    public String access_token;
    @JsonIgnore
    public String token_type;
    @JsonIgnore
    public String scope;
    public String expires_in;

}

