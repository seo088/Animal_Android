package com.animalloo.ui.more;

import android.Manifest;
import android.os.Build;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.animalloo.R;
import com.animalloo.util.PermissionHelper;
import com.animalloo.util.SettingsPreferenceHelper;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

public class MoreNotificationSwitchController {

    public enum PendingSwitch {
        NONE,
        RESCUE,
        LOST
    }

    private final Fragment fragment;
    private final View snackbarAnchor;
    private final MaterialSwitch switchRescue;
    private final MaterialSwitch switchLost;
    private final MaterialSwitch switchLocation;
    private final ActivityResultLauncher<String> permissionLauncher;

    private boolean suppressCallbacks;
    private PendingSwitch pendingSwitch = PendingSwitch.NONE;

    public MoreNotificationSwitchController(
            @NonNull Fragment fragment,
            @NonNull View snackbarAnchor,
            @NonNull MaterialSwitch switchRescue,
            @NonNull MaterialSwitch switchLost,
            @NonNull MaterialSwitch switchLocation,
            @NonNull ActivityResultLauncher<String> permissionLauncher) {
        this.fragment = fragment;
        this.snackbarAnchor = snackbarAnchor;
        this.switchRescue = switchRescue;
        this.switchLost = switchLost;
        this.switchLocation = switchLocation;
        this.permissionLauncher = permissionLauncher;
    }

    public void bind() {
        switchRescue.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleSwitchChange(PendingSwitch.RESCUE, switchRescue, isChecked,
                        SettingsPreferenceHelper::isRescueAlertEnabled,
                        SettingsPreferenceHelper::setRescueAlertEnabled));
        switchLost.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleSwitchChange(PendingSwitch.LOST, switchLost, isChecked,
                        SettingsPreferenceHelper::isLostAlertEnabled,
                        SettingsPreferenceHelper::setLostAlertEnabled));
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressCallbacks) {
                return;
            }
            SettingsPreferenceHelper.setLocationAlertEnabled(fragment.requireContext(), isChecked);
            showSavedSnackbar();
        });

        refresh();
    }

    public void refresh() {
        suppressCallbacks = true;
        switchRescue.setChecked(SettingsPreferenceHelper.isRescueAlertEnabled(fragment.requireContext()));
        switchLost.setChecked(SettingsPreferenceHelper.isLostAlertEnabled(fragment.requireContext()));
        switchLocation.setChecked(SettingsPreferenceHelper.isLocationAlertEnabled(fragment.requireContext()));
        suppressCallbacks = false;
    }

    public void onPermissionResult(boolean granted) {
        if (!granted) {
            pendingSwitch = PendingSwitch.NONE;
            refresh();
            Snackbar.make(snackbarAnchor, R.string.permission_notification_rationale, Snackbar.LENGTH_LONG).show();
            return;
        }
        applyPendingSwitch();
    }

    private void handleSwitchChange(
            PendingSwitch type,
            MaterialSwitch switchView,
            boolean isChecked,
            PreferenceReader reader,
            PreferenceWriter writer) {
        if (suppressCallbacks) {
            return;
        }
        if (isChecked && !PermissionHelper.hasNotificationPermission(fragment.requireContext())) {
            pendingSwitch = type;
            suppressCallbacks = true;
            switchView.setChecked(false);
            suppressCallbacks = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            return;
        }
        writer.write(fragment.requireContext(), isChecked);
        showSavedSnackbar();
    }

    private void applyPendingSwitch() {
        if (pendingSwitch == PendingSwitch.RESCUE) {
            suppressCallbacks = true;
            switchRescue.setChecked(true);
            suppressCallbacks = false;
            SettingsPreferenceHelper.setRescueAlertEnabled(fragment.requireContext(), true);
            showSavedSnackbar();
        } else if (pendingSwitch == PendingSwitch.LOST) {
            suppressCallbacks = true;
            switchLost.setChecked(true);
            suppressCallbacks = false;
            SettingsPreferenceHelper.setLostAlertEnabled(fragment.requireContext(), true);
            showSavedSnackbar();
        }
        pendingSwitch = PendingSwitch.NONE;
    }

    private void showSavedSnackbar() {
        Snackbar.make(snackbarAnchor, R.string.settings_saved, Snackbar.LENGTH_SHORT).show();
    }

    private interface PreferenceReader {
        boolean read(android.content.Context context);
    }

    private interface PreferenceWriter {
        void write(android.content.Context context, boolean enabled);
    }
}
