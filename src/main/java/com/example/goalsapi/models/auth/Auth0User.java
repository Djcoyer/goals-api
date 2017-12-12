package com.example.goalsapi.models.auth;

import com.example.goalsapi.models.auth.AuthProperties.AppMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auth0User {

    private String auth0Id;
    private AppMetadata appMetadata;
}
