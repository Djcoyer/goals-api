package com.example.goalsapi.models.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Users")
public class UserDao implements Serializable {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String auth0Id;
    private String emailAddress;
    private String refreshToken;
}
