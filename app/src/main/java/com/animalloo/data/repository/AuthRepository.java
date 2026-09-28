package com.animalloo.data.repository;

import com.animalloo.data.model.User;

/**
 * Authentication repository interface.
 * Mock implementation stores accounts and session locally.
 */
public interface AuthRepository {

    void login(String email, String password, RepositoryCallback<User> callback);

    void signUp(String displayName, String email, String password, RepositoryCallback<User> callback);

    boolean isLoggedIn();

    User getCurrentUser();

    void logout();
}
