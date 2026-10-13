package com.animalloo.data.remote.dto;

public class SignUpRequest {

    private final String displayName;
    private final String email;
    private final String password;

    public SignUpRequest(String displayName, String email, String password) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }
}
