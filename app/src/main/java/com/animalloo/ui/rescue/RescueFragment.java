package com.animalloo.ui.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.animalloo.databinding.FragmentRescueBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.home.HomeFragment;

public class RescueFragment extends BaseFragment {

    private FragmentRescueBinding binding;
    private int selectedTabIndex = HomeFragment.RESCUE_TAB_LOST;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        binding = FragmentRescueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Phase 8에서 TabLayout과 연동됩니다.
     */
    public void selectTab(int tabIndex) {
        selectedTabIndex = tabIndex;
        if (binding != null) {
            updateTabPlaceholder();
        }
    }

    private void updateTabPlaceholder() {
        if (selectedTabIndex == HomeFragment.RESCUE_TAB_RESCUED) {
            binding.tvRescuePlaceholder.setText(com.animalloo.R.string.rescue_tab_rescued);
        } else {
            binding.tvRescuePlaceholder.setText(com.animalloo.R.string.rescue_tab_lost);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            selectedTabIndex = savedInstanceState.getInt("selected_tab", HomeFragment.RESCUE_TAB_LOST);
        }
        updateTabPlaceholder();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_tab", selectedTabIndex);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
