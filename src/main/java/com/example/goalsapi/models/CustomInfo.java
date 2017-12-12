package com.example.goalsapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomInfo {
    private String firstName;
    private String lastName;
    private String userId;
    private String[] roles;

}