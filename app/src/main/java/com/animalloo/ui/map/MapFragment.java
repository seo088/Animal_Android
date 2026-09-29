package com.animalloo.ui.map;

import android.Manifest;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.FacilityListAdapter;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentMapBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.util.FacilityMapController;
import com.animalloo.util.MapsAvailabilityChecker;
import com.animalloo.util.PermissionHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;

public class MapFragment extends BaseFragment {

    private static final long CATEGORY_ANIMATION_MS = 180L;

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    private FacilityMapController mapController;
    private FacilityListAdapter facilityListAdapter;
    private boolean mapsAvailable;
    private boolean categoryMenuExpanded;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private boolean locationPromptShown;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handleLocationPermissionResult);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        mapsAvailable = MapsAvailabilityChecker.isMapsAvailable(requireContext());

        FacilityCategory category = viewModel.getSelectedCategory().getValue();
        if (savedInstanceState != null) {
            String savedCategory = savedInstanceState.getString("selected_category");
            category = savedCategory == null ? null : FacilityCategory.valueOf(savedCategory);
            categoryMenuExpanded = savedInstanceState.getBoolean("category_menu_expanded", false);
        }

        setupCategoryMenu();
        restoreCategorySelection(category);
        setupFallbackList();
        setupMapContainer();
        observeViewModel();

        if (categoryMenuExpanded) {
            showCategoryMenu(false);
        } else {
            hideCategoryMenu(false);
        }

        viewModel.loadFacilities(category);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapController != null) {
            mapController.resume();
        }
    }

    @Override
    public void onPause() {
        if (mapController != null) {
            mapController.pause();
        }
        super.onPause();
    }

    private void setupMapContainer() {
        if (mapsAvailable) {
            binding.layoutMapFallback.setVisibility(View.GONE);
            binding.mapContainer.setVisibility(View.VISIBLE);
            mapController = new FacilityMapController(binding.mapContainer);
            mapController.setOnFacilityClickListener(this::showFacilityBottomSheet);
            mapController.start();
            promptLocationPermissionIfNeeded();
        } else {
            binding.mapContainer.setVisibility(View.GONE);
            binding.layoutMapFallback.setVisibility(View.VISIBLE);
        }
    }

    private void setupFallbackList() {
        facilityListAdapter = new FacilityListAdapter();
        binding.rvFacilityList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFacilityList.setAdapter(facilityListAdapter);
        facilityListAdapter.setOnFacilityClickListener(
                facility -> showFacilityBottomSheet(facility.getId()));
    }

    private void setupCategoryMenu() {
        binding.btnCategoryToggle.setOnClickListener(v -> toggleCategoryMenu());

        setupCategoryChip(binding.chipAll, null);
        setupCategoryChip(binding.chipHospital, FacilityCategory.HOSPITAL);
        setupCategoryChip(binding.chipPharmacy, FacilityCategory.PHARMACY);
        setupCategoryChip(binding.chipShelter, FacilityCategory.SHELTER);
        setupCategoryChip(binding.chipRestaurant, FacilityCategory.RESTAURANT);
        setupCategoryChip(binding.chipCafe, FacilityCategory.CAFE);
        setupCategoryChip(binding.chipHotel, FacilityCategory.HOTEL);
        setupCategoryChip(binding.chipTourism, FacilityCategory.TOURISM);
    }

    private void setupCategoryChip(Chip chip, @Nullable FacilityCategory category) {
        chip.setCheckable(false);
        chip.setGravity(Gravity.CENTER);
        chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        chip.setOnClickListener(v -> {
            selectCategory(category, chip);
            hideCategoryMenu(true);
        });
    }

    private void toggleCategoryMenu() {
        if (categoryMenuExpanded) {
            hideCategoryMenu(true);
        } else {
            showCategoryMenu(true);
        }
    }

    private void showCategoryMenu(boolean animate) {
        categoryMenuExpanded = true;
        View menu = binding.layoutCategoryItems;
        menu.setVisibility(View.VISIBLE);

        if (!animate) {
            menu.setAlpha(1f);
            menu.setTranslationY(0f);
            return;
        }

        menu.setAlpha(0f);
        menu.setTranslationY(-menu.getResources().getDimensionPixelSize(R.dimen.spacing_sm));
        menu.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(CATEGORY_ANIMATION_MS)
                .start();
    }

    private void hideCategoryMenu(boolean animate) {
        categoryMenuExpanded = false;
        View menu = binding.layoutCategoryItems;

        if (!animate || menu.getVisibility() != View.VISIBLE) {
            menu.setVisibility(View.GONE);
            menu.setAlpha(1f);
            menu.setTranslationY(0f);
            return;
        }

        menu.animate()
                .alpha(0f)
                .translationY(-menu.getResources().getDimensionPixelSize(R.dimen.spacing_sm))
                .setDuration(CATEGORY_ANIMATION_MS)
                .withEndAction(() -> {
                    menu.setVisibility(View.GONE);
                    menu.setAlpha(1f);
                    menu.setTranslationY(0f);
                })
                .start();
    }

    private void selectCategory(@Nullable FacilityCategory category, Chip selectedChip) {
        highlightSelectedChip(selectedChip);
        viewModel.loadFacilities(category);
    }

    private void restoreCategorySelection(@Nullable FacilityCategory category) {
        Chip targetChip = binding.chipAll;
        if (category == FacilityCategory.HOSPITAL) {
            targetChip = binding.chipHospital;
        } else if (category == FacilityCategory.PHARMACY) {
            targetChip = binding.chipPharmacy;
        } else if (category == FacilityCategory.SHELTER) {
            targetChip = binding.chipShelter;
        } else if (category == FacilityCategory.RESTAURANT) {
            targetChip = binding.chipRestaurant;
        } else if (category == FacilityCategory.CAFE) {
            targetChip = binding.chipCafe;
        } else if (category == FacilityCategory.HOTEL) {
            targetChip = binding.chipHotel;
        } else if (category == FacilityCategory.TOURISM) {
            targetChip = binding.chipTourism;
        }
        highlightSelectedChip(targetChip);
    }

    private void highlightSelectedChip(Chip selectedChip) {
        clearChipHighlight(binding.chipAll);
        clearChipHighlight(binding.chipHospital);
        clearChipHighlight(binding.chipPharmacy);
        clearChipHighlight(binding.chipShelter);
        clearChipHighlight(binding.chipRestaurant);
        clearChipHighlight(binding.chipCafe);
        clearChipHighlight(binding.chipHotel);
        clearChipHighlight(binding.chipTourism);

        int primary = ContextCompat.getColor(requireContext(), R.color.color_primary);
        int onPrimary = ContextCompat.getColor(requireContext(), R.color.color_on_primary);
        selectedChip.setChipBackgroundColor(ColorStateList.valueOf(primary));
        selectedChip.setTextColor(onPrimary);
    }

    private void clearChipHighlight(Chip chip) {
        int background = ContextCompat.getColor(requireContext(), R.color.map_category_chip_background);
        chip.setChipBackgroundColor(ColorStateList.valueOf(background));
        chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_color));
    }

    private void observeViewModel() {
        viewModel.getFacilitiesState().observe(getViewLifecycleOwner(), this::renderFacilitiesState);
    }

    private void renderFacilitiesState(UiState<List<Facility>> state) {
        if (state == null) {
            return;
        }

        ViewGroup stateContainer = mapsAvailable
                ? binding.mapStateContainer
                : binding.fallbackStateContainer;

        if (state.isLoading()) {
            if (mapsAvailable && mapController != null) {
                mapController.clearMarkers();
            } else if (!mapsAvailable) {
                facilityListAdapter.setItems(null);
            }
            showOverlayState(stateContainer, R.layout.layout_loading, null);
            return;
        }

        hideOverlayState(stateContainer);

        if (state.isError()) {
            if (mapsAvailable && mapController != null) {
                mapController.clearMarkers();
            }
            if (!mapsAvailable) {
                facilityListAdapter.setItems(null);
            }
            showOverlayState(stateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.refresh());
            });
            return;
        }

        if (state.isEmpty()) {
            if (mapsAvailable && mapController != null) {
                mapController.clearMarkers();
            }
            if (!mapsAvailable) {
                facilityListAdapter.setItems(null);
            }
            showOverlayState(stateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            List<Facility> facilities = state.getData();
            if (mapsAvailable && mapController != null) {
                mapController.updateFacilities(facilities);
            } else {
                facilityListAdapter.setItems(facilities);
            }
        }
    }

    private void showFacilityBottomSheet(String facilityId) {
        if (getChildFragmentManager().findFragmentByTag("facility_bottom_sheet") != null) return;
        FacilityBottomSheetFragment bottomSheet = FacilityBottomSheetFragment.newInstance(facilityId);
        bottomSheet.showNow(getChildFragmentManager(), "facility_bottom_sheet");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        FacilityCategory category = viewModel.getSelectedCategory().getValue();
        outState.putString("selected_category", category == null ? null : category.name());
        outState.putBoolean("category_menu_expanded", categoryMenuExpanded);
    }

    private void handleLocationPermissionResult(Map<String, Boolean> result) {
        Boolean fineGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
        Boolean coarseGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)
                || PermissionHelper.hasLocationPermission(requireContext())) {
            showLocationEnabledMessage();
        } else if (binding != null) {
            Snackbar.make(binding.getRoot(), R.string.map_location_denied, Snackbar.LENGTH_LONG).show();
        }
    }

    private void promptLocationPermissionIfNeeded() {
        if (!mapsAvailable || binding == null) {
            return;
        }

        if (PermissionHelper.hasLocationPermission(requireContext())) {
            return;
        }

        if (!locationPromptShown) {
            locationPromptShown = true;
            Snackbar.make(binding.getRoot(), R.string.permission_location_rationale, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, v -> requestLocationPermission())
                    .show();
        }
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void showLocationEnabledMessage() {
        if (binding != null) {
            Snackbar.make(binding.getRoot(), R.string.map_location_enabled, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showOverlayState(ViewGroup container, int layoutRes, StateViewSetup setup) {
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);
        View stateView = getLayoutInflater().inflate(layoutRes, container, false);
        container.addView(stateView);
        if (setup != null) {
            setup.setup(stateView);
        }
    }

    private void hideOverlayState(ViewGroup container) {
        container.removeAllViews();
        container.setVisibility(View.GONE);
    }

    private interface StateViewSetup {
        void setup(View stateView);
    }

    @Override
    public void onDestroyView() {
        if (mapController != null) {
            mapController.destroy();
            mapController = null;
        }
        binding = null;
        super.onDestroyView();
    }
}
