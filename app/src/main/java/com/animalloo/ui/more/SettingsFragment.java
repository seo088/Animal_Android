package com.animalloo.ui.more;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.animalloo.R;
import com.animalloo.databinding.FragmentSettingsBinding;
import com.animalloo.notification.FcmTokenStore;
import com.animalloo.notification.NotificationHelper;
import com.animalloo.ui.auth.AuthActivity;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.util.FirebaseAvailabilityChecker;
import com.animalloo.util.ImageFileHelper;
import com.animalloo.util.PermissionHelper;
import com.animalloo.util.SettingsPreferenceHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends BaseFragment {

    private static final long TEST_NOTIFICATION_COOLDOWN_MS = 1500L;
    private static final String STATE_PENDING_SWITCH = "pending_notification_switch";

    private enum PendingNotificationSwitch {
        NONE,
        RESCUE,
        LOST
    }

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private PendingNotificationSwitch pendingNotificationSwitch = PendingNotificationSwitch.NONE;
    private boolean suppressSwitchCallbacks;
    private long lastTestNotificationAt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pendingNotificationSwitch = PendingNotificationSwitch.valueOf(
                    savedInstanceState.getString(
                            STATE_PENDING_SWITCH,
                            PendingNotificationSwitch.NONE.name()));
        }
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!isBindingAvailable()) {
                        return;
                    }
                    if (granted) {
                        applyPendingNotificationSwitch();
                        updateNotificationPermissionStatus();
                        return;
                    }
                    pendingNotificationSwitch = PendingNotificationSwitch.NONE;
                    loadSettings();
                    showPermissionDeniedSnackbar();
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragment() instanceof MoreHost) {
                ((MoreHost) getParentFragment()).onMoreBackPressed();
            }
        });

        setupAccountObservers();
        setupLogoutAction();
        setupSwitchListeners();
        setupUtilityActions();
        updateAppInfo();
        updateFcmStatus();
        updateCacheInfo();
        updateNotificationPermissionStatus();
        loadSettings();
        viewModel.loadAccount();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isBindingAvailable()) {
            return;
        }
        loadSettings();
        updateNotificationPermissionStatus();
        updateFcmStatus();
    }

    private void setupAccountObservers() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                binding.tvAccountName.setText(R.string.settings_account_unavailable);
                binding.tvAccountEmail.setVisibility(View.GONE);
                binding.btnLogout.setEnabled(false);
                binding.btnRetryAccount.setVisibility(View.VISIBLE);
                return;
            }

            binding.tvAccountName.setText(user.getDisplayName());
            binding.tvAccountEmail.setText(user.getEmail());
            binding.tvAccountEmail.setVisibility(View.VISIBLE);
            binding.btnLogout.setEnabled(true);
            binding.btnRetryAccount.setVisibility(View.GONE);
        });

        viewModel.getLogoutComplete().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                redirectToAuth();
            }
        });

        viewModel.getLogoutInProgress().observe(getViewLifecycleOwner(), inProgress -> {
            binding.btnLogout.setEnabled(!Boolean.TRUE.equals(inProgress));
        });
    }

    private void setupLogoutAction() {
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        binding.btnRetryAccount.setOnClickListener(v -> viewModel.loadAccount());
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_logout_confirm_title)
                .setMessage(R.string.settings_logout_confirm_message)
                .setNegativeButton(R.string.settings_logout_confirm_negative, null)
                .setPositiveButton(R.string.settings_logout_confirm_positive, (dialog, which) ->
                        viewModel.logout())
                .show();
    }

    private void redirectToAuth() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadSettings() {
        suppressSwitchCallbacks = true;
        binding.switchRescueAlert.setChecked(
                SettingsPreferenceHelper.isRescueAlertEnabled(requireContext()));
        binding.switchLostAlert.setChecked(
                SettingsPreferenceHelper.isLostAlertEnabled(requireContext()));
        binding.switchLocationAlert.setChecked(
                SettingsPreferenceHelper.isLocationAlertEnabled(requireContext()));
        suppressSwitchCallbacks = false;
    }

    private void setupSwitchListeners() {
        binding.switchRescueAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressSwitchCallbacks) {
                return;
            }
            if (isChecked && !PermissionHelper.hasNotificationPermission(requireContext())) {
                pendingNotificationSwitch = PendingNotificationSwitch.RESCUE;
                suppressSwitchCallbacks = true;
                binding.switchRescueAlert.setChecked(false);
                suppressSwitchCallbacks = false;
                requestNotificationPermission();
                return;
            }
            SettingsPreferenceHelper.setRescueAlertEnabled(requireContext(), isChecked);
            showSavedSnackbar();
        });

        binding.switchLostAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressSwitchCallbacks) {
                return;
            }
            if (isChecked && !PermissionHelper.hasNotificationPermission(requireContext())) {
                pendingNotificationSwitch = PendingNotificationSwitch.LOST;
                suppressSwitchCallbacks = true;
                binding.switchLostAlert.setChecked(false);
                suppressSwitchCallbacks = false;
                requestNotificationPermission();
                return;
            }
            SettingsPreferenceHelper.setLostAlertEnabled(requireContext(), isChecked);
            showSavedSnackbar();
        });

        binding.switchLocationAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressSwitchCallbacks) {
                return;
            }
            SettingsPreferenceHelper.setLocationAlertEnabled(requireContext(), isChecked);
            showSavedSnackbar();
        });
    }

    private void applyPendingNotificationSwitch() {
        if (pendingNotificationSwitch == PendingNotificationSwitch.RESCUE) {
            suppressSwitchCallbacks = true;
            binding.switchRescueAlert.setChecked(true);
            suppressSwitchCallbacks = false;
            SettingsPreferenceHelper.setRescueAlertEnabled(requireContext(), true);
            showSavedSnackbar();
        } else if (pendingNotificationSwitch == PendingNotificationSwitch.LOST) {
            suppressSwitchCallbacks = true;
            binding.switchLostAlert.setChecked(true);
            suppressSwitchCallbacks = false;
            SettingsPreferenceHelper.setLostAlertEnabled(requireContext(), true);
            showSavedSnackbar();
        }
        pendingNotificationSwitch = PendingNotificationSwitch.NONE;
    }

    private void setupUtilityActions() {
        binding.btnOpenNotificationSettings.setOnClickListener(v ->
                PermissionHelper.openAppNotificationSettings(requireContext()));
        binding.btnTestRescueNotification.setOnClickListener(v -> sendTestRescueNotification());
        binding.btnTestLostNotification.setOnClickListener(v -> sendTestLostNotification());
        binding.btnTestLocationNotification.setOnClickListener(v -> sendTestLocationNotification());
        binding.btnClearPhotoCache.setOnClickListener(v -> clearPhotoCache());
    }

    private void sendTestRescueNotification() {
        if (!canSendTestNotification()) {
            return;
        }
        boolean posted = NotificationHelper.showRescueNotification(
                requireContext(),
                getString(R.string.settings_test_rescue_title),
                getString(R.string.settings_test_rescue_body));
        handleTestNotificationResult(posted);
    }

    private void sendTestLostNotification() {
        if (!canSendTestNotification()) {
            return;
        }
        boolean posted = NotificationHelper.showLostNotification(
                requireContext(),
                getString(R.string.settings_test_lost_title),
                getString(R.string.settings_test_lost_body));
        handleTestNotificationResult(posted);
    }

    private void sendTestLocationNotification() {
        if (!canSendTestNotification()) {
            return;
        }
        boolean posted = NotificationHelper.showGeneralNotification(
                requireContext(),
                getString(R.string.settings_test_location_title),
                getString(R.string.settings_test_location_body));
        handleTestNotificationResult(posted);
    }

    private boolean canSendTestNotification() {
        if (!ensureNotificationPermissionForTest()) {
            return false;
        }
        if (!NotificationHelper.canPostNotifications(requireContext())) {
            Snackbar.make(binding.getRoot(), R.string.settings_notification_blocked, Snackbar.LENGTH_LONG)
                    .setAction(R.string.settings_open_notification_settings, v ->
                            PermissionHelper.openAppNotificationSettings(requireContext()))
                    .show();
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - lastTestNotificationAt < TEST_NOTIFICATION_COOLDOWN_MS) {
            Snackbar.make(binding.getRoot(), R.string.settings_test_notification_cooldown, Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }
        lastTestNotificationAt = now;
        return true;
    }

    private void handleTestNotificationResult(boolean posted) {
        if (posted) {
            Snackbar.make(binding.getRoot(), R.string.settings_test_notification_sent, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Snackbar.make(binding.getRoot(), R.string.settings_test_notification_failed, Snackbar.LENGTH_LONG).show();
    }

    private boolean ensureNotificationPermissionForTest() {
        if (PermissionHelper.hasNotificationPermission(requireContext())) {
            return true;
        }
        pendingNotificationSwitch = PendingNotificationSwitch.NONE;
        requestNotificationPermission();
        Snackbar.make(binding.getRoot(), R.string.permission_notification_rationale, Snackbar.LENGTH_LONG).show();
        return false;
    }

    private void clearPhotoCache() {
        int deletedCount = ImageFileHelper.clearLostReportCache(requireContext());
        updateCacheInfo();
        Snackbar.make(binding.getRoot(),
                getString(R.string.settings_cache_cleared_format, deletedCount),
                Snackbar.LENGTH_LONG).show();
    }

    private void updateAppInfo() {
        String versionName = "1.0.0";
        try {
            versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException exception) {
            // fallback to default
        }
        binding.tvAppVersion.setText(getString(R.string.settings_app_version_format, versionName));
    }

    private void updateFcmStatus() {
        if (!FirebaseAvailabilityChecker.isFirebaseAvailable(requireContext())) {
            binding.tvFcmStatus.setText(R.string.settings_fcm_not_configured);
            return;
        }

        String token = FcmTokenStore.getToken(requireContext());
        if (token == null || token.trim().isEmpty()) {
            binding.tvFcmStatus.setText(R.string.settings_fcm_waiting_token);
            return;
        }

        binding.tvFcmStatus.setText(getString(
                R.string.settings_fcm_token_format, maskToken(token)));
    }

    private void updateNotificationPermissionStatus() {
        if (PermissionHelper.hasNotificationPermission(requireContext())
                && NotificationHelper.canPostNotifications(requireContext())) {
            binding.tvNotificationPermissionStatus.setText(R.string.settings_notification_permission_granted);
            return;
        }
        if (PermissionHelper.hasNotificationPermission(requireContext())) {
            binding.tvNotificationPermissionStatus.setText(R.string.settings_notification_permission_blocked);
            return;
        }
        binding.tvNotificationPermissionStatus.setText(R.string.settings_notification_permission_denied);
    }

    private void updateCacheInfo() {
        long cacheSize = ImageFileHelper.getLostReportCacheSize(requireContext());
        binding.tvCacheInfo.setText(getString(R.string.settings_cache_size_format, formatFileSize(cacheSize)));
    }

    private String maskToken(String token) {
        if (token.length() <= 12) {
            return token;
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        return String.format(java.util.Locale.KOREA, "%.1f KB", bytes / 1024.0);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void showPermissionDeniedSnackbar() {
        Snackbar.make(binding.getRoot(), R.string.permission_denied, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings_open_notification_settings, v ->
                        PermissionHelper.openAppNotificationSettings(requireContext()))
                .show();
    }

    private void showSavedSnackbar() {
        Snackbar.make(binding.getRoot(), R.string.settings_saved, Snackbar.LENGTH_SHORT).show();
    }

    private boolean isBindingAvailable() {
        return binding != null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PENDING_SWITCH, pendingNotificationSwitch.name());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
