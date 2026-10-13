package com.animalloo.data.remote.dto;

import com.animalloo.data.model.User;

public class AuthResponse {

    private String token;
    private User user;

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
