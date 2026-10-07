package com.animalloo.ui.more;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.animalloo.R;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentMoreBinding;
import com.animalloo.databinding.ItemMoreListRowBinding;
import com.animalloo.ui.auth.AuthActivity;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.home.HomeFragment;
import com.animalloo.ui.main.MainNavigator;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MoreFragment extends BaseFragment implements MoreHost {

    public static final int SECTION_HUB = 0;
    public static final int SECTION_PET_FRIENDLY = 1;
    public static final int SECTION_SETTINGS = 2;
    public static final int SECTION_DATA_SOURCE = 3;

    private static final String TAG_PET_FRIENDLY = "tag_pet_friendly";
    private static final String TAG_SETTINGS = "tag_settings";
    private static final String TAG_DATA_SOURCE = "tag_data_source";

    private FragmentMoreBinding binding;
    private MoreViewModel viewModel;
    private MainNavigator mainNavigator;
    private OnBackPressedCallback backPressedCallback;
    private MoreNotificationSwitchController notificationSwitchController;
    private int currentSection = SECTION_HUB;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (notificationSwitchController != null) {
                    notificationSwitchController.onPermissionResult(granted);
                }
            });

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof MainNavigator) {
            mainNavigator = (MainNavigator) context;
        } else {
            throw new IllegalStateException("Host Activity must implement MainNavigator");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        setupProfileHeader();
        setupListRows();
        setupNotificationSwitches();
        setupLogout();
        setupBackHandler();
        observeViewModel();

        if (savedInstanceState != null) {
            currentSection = savedInstanceState.getInt("more_section", SECTION_HUB);
        }
        updateSectionVisibility(currentSection);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadHubData();
        if (notificationSwitchController != null) {
            notificationSwitchController.refresh();
        }
    }

    public int getCurrentSection() {
        return currentSection;
    }

    public void openSection(int section) {
        if (section == SECTION_HUB) {
            showHub();
            return;
        }

        currentSection = section;
        Fragment targetFragment = getOrCreateSectionFragment(section);
        if (targetFragment == null) {
            return;
        }

        binding.layoutMoreHub.setVisibility(View.GONE);
        binding.moreSubContainer.setVisibility(View.VISIBLE);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        hideAllSubFragments(transaction);
        transaction.show(targetFragment);
        transaction.commit();

        updateBackHandlerEnabled(true);
        notifySectionChanged(section);
    }

    @Override
    public void onMoreBackPressed() {
        showHub();
    }

    private void showHub() {
        currentSection = SECTION_HUB;
        binding.layoutMoreHub.setVisibility(View.VISIBLE);
        binding.moreSubContainer.setVisibility(View.GONE);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        hideAllSubFragments(transaction);
        transaction.commit();

        updateBackHandlerEnabled(false);
        notifySectionChanged(SECTION_HUB);
        viewModel.refreshFavoriteCount();
    }

    private void setupProfileHeader() {
        binding.layoutProfileHeader.getRoot().setOnClickListener(v -> openSection(SECTION_SETTINGS));
    }

    private void setupListRows() {
        setupNavigationRow(
                binding.rowPetProfile,
                getString(R.string.more_pet_profile),
                null,
                () -> openSection(SECTION_SETTINGS));

        setupNavigationRow(
                binding.rowMyLostReport,
                getString(R.string.more_my_lost_report),
                getString(R.string.more_my_lost_report_desc),
                () -> mainNavigator.navigateToRescueWithTab(HomeFragment.RESCUE_TAB_LOST));

        setupNavigationRow(
                binding.rowFavoriteRescued,
                getString(R.string.more_favorite_rescued),
                null,
                () -> mainNavigator.navigateToRescueWithTab(HomeFragment.RESCUE_TAB_RESCUED));

        setupNavigationRow(
                binding.rowPetFriendly,
                getString(R.string.more_pet_friendly),
                null,
                () -> openSection(SECTION_PET_FRIENDLY));

        setupNavigationRow(
                binding.rowDataSource,
                getString(R.string.more_data_source),
                null,
                () -> openSection(SECTION_DATA_SOURCE));

        setupNavigationRow(
                binding.rowAccountSettings,
                getString(R.string.more_account_settings),
                getString(R.string.more_account_settings_desc),
                () -> openSection(SECTION_SETTINGS));

        setupInfoRow(binding.rowAppInfo, getString(R.string.more_app_info), resolveAppVersion());

        setupLogoutRow();
    }

    private void setupNavigationRow(ItemMoreListRowBinding rowBinding, String title,
                                    @Nullable String subtitle, Runnable action) {
        rowBinding.tvRowTitle.setText(title);
        if (subtitle != null && !subtitle.isEmpty()) {
            rowBinding.tvRowSubtitle.setText(subtitle);
            rowBinding.tvRowSubtitle.setVisibility(View.VISIBLE);
        } else {
            rowBinding.tvRowSubtitle.setVisibility(View.GONE);
        }
        rowBinding.tvRowValue.setVisibility(View.GONE);
        rowBinding.ivRowChevron.setVisibility(View.VISIBLE);
        rowBinding.getRoot().setOnClickListener(v -> action.run());
    }

    private void setupInfoRow(ItemMoreListRowBinding rowBinding, String title, String value) {
        rowBinding.tvRowTitle.setText(title);
        rowBinding.tvRowSubtitle.setVisibility(View.GONE);
        rowBinding.tvRowValue.setText(value);
        rowBinding.tvRowValue.setVisibility(View.VISIBLE);
        rowBinding.ivRowChevron.setVisibility(View.GONE);
        rowBinding.getRoot().setClickable(false);
        rowBinding.getRoot().setFocusable(false);
        rowBinding.getRoot().setBackground(null);
    }

    private void setupLogoutRow() {
        ItemMoreListRowBinding rowBinding = binding.rowLogout;
        rowBinding.tvRowTitle.setText(R.string.settings_logout);
        rowBinding.tvRowTitle.setTextColor(requireContext().getColor(R.color.color_badge_error));
        rowBinding.tvRowSubtitle.setVisibility(View.GONE);
        rowBinding.tvRowValue.setVisibility(View.GONE);
        rowBinding.ivRowChevron.setVisibility(View.VISIBLE);
        rowBinding.getRoot().setOnClickListener(v -> showLogoutConfirmation());
    }

    private void setupNotificationSwitches() {
        binding.rowSwitchRescue.tvSwitchTitle.setText(R.string.settings_rescue_alert);
        binding.rowSwitchLost.tvSwitchTitle.setText(R.string.settings_lost_alert);
        binding.rowSwitchLocation.tvSwitchTitle.setText(R.string.settings_location_alert);

        notificationSwitchController = new MoreNotificationSwitchController(
                this,
                binding.getRoot(),
                binding.rowSwitchRescue.switchRow,
                binding.rowSwitchLost.switchRow,
                binding.rowSwitchLocation.switchRow,
                notificationPermissionLauncher);
        notificationSwitchController.bind();
    }

    private void setupLogout() {
        viewModel.getLogoutComplete().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                redirectToAuth();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getProfileState().observe(getViewLifecycleOwner(), this::renderProfileState);
        viewModel.getFavoriteRescuedCount().observe(getViewLifecycleOwner(), count -> {
            if (count == null) {
                return;
            }
            if (count > 0) {
                binding.rowFavoriteRescued.tvRowSubtitle.setText(
                        getString(R.string.more_favorite_rescued_count_format, count));
                binding.rowFavoriteRescued.tvRowSubtitle.setVisibility(View.VISIBLE);
            } else {
                binding.rowFavoriteRescued.tvRowSubtitle.setText(R.string.more_favorite_rescued_empty);
                binding.rowFavoriteRescued.tvRowSubtitle.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderProfileState(UiState<HomeProfile> state) {
        if (state == null || !state.isSuccess() || state.getData() == null) {
            return;
        }

        HomeProfile profile = state.getData();
        Glide.with(this)
                .load(profile.getUserPhotoUrl())
                .placeholder(profile.getUserPhotoFallbackResId())
                .error(profile.getUserPhotoFallbackResId())
                .circleCrop()
                .into(binding.layoutProfileHeader.ivProfileAvatar);

        binding.layoutProfileHeader.tvProfileName.setText(profile.getUserDisplayName());
        binding.layoutProfileHeader.tvProfilePetSummary.setText(
                getString(R.string.more_pet_summary_format,
                        profile.getPetName(),
                        profile.getPetBreed(),
                        profile.getPetAge()));

        binding.rowPetProfile.tvRowSubtitle.setText(
                getString(R.string.home_pet_info_format, profile.getPetBreed(), profile.getPetAge()));
        binding.rowPetProfile.tvRowSubtitle.setVisibility(View.VISIBLE);
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

    private String resolveAppVersion() {
        try {
            return getString(
                    R.string.settings_app_version_format,
                    requireContext().getPackageManager()
                            .getPackageInfo(requireContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException exception) {
            return getString(R.string.settings_app_version_format, "1.0.0");
        }
    }

    private Fragment getOrCreateSectionFragment(int section) {
        FragmentManager childFragmentManager = getChildFragmentManager();
        String tag = getTagForSection(section);
        if (tag == null) {
            return null;
        }

        Fragment existing = childFragmentManager.findFragmentByTag(tag);
        if (existing != null) {
            return existing;
        }

        Fragment fragment;
        if (section == SECTION_PET_FRIENDLY) {
            fragment = new PetFriendlyFragment();
        } else if (section == SECTION_SETTINGS) {
            fragment = new SettingsFragment();
        } else if (section == SECTION_DATA_SOURCE) {
            fragment = new DataSourceFragment();
        } else {
            return null;
        }

        FragmentTransaction transaction = childFragmentManager.beginTransaction();
        transaction.add(R.id.more_sub_container, fragment, tag);
        transaction.hide(fragment);
        transaction.commit();
        return fragment;
    }

    private void hideAllSubFragments(FragmentTransaction transaction) {
        Fragment petFriendly = getChildFragmentManager().findFragmentByTag(TAG_PET_FRIENDLY);
        Fragment settings = getChildFragmentManager().findFragmentByTag(TAG_SETTINGS);
        Fragment dataSource = getChildFragmentManager().findFragmentByTag(TAG_DATA_SOURCE);

        if (petFriendly != null) {
            transaction.hide(petFriendly);
        }
        if (settings != null) {
            transaction.hide(settings);
        }
        if (dataSource != null) {
            transaction.hide(dataSource);
        }
    }

    @Nullable
    private String getTagForSection(int section) {
        if (section == SECTION_PET_FRIENDLY) {
            return TAG_PET_FRIENDLY;
        } else if (section == SECTION_SETTINGS) {
            return TAG_SETTINGS;
        } else if (section == SECTION_DATA_SOURCE) {
            return TAG_DATA_SOURCE;
        }
        return null;
    }

    private void updateSectionVisibility(int section) {
        if (section == SECTION_HUB) {
            showHub();
        } else {
            openSection(section);
        }
    }

    private void setupBackHandler() {
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                showHub();
            }
        };
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), backPressedCallback);
    }

    private void updateBackHandlerEnabled(boolean enabled) {
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(enabled);
        }
    }

    private void notifySectionChanged(int section) {
        if (getActivity() instanceof MoreSectionListener) {
            ((MoreSectionListener) getActivity()).onMoreSectionChanged(section);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("more_section", currentSection);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        notificationSwitchController = null;
        super.onDestroyView();
    }
}
