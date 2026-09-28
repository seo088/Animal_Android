package com.animalloo.ui.auth;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.UiState;
import com.animalloo.data.model.User;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

public class AuthViewModel extends ViewModel {

    public enum AuthMode {
        LOGIN,
        SIGN_UP
    }

    private final AuthRepository authRepository;
    private final MutableLiveData<AuthMode> authMode = new MutableLiveData<>(AuthMode.LOGIN);
    private final MutableLiveData<UiState<User>> authState = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = RepositoryProvider.getInstance().getAuthRepository();
    }

    public LiveData<AuthMode> getAuthMode() {
        return authMode;
    }

    public LiveData<UiState<User>> getAuthState() {
        return authState;
    }

    public void showLogin() {
        authMode.setValue(AuthMode.LOGIN);
        authState.setValue(null);
    }

    public void showSignUp() {
        authMode.setValue(AuthMode.SIGN_UP);
        authState.setValue(null);
    }

    public void login(String email, String password) {
        String validationError = validateLogin(email, password);
        if (validationError != null) {
            authState.setValue(UiState.error(validationError));
            return;
        }

        authState.setValue(UiState.loading());
        authRepository.login(email, password, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authState.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                authState.setValue(UiState.error(message));
            }
        });
    }

    public void signUp(String displayName, String email, String password, String confirmPassword) {
        String validationError = validateSignUp(displayName, email, password, confirmPassword);
        if (validationError != null) {
            authState.setValue(UiState.error(validationError));
            return;
        }

        authState.setValue(UiState.loading());
        authRepository.signUp(displayName, email, password, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authState.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                authState.setValue(UiState.error(message));
            }
        });
    }

    public void clearAuthState() {
        authState.setValue(null);
    }

    private String validateLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "이메일을 입력해 주세요.";
        }
        if (!isValidEmail(email)) {
            return "올바른 이메일 형식을 입력해 주세요.";
        }
        if (password == null || password.isEmpty()) {
            return "비밀번호를 입력해 주세요.";
        }
        return null;
    }

    private String validateSignUp(String displayName, String email, String password,
                                  String confirmPassword) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return "이름을 입력해 주세요.";
        }
        if (email == null || email.trim().isEmpty()) {
            return "이메일을 입력해 주세요.";
        }
        if (!isValidEmail(email)) {
            return "올바른 이메일 형식을 입력해 주세요.";
        }
        if (password == null || password.length() < 8) {
            return "비밀번호는 8자 이상이어야 합니다.";
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return "비밀번호가 일치하지 않습니다.";
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
}
