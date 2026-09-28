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

    public enum AuthField {
        NONE,
        DISPLAY_NAME,
        EMAIL,
        PASSWORD,
        CONFIRM_PASSWORD,
        GENERAL
    }

    public static class AuthValidationError {
        private final AuthField field;
        private final String message;

        public AuthValidationError(AuthField field, String message) {
            this.field = field;
            this.message = message;
        }

        public AuthField getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }

    private final AuthRepository authRepository;
    private final MutableLiveData<AuthMode> authMode = new MutableLiveData<>(AuthMode.LOGIN);
    private final MutableLiveData<UiState<User>> authState = new MutableLiveData<>();
    private final MutableLiveData<AuthValidationError> validationError = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = RepositoryProvider.getInstance().getAuthRepository();
    }

    public LiveData<AuthMode> getAuthMode() {
        return authMode;
    }

    public LiveData<UiState<User>> getAuthState() {
        return authState;
    }

    public LiveData<AuthValidationError> getValidationError() {
        return validationError;
    }

    public void showLogin() {
        authMode.setValue(AuthMode.LOGIN);
        authState.setValue(null);
        validationError.setValue(null);
    }

    public void showSignUp() {
        authMode.setValue(AuthMode.SIGN_UP);
        authState.setValue(null);
        validationError.setValue(null);
    }

    public void login(String email, String password) {
        AuthValidationError validationError = validateLogin(email, password);
        if (validationError != null) {
            this.validationError.setValue(validationError);
            authState.setValue(null);
            return;
        }

        this.validationError.setValue(null);
        authState.setValue(UiState.loading());
        authRepository.login(email, password, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authState.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                AuthValidationError mappedError = mapRepositoryError(message);
                validationError.setValue(mappedError);
                authState.setValue(UiState.error(message));
            }
        });
    }

    public void signUp(String displayName, String email, String password, String confirmPassword) {
        AuthValidationError validationError = validateSignUp(
                displayName, email, password, confirmPassword);
        if (validationError != null) {
            this.validationError.setValue(validationError);
            authState.setValue(null);
            return;
        }

        this.validationError.setValue(null);
        authState.setValue(UiState.loading());
        authRepository.signUp(displayName, email, password, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authState.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                AuthValidationError mappedError = mapRepositoryError(message);
                validationError.setValue(mappedError);
                authState.setValue(UiState.error(message));
            }
        });
    }

    public void clearAuthState() {
        authState.setValue(null);
        validationError.setValue(null);
    }

    private AuthValidationError validateLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new AuthValidationError(AuthField.EMAIL, "이메일을 입력해 주세요.");
        }
        if (!isValidEmail(email)) {
            return new AuthValidationError(AuthField.EMAIL, "올바른 이메일 형식을 입력해 주세요.");
        }
        if (password == null || password.isEmpty()) {
            return new AuthValidationError(AuthField.PASSWORD, "비밀번호를 입력해 주세요.");
        }
        return null;
    }

    private AuthValidationError validateSignUp(String displayName, String email, String password,
                                               String confirmPassword) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return new AuthValidationError(AuthField.DISPLAY_NAME, "이름을 입력해 주세요.");
        }
        if (email == null || email.trim().isEmpty()) {
            return new AuthValidationError(AuthField.EMAIL, "이메일을 입력해 주세요.");
        }
        if (!isValidEmail(email)) {
            return new AuthValidationError(AuthField.EMAIL, "올바른 이메일 형식을 입력해 주세요.");
        }
        if (password == null || password.length() < 8) {
            return new AuthValidationError(AuthField.PASSWORD, "비밀번호는 8자 이상이어야 합니다.");
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return new AuthValidationError(AuthField.CONFIRM_PASSWORD, "비밀번호가 일치하지 않습니다.");
        }
        return null;
    }

    private AuthValidationError mapRepositoryError(String message) {
        if (message == null || message.isEmpty()) {
            return new AuthValidationError(AuthField.GENERAL, "요청을 처리할 수 없습니다.");
        }
        if (message.contains("이메일") && message.contains("비밀번호")) {
            return new AuthValidationError(AuthField.GENERAL, message);
        }
        if (message.contains("이메일")) {
            return new AuthValidationError(AuthField.EMAIL, message);
        }
        if (message.contains("비밀번호")) {
            return new AuthValidationError(AuthField.PASSWORD, message);
        }
        if (message.contains("이름")) {
            return new AuthValidationError(AuthField.DISPLAY_NAME, message);
        }
        return new AuthValidationError(AuthField.GENERAL, message);
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
}
