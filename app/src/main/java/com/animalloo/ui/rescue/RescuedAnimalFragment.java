package com.animalloo.ui.rescue;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.RescuedAnimalAdapter;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentRescuedAnimalBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Set;

public class RescuedAnimalFragment extends BaseFragment {

    private FragmentRescuedAnimalBinding binding;
    private RescuedAnimalViewModel viewModel;
    private RescuedAnimalAdapter rescuedAnimalAdapter;
    private RescuedAnimal contextMenuAnimal;
    private DetailNavigator detailNavigator;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof DetailNavigator) {
            detailNavigator = (DetailNavigator) context;
        } else {
            throw new IllegalStateException("Host Activity must implement DetailNavigator");
        }
    }

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

        UiState<List<RescuedAnimal>> currentState = viewModel.getAnimalsState().getValue();
        if (currentState == null) {
            viewModel.loadAnimals();
        }
    }

    private void setupFilters() {
        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.rescued_animal_breeds,
                android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterBreed.setAdapter(breedAdapter);
        binding.spinnerFilterBreed.setSelection(
                Math.max(0, breedAdapter.getPosition(viewModel.getSelectedBreed().getValue())));
        binding.spinnerFilterBreed.setOnItemSelectedListener(new SimpleItemSelectedListener(value ->
                viewModel.setBreedFilter(value)));

        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.regions, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterRegion.setAdapter(regionAdapter);
        binding.spinnerFilterRegion.setSelection(
                Math.max(0, regionAdapter.getPosition(viewModel.getSelectedRegion().getValue())));
        binding.spinnerFilterRegion.setOnItemSelectedListener(new SimpleItemSelectedListener(value ->
                viewModel.setRegionFilter(value)));

        String gender = viewModel.getSelectedGender().getValue();
        if (getString(R.string.gender_male).equals(gender)) {
            binding.chipGroupGender.check(R.id.chip_gender_male);
        } else if (getString(R.string.gender_female).equals(gender)) {
            binding.chipGroupGender.check(R.id.chip_gender_female);
        } else {
            binding.chipGroupGender.check(R.id.chip_gender_all);
        }

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
                detailNavigator.navigateToDetail(DetailType.RESCUED_ANIMAL, animal.getId());
            }

            @Override
            public void onAnimalLongClick(RescuedAnimal animal, View anchorView) {
                showAnimalContextMenu(animal, anchorView);
            }
        });
        rescuedAnimalAdapter.setOnAnimalActionListener(new RescuedAnimalAdapter.OnAnimalActionListener() {
            @Override
            public void onFavoriteClick(RescuedAnimal animal) {
                boolean wasFavorite = viewModel.isAnimalFavorite(animal.getId());
                viewModel.toggleAnimalFavorite(animal.getId());
                Snackbar.make(binding.getRoot(),
                        wasFavorite ? R.string.rescued_unfavorited : R.string.rescued_favorited,
                        Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onShareClick(RescuedAnimal animal) {
                shareAnimal(animal);
            }
        });
    }

    private void showAnimalContextMenu(RescuedAnimal animal, View anchorView) {
        contextMenuAnimal = animal;
        anchorView.setOnCreateContextMenuListener((menu, view, menuInfo) -> {
            requireActivity().getMenuInflater().inflate(R.menu.context_menu_rescued_animal, menu);
            MenuItem favoriteItem = menu.findItem(R.id.context_favorite);
            if (favoriteItem != null) {
                boolean isFavorite = viewModel.isAnimalFavorite(animal.getId());
                favoriteItem.setTitle(isFavorite
                        ? R.string.context_unfavorite
                        : R.string.context_favorite);
            }
        });
        anchorView.showContextMenu();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (contextMenuAnimal == null) {
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == R.id.context_share) {
            shareAnimal(contextMenuAnimal);
            return true;
        } else if (itemId == R.id.context_favorite) {
            boolean wasFavorite = viewModel.isAnimalFavorite(contextMenuAnimal.getId());
            viewModel.toggleAnimalFavorite(contextMenuAnimal.getId());
            Snackbar.make(binding.getRoot(),
                    wasFavorite ? R.string.rescued_unfavorited : R.string.rescued_favorited,
                    Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void shareAnimal(RescuedAnimal animal) {
        String shareText = getString(
                R.string.rescued_share_format,
                animal.getName(),
                animal.getSpecies(),
                animal.getBreed(),
                animal.getRegion(),
                animal.getRescuedDate(),
                animal.getProtectionStatus(),
                animal.getFeatures());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.rescued_share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.context_share)));
    }

    private void observeViewModel() {
        viewModel.getAnimalsState().observe(getViewLifecycleOwner(), this::renderAnimalsState);
        viewModel.getFavoriteAnimalIds().observe(getViewLifecycleOwner(),
                rescuedAnimalAdapter::setFavoriteAnimalIds);
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
            showStateView(R.layout.layout_rescued_empty, stateView ->
                    stateView.findViewById(R.id.btn_reset_filters).setOnClickListener(v -> {
                        viewModel.resetFilters();
                        resetFilterUi();
                    }));
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvRescuedAnimals.setVisibility(View.VISIBLE);
            rescuedAnimalAdapter.setItems(state.getData());
        }
    }

    private void resetFilterUi() {
        binding.spinnerFilterBreed.setSelection(0);
        binding.spinnerFilterRegion.setSelection(0);
        binding.chipGroupGender.check(R.id.chip_gender_all);
        scrollToTop();
    }

    private void scrollToTop() {
        binding.getRoot().scrollTo(0, 0);
        ViewParent parent = binding.getRoot().getParent();
        while (parent instanceof View) {
            if (parent instanceof NestedScrollView) {
                ((NestedScrollView) parent).scrollTo(0, 0);
                break;
            }
            parent = parent.getParent();
        }
        if (binding.rvRescuedAnimals.getVisibility() == View.VISIBLE) {
            binding.rvRescuedAnimals.scrollToPosition(0);
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

        SimpleItemSelectedListener(SelectionCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            Object item = parent.getItemAtPosition(position);
            if (item != null) {
                callback.onSelected(item.toString());
            }
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
