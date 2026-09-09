package com.animalloo.ui.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.RescuedAnimalAdapter;
import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentRescuedAnimalBinding;
import com.animalloo.ui.common.BaseFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class RescuedAnimalFragment extends BaseFragment {

    private FragmentRescuedAnimalBinding binding;
    private RescuedAnimalViewModel viewModel;
    private RescuedAnimalAdapter rescuedAnimalAdapter;
    private RescuedAnimal contextMenuAnimal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRescuedAnimalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RescuedAnimalViewModel.class);
        setupFilters();
        setupRecyclerView();
        observeViewModel();
        viewModel.loadAnimals();
    }

    private void setupFilters() {
        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.dog_breeds, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterBreed.setAdapter(breedAdapter);
        binding.spinnerFilterBreed.setOnItemSelectedListener(new SimpleItemSelectedListener(value ->
                viewModel.setBreedFilter(value)));

        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.regions, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterRegion.setAdapter(regionAdapter);
        binding.spinnerFilterRegion.setOnItemSelectedListener(new SimpleItemSelectedListener(value ->
                viewModel.setRegionFilter(value)));

        binding.chipGroupGender.setOnCheckedStateChangeListener(
                (ChipGroup group, List<Integer> checkedIds) -> {
                    if (checkedIds.isEmpty()) {
                        return;
                    }
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chip_gender_male) {
                        viewModel.setGenderFilter(getString(R.string.gender_male));
                    } else if (checkedId == R.id.chip_gender_female) {
                        viewModel.setGenderFilter(getString(R.string.gender_female));
                    } else {
                        viewModel.setGenderFilter("전체");
                    }
                });
    }

    private void setupRecyclerView() {
        rescuedAnimalAdapter = new RescuedAnimalAdapter();
        binding.rvRescuedAnimals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRescuedAnimals.setAdapter(rescuedAnimalAdapter);
        rescuedAnimalAdapter.setOnAnimalClickListener(new RescuedAnimalAdapter.OnAnimalClickListener() {
            @Override
            public void onAnimalClick(RescuedAnimal animal) {
                Snackbar.make(binding.getRoot(), R.string.rescued_detail_prepare, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimalLongClick(RescuedAnimal animal, View anchorView) {
                showAnimalContextMenu(animal, anchorView);
            }
        });
    }

    private void showAnimalContextMenu(RescuedAnimal animal, View anchorView) {
        contextMenuAnimal = animal;
        anchorView.setOnCreateContextMenuListener((menu, view, menuInfo) ->
                requireActivity().getMenuInflater().inflate(R.menu.context_menu_rescued_animal, menu));
        anchorView.showContextMenu();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (contextMenuAnimal == null) {
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == R.id.context_share) {
            Snackbar.make(binding.getRoot(),
                    getString(R.string.rescued_shared) + " (" + contextMenuAnimal.getBreed() + ")",
                    Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.context_favorite) {
            Snackbar.make(binding.getRoot(), R.string.home_alert_favorited, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void observeViewModel() {
        viewModel.getAnimalsState().observe(getViewLifecycleOwner(), this::renderAnimalsState);
    }

    private void renderAnimalsState(UiState<List<RescuedAnimal>> state) {
        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            binding.rvRescuedAnimals.setVisibility(View.GONE);
            showStateView(R.layout.layout_loading, null);
            return;
        }

        hideStateView();

        if (state.isError()) {
            binding.rvRescuedAnimals.setVisibility(View.GONE);
            showStateView(R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.loadAnimals());
            });
            return;
        }

        if (state.isEmpty()) {
            binding.rvRescuedAnimals.setVisibility(View.GONE);
            showStateView(R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvRescuedAnimals.setVisibility(View.VISIBLE);
            rescuedAnimalAdapter.setItems(state.getData());
        }
    }

    private void showStateView(int layoutRes, StateViewSetup setup) {
        binding.rescuedStateContainer.removeAllViews();
        binding.rescuedStateContainer.setVisibility(View.VISIBLE);
        View stateView = getLayoutInflater().inflate(layoutRes, binding.rescuedStateContainer, false);
        binding.rescuedStateContainer.addView(stateView);
        if (setup != null) {
            setup.setup(stateView);
        }
    }

    private void hideStateView() {
        binding.rescuedStateContainer.removeAllViews();
        binding.rescuedStateContainer.setVisibility(View.GONE);
    }

    private interface StateViewSetup {
        void setup(View stateView);
    }

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {

        interface SelectionCallback {
            void onSelected(String value);
        }

        private final SelectionCallback callback;
        private boolean firstSelection = true;

        SimpleItemSelectedListener(SelectionCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            if (firstSelection) {
                firstSelection = false;
                return;
            }
            callback.onSelected(parent.getItemAtPosition(position).toString());
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
            // no-op
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
