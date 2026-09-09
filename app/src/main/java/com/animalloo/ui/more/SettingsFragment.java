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
import androidx.core.content.ContextCompat;

import com.animalloo.R;
import com.animalloo.databinding.FragmentSettingsBinding;
import com.animalloo.ui.common.BaseFragment;
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
        updateAppInfo();
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
            if (isChecked && !hasNotificationPermission()) {
                binding.switchRescueAlert.setChecked(false);
                requestNotificationPermission();
                return;
            }
            SettingsPreferenceHelper.setRescueAlertEnabled(requireContext(), isChecked);
            showSavedSnackbar();
        });

        binding.switchLostAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !hasNotificationPermission()) {
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

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
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
