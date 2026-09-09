package com.animalloo.ui.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.animalloo.R;
import com.animalloo.databinding.FragmentRescueBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.home.HomeFragment;
import com.google.android.material.tabs.TabLayout;

public class RescueFragment extends BaseFragment implements RescueHost {

    private static final String TAG_LOST = "tag_lost_report";
    private static final String TAG_RESCUED = "tag_rescued_animal";
    private static final String TAG_MATCHING = "tag_matching_result";

    private FragmentRescueBinding binding;
    private LostReportFragment lostReportFragment;
    private RescuedAnimalFragment rescuedAnimalFragment;
    private int selectedTabIndex = HomeFragment.RESCUE_TAB_LOST;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRescueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            selectedTabIndex = savedInstanceState.getInt("selected_tab", HomeFragment.RESCUE_TAB_LOST);
        }

        initChildFragments(savedInstanceState);
        setupTabLayout();
        selectTab(selectedTabIndex);
    }

    private void initChildFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            lostReportFragment = new LostReportFragment();
            rescuedAnimalFragment = new RescuedAnimalFragment();

            getChildFragmentManager().beginTransaction()
                    .add(R.id.rescue_child_container, lostReportFragment, TAG_LOST)
                    .add(R.id.rescue_child_container, rescuedAnimalFragment, TAG_RESCUED)
                    .hide(rescuedAnimalFragment)
                    .commit();
        } else {
            lostReportFragment = (LostReportFragment) getChildFragmentManager().findFragmentByTag(TAG_LOST);
            rescuedAnimalFragment = (RescuedAnimalFragment) getChildFragmentManager().findFragmentByTag(TAG_RESCUED);
        }
    }

    private void setupTabLayout() {
        if (binding.tabLayoutRescue.getTabCount() == 0) {
            binding.tabLayoutRescue.addTab(
                    binding.tabLayoutRescue.newTab().setText(R.string.rescue_tab_lost));
            binding.tabLayoutRescue.addTab(
                    binding.tabLayoutRescue.newTab().setText(R.string.rescue_tab_rescued));
        }

        binding.tabLayoutRescue.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabIndex = tab.getPosition();
                switchChildTab(selectedTabIndex);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // no-op
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // no-op
            }
        });
    }

    public void selectTab(int tabIndex) {
        selectedTabIndex = tabIndex;
        if (binding != null && binding.tabLayoutRescue.getTabCount() > tabIndex) {
            TabLayout.Tab tab = binding.tabLayoutRescue.getTabAt(tabIndex);
            if (tab != null && !tab.isSelected()) {
                tab.select();
            } else {
                switchChildTab(tabIndex);
            }
        }
    }

    private void switchChildTab(int tabIndex) {
        if (lostReportFragment == null || rescuedAnimalFragment == null) {
            return;
        }

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment matchingFragment = getChildFragmentManager().findFragmentByTag(TAG_MATCHING);
        if (matchingFragment != null) {
            transaction.remove(matchingFragment);
        }

        if (tabIndex == HomeFragment.RESCUE_TAB_RESCUED) {
            transaction.hide(lostReportFragment).show(rescuedAnimalFragment);
        } else {
            transaction.hide(rescuedAnimalFragment).show(lostReportFragment);
        }
        transaction.commit();
    }

    @Override
    public void showMatchingResults() {
        if (lostReportFragment == null) {
            return;
        }

        MatchingResultFragment matchingResultFragment = new MatchingResultFragment();
        getChildFragmentManager().beginTransaction()
                .hide(lostReportFragment)
                .add(R.id.rescue_child_container, matchingResultFragment, TAG_MATCHING)
                .addToBackStack(TAG_MATCHING)
                .commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_tab", selectedTabIndex);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
