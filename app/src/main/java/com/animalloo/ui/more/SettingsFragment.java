package com.animalloo.ui.more;

import android.Manifest;
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

import com.animalloo.R;
import com.animalloo.databinding.FragmentSettingsBinding;
import com.animalloo.notification.NotificationHelper;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.util.FirebaseAvailabilityChecker;
import com.animalloo.util.ImageFileHelper;
import com.animalloo.util.PermissionHelper;
import com.animalloo.util.SettingsPreferenceHelper;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends BaseFragment {

    private FragmentSettingsBinding binding;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {
                        binding.switchRescueAlert.setChecked(false);
                        binding.switchLostAlert.setChecked(false);
                        Snackbar.make(binding.getRoot(), R.string.permission_denied, Snackbar.LENGTH_LONG).show();
                    }
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

        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragment() instanceof MoreHost) {
                ((MoreHost) getParentFragment()).onMoreBackPressed();
            }
        });

        loadSettings();
        setupSwitchListeners();
        setupUtilityActions();
        updateAppInfo();
        updateFcmStatus();
        updateCacheInfo();
    }

    private void loadSettings() {
        binding.switchRescueAlert.setChecked(
                SettingsPreferenceHelper.isRescueAlertEnabled(requireContext()));
        binding.switchLostAlert.setChecked(
                SettingsPreferenceHelper.isLostAlertEnabled(requireContext()));
        binding.switchLocationAlert.setChecked(
                SettingsPreferenceHelper.isLocationAlertEnabled(requireContext()));
    }

    private void setupSwitchListeners() {
        binding.switchRescueAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasNotificationPermission(requireContext())) {
                binding.switchRescueAlert.setChecked(false);
                requestNotificationPermission();
                return;
            }
            SettingsPreferenceHelper.setRescueAlertEnabled(requireContext(), isChecked);
            showSavedSnackbar();
        });

        binding.switchLostAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasNotificationPermission(requireContext())) {
                binding.switchLostAlert.setChecked(false);
                requestNotificationPermission();
                return;
            }
            SettingsPreferenceHelper.setLostAlertEnabled(requireContext(), isChecked);
            showSavedSnackbar();
        });

        binding.switchLocationAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsPreferenceHelper.setLocationAlertEnabled(requireContext(), isChecked);
            showSavedSnackbar();
        });
    }

    private void setupUtilityActions() {
        binding.btnTestRescueNotification.setOnClickListener(v -> sendTestRescueNotification());
        binding.btnTestLostNotification.setOnClickListener(v -> sendTestLostNotification());
        binding.btnClearPhotoCache.setOnClickListener(v -> clearPhotoCache());
    }

    private void sendTestRescueNotification() {
        if (!ensureNotificationPermissionForTest()) {
            return;
        }
        NotificationHelper.showRescueNotification(
                requireContext(),
                getString(R.string.settings_test_rescue_title),
                getString(R.string.settings_test_rescue_body));
        Snackbar.make(binding.getRoot(), R.string.settings_test_notification_sent, Snackbar.LENGTH_SHORT).show();
    }

    private void sendTestLostNotification() {
        if (!ensureNotificationPermissionForTest()) {
            return;
        }
        NotificationHelper.showLostNotification(
                requireContext(),
                getString(R.string.settings_test_lost_title),
                getString(R.string.settings_test_lost_body));
        Snackbar.make(binding.getRoot(), R.string.settings_test_notification_sent, Snackbar.LENGTH_SHORT).show();
    }

    private boolean ensureNotificationPermissionForTest() {
        if (PermissionHelper.hasNotificationPermission(requireContext())) {
            return true;
        }
        requestNotificationPermission();
        Snackbar.make(binding.getRoot(), R.string.permission_notification_rationale, Snackbar.LENGTH_LONG).show();
        return false;
    }

    private void clearPhotoCache() {
        int deletedCount = ImageFileHelper.clearLostReportCache(requireContext());
        updateCacheInfo();
        Snackbar.make(binding.getRoot(),
                getString(R.string.settings_cache_cleared_format, deletedCount),
                Snackbar.LENGTH_SHORT).show();
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
        if (FirebaseAvailabilityChecker.isFirebaseAvailable(requireContext())) {
            binding.tvFcmStatus.setText(R.string.settings_fcm_configured);
        } else {
            binding.tvFcmStatus.setText(R.string.settings_fcm_not_configured);
        }
    }

    private void updateCacheInfo() {
        long cacheSize = ImageFileHelper.getLostReportCacheSize(requireContext());
        binding.tvCacheInfo.setText(getString(R.string.settings_cache_size_format, formatFileSize(cacheSize)));
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

    private void showSavedSnackbar() {
        Snackbar.make(binding.getRoot(), R.string.settings_saved, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
