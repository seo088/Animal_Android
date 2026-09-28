package com.animalloo.data.mock;

import android.content.Context;
import android.util.Patterns;

import com.animalloo.data.model.User;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.AuthSessionHelper;

import java.util.UUID;

/**
 * Local mock authentication backed by SharedPreferences.
 */
public class MockAuthRepository implements AuthRepository {

    private final Context appContext;

    public MockAuthRepository(Context context) {
        this.appContext = context.getApplicationContext();
        AuthSessionHelper.ensureDemoAccount(appContext);
    }

    @Override
    public void login(String email, String password, RepositoryCallback<User> callback) {
        MockAsyncHelper.getInstance().execute(() -> {
            String normalizedEmail = AuthSessionHelper.normalizeEmail(email);
            if (normalizedEmail.isEmpty() || password == null || password.isEmpty()) {
                throw new IllegalArgumentException("이메일과 비밀번호를 입력해 주세요.");
            }

            User user = AuthSessionHelper.authenticate(appContext, normalizedEmail, password);
            if (user == null) {
                throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
            }

            AuthSessionHelper.saveSession(appContext, user);
            return user;
        }, callback);
    }

    @Override
    public void signUp(String displayName, String email, String password,
                       RepositoryCallback<User> callback) {
        MockAsyncHelper.getInstance().execute(() -> {
            String normalizedEmail = AuthSessionHelper.normalizeEmail(email);
            String trimmedName = displayName == null ? "" : displayName.trim();

            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("이름을 입력해 주세요.");
            }
            if (normalizedEmail.isEmpty()) {
                throw new IllegalArgumentException("이메일을 입력해 주세요.");
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                throw new IllegalArgumentException("올바른 이메일 형식을 입력해 주세요.");
            }
            if (password == null || password.length() < 8) {
                throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
            }

            if (AuthSessionHelper.isEmailRegistered(appContext, normalizedEmail)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }

            String userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
            AuthSessionHelper.saveAccount(appContext, userId, trimmedName, normalizedEmail, password);
            User user = new User(userId, trimmedName, normalizedEmail);
            AuthSessionHelper.saveSession(appContext, user);
            return user;
        }, callback);
    }

    @Override
    public boolean isLoggedIn() {
        return AuthSessionHelper.getSessionUser(appContext) != null;
    }

    @Override
    public User getCurrentUser() {
        return AuthSessionHelper.getSessionUser(appContext);
    }

    @Override
    public void logout() {
        AuthSessionHelper.clearSession(appContext);
    }
}
