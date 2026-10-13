package com.animalloo.data.remote;

import com.animalloo.data.model.User;
import com.animalloo.data.remote.dto.AuthResponse;
import com.animalloo.data.remote.dto.LoginRequest;
import com.animalloo.data.remote.dto.SignUpRequest;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.RepositoryCallback;

/**
 * POST /auth/login, /auth/signup. The JWT and user are kept in SessionStore so
 * isLoggedIn()/getCurrentUser() stay synchronous as the interface requires.
 */
public class RemoteAuthRepository implements AuthRepository {

    private final AnimalLooApi api;
    private final SessionStore sessionStore;
    private final Runnable onSessionChanged;

    public RemoteAuthRepository(AnimalLooApi api, SessionStore sessionStore, Runnable onSessionChanged) {
        this.api = api;
        this.sessionStore = sessionStore;
        this.onSessionChanged = onSessionChanged;
    }

    @Override
    public void login(String email, String password, RepositoryCallback<User> callback) {
        api.login(new LoginRequest(email == null ? "" : email.trim(), password))
                .enqueue(ApiCallbacks.map(callback, this::startSession));
    }

    @Override
    public void signUp(String displayName, String email, String password, RepositoryCallback<User> callback) {
        api.signUp(new SignUpRequest(
                        displayName == null ? "" : displayName.trim(),
                        email == null ? "" : email.trim(),
                        password))
                .enqueue(ApiCallbacks.map(callback, this::startSession));
    }

    private User startSession(AuthResponse response) {
        sessionStore.save(response.getToken(), response.getUser());
        onSessionChanged.run();
        return response.getUser();
    }

    @Override
    public boolean isLoggedIn() {
        return sessionStore.getToken() != null;
    }

    @Override
    public User getCurrentUser() {
        return sessionStore.getUser();
    }

    @Override
    public void logout() {
        sessionStore.clear();
        onSessionChanged.run();
    }
}
