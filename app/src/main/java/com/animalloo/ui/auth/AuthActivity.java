package com.animalloo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.animalloo.R;
import com.animalloo.databinding.ActivityAuthBinding;
import com.animalloo.notification.NotificationHelper;
import com.animalloo.ui.main.MainActivity;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthViewModel viewModel;
    private boolean isSubmitting;
    private AuthViewModel.AuthMode lastRenderedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();
        setupActions();
        setupKeyboardActions();
        setupFieldChangeListeners();
    }

    private void setupObservers() {
        viewModel.getAuthMode().observe(this, mode -> {
            if (mode == null) {
                return;
            }
            if (mode != lastRenderedMode) {
                applyModeUi(mode);
                lastRenderedMode = mode;
            }
        });

        viewModel.getValidationError().observe(this, error -> {
            clearFieldErrors();
            if (error == null) {
                hideError();
                return;
            }
            applyFieldError(error);
        });

        viewModel.getAuthState().observe(this, state -> {
            if (state == null) {
                hideError();
                setSubmitting(false);
                return;
            }

            if (state.isLoading()) {
                setSubmitting(true);
                hideError();
                return;
            }

            setSubmitting(false);

            if (state.isError()) {
                AuthViewModel.AuthValidationError validationError =
                        viewModel.getValidationError().getValue();
                if (validationError == null
                        || validationError.getField() == AuthViewModel.AuthField.GENERAL) {
                    showError(state.getErrorMessage());
                } else {
                    hideError();
                }
                return;
            }

            if (state.isSuccess()) {
                navigateToMain();
            }
        });
    }

    private void setupActions() {
        binding.btnSubmit.setOnClickListener(v -> submitCurrentForm());
        binding.btnToggleMode.setOnClickListener(v -> {
            AuthViewModel.AuthMode currentMode = viewModel.getAuthMode().getValue();
            if (currentMode == AuthViewModel.AuthMode.SIGN_UP) {
                viewModel.showLogin();
            } else {
                viewModel.showSignUp();
            }
        });
    }

    private void setupKeyboardActions() {
        binding.etDisplayName.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etEmail.requestFocus();
                return true;
            }
            return false;
        });

        binding.etEmail.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etPassword.requestFocus();
                return true;
            }
            return false;
        });

        binding.etPassword.setOnEditorActionListener((textView, actionId, event) -> {
            AuthViewModel.AuthMode mode = viewModel.getAuthMode().getValue();
            if (mode == AuthViewModel.AuthMode.SIGN_UP
                    && actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etConfirmPassword.requestFocus();
                return true;
            }
            if (mode == AuthViewModel.AuthMode.LOGIN
                    && (actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_GO)) {
                submitCurrentForm();
                return true;
            }
            return false;
        });

        binding.etConfirmPassword.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitCurrentForm();
                return true;
            }
            return false;
        });
    }

    private void setupFieldChangeListeners() {
        binding.etDisplayName.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.layoutDisplayName.setError(null);
            }
        });
        binding.etEmail.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.layoutEmail.setError(null);
            }
        });
        binding.etPassword.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.layoutPassword.setError(null);
            }
        });
        binding.etConfirmPassword.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.layoutConfirmPassword.setError(null);
            }
        });
    }

    private void submitCurrentForm() {
        if (isSubmitting) {
            return;
        }

        clearFieldErrors();
        hideError();

        AuthViewModel.AuthMode mode = viewModel.getAuthMode().getValue();
        if (mode == AuthViewModel.AuthMode.SIGN_UP) {
            viewModel.signUp(
                    binding.etDisplayName.getText() != null
                            ? binding.etDisplayName.getText().toString() : "",
                    binding.etEmail.getText() != null
                            ? binding.etEmail.getText().toString() : "",
                    binding.etPassword.getText() != null
                            ? binding.etPassword.getText().toString() : "",
                    binding.etConfirmPassword.getText() != null
                            ? binding.etConfirmPassword.getText().toString() : "");
        } else {
            viewModel.login(
                    binding.etEmail.getText() != null
                            ? binding.etEmail.getText().toString() : "",
                    binding.etPassword.getText() != null
                            ? binding.etPassword.getText().toString() : "");
        }
    }

    private void applyModeUi(AuthViewModel.AuthMode mode) {
        boolean isSignUp = mode == AuthViewModel.AuthMode.SIGN_UP;
        binding.tvAuthTitle.setText(isSignUp
                ? R.string.auth_sign_up_title
                : R.string.auth_login_title);
        binding.tvAuthSubtitle.setText(isSignUp
                ? R.string.auth_sign_up_subtitle
                : R.string.auth_login_subtitle);
        binding.btnSubmit.setText(isSignUp
                ? R.string.auth_sign_up_button
                : R.string.auth_login_button);
        binding.btnToggleMode.setText(isSignUp
                ? R.string.auth_go_to_login
                : R.string.auth_go_to_sign_up);

        binding.layoutDisplayName.setVisibility(isSignUp ? View.VISIBLE : View.GONE);
        binding.layoutConfirmPassword.setVisibility(isSignUp ? View.VISIBLE : View.GONE);

        binding.etDisplayName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.etEmail.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.etPassword.setImeOptions(isSignUp
                ? EditorInfo.IME_ACTION_NEXT
                : EditorInfo.IME_ACTION_DONE);
        binding.etConfirmPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);

        clearFieldErrors();
        hideError();
    }

    private void applyFieldError(AuthViewModel.AuthValidationError error) {
        TextInputLayout targetLayout = null;
        switch (error.getField()) {
            case DISPLAY_NAME:
                targetLayout = binding.layoutDisplayName;
                break;
            case EMAIL:
                targetLayout = binding.layoutEmail;
                break;
            case PASSWORD:
                targetLayout = binding.layoutPassword;
                break;
            case CONFIRM_PASSWORD:
                targetLayout = binding.layoutConfirmPassword;
                break;
            case GENERAL:
                showError(error.getMessage());
                return;
            case NONE:
            default:
                break;
        }

        if (targetLayout != null) {
            targetLayout.setError(error.getMessage());
        } else {
            showError(error.getMessage());
        }
    }

    private void clearFieldErrors() {
        binding.layoutDisplayName.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        binding.btnSubmit.setEnabled(!submitting);
        binding.btnToggleMode.setEnabled(!submitting);
        binding.etDisplayName.setEnabled(!submitting);
        binding.etEmail.setEnabled(!submitting);
        binding.etPassword.setEnabled(!submitting);
        binding.etConfirmPassword.setEnabled(!submitting);
        binding.btnSubmit.setText(submitting
                ? getString(R.string.auth_submitting)
                : getSubmitButtonText());
    }

    private String getSubmitButtonText() {
        AuthViewModel.AuthMode mode = viewModel.getAuthMode().getValue();
        return getString(mode == AuthViewModel.AuthMode.SIGN_UP
                ? R.string.auth_sign_up_button
                : R.string.auth_login_button);
    }

    private void showError(String message) {
        binding.tvAuthError.setText(message);
        binding.tvAuthError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvAuthError.setVisibility(View.GONE);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        Intent sourceIntent = getIntent();
        if (sourceIntent != null) {
            if (sourceIntent.hasExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB)) {
                intent.putExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB,
                        sourceIntent.getIntExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB, 0));
            }
            if (sourceIntent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_HOME, false)) {
                intent.putExtra(NotificationHelper.EXTRA_OPEN_HOME, true);
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
