package com.animalloo.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.animalloo.R;
import com.animalloo.databinding.FragmentMoreBinding;
import com.animalloo.databinding.ItemMoreMenuBinding;
import com.animalloo.ui.common.BaseFragment;

public class MoreFragment extends BaseFragment implements MoreHost {

    public static final int SECTION_HUB = 0;
    public static final int SECTION_PET_FRIENDLY = 1;
    public static final int SECTION_SETTINGS = 2;
    public static final int SECTION_DATA_SOURCE = 3;

    private static final String TAG_PET_FRIENDLY = "tag_pet_friendly";
    private static final String TAG_SETTINGS = "tag_settings";
    private static final String TAG_DATA_SOURCE = "tag_data_source";

    private FragmentMoreBinding binding;
    private OnBackPressedCallback backPressedCallback;
    private int currentSection = SECTION_HUB;

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

        setupMenuItems();
        setupBackHandler();

        if (savedInstanceState != null) {
            currentSection = savedInstanceState.getInt("more_section", SECTION_HUB);
        }
        updateSectionVisibility(currentSection);
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
    }

    private void setupMenuItems() {
        setupMenuItem(binding.menuPetFriendly, R.drawable.ic_more, R.string.more_pet_friendly,
                R.string.more_pet_friendly_desc, () -> openSection(SECTION_PET_FRIENDLY));
        setupMenuItem(binding.menuSettings, R.drawable.ic_more, R.string.more_settings,
                R.string.more_settings_desc, () -> openSection(SECTION_SETTINGS));
        setupMenuItem(binding.menuDataSource, R.drawable.ic_more, R.string.more_data_source,
                R.string.more_data_source_desc, () -> openSection(SECTION_DATA_SOURCE));
    }

    private void setupMenuItem(ItemMoreMenuBinding menuBinding, int iconRes, int titleRes,
                               int descRes, Runnable action) {
        menuBinding.ivMenuIcon.setImageResource(iconRes);
        menuBinding.tvMenuTitle.setText(titleRes);
        menuBinding.tvMenuDescription.setText(descRes);
        menuBinding.getRoot().setOnClickListener(v -> action.run());
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("more_section", currentSection);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
