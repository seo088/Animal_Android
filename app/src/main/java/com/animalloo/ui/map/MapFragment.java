package com.animalloo.ui.map;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;

public class MapFragment extends BaseFragment {

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    private FacilityMapController mapController;
    private FacilityListAdapter facilityListAdapter;
    private boolean mapsAvailable;
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
        }
        restoreChipSelection(category);
        setupChipGroup();
        setupFallbackList();
        setupMapContainer();
        observeViewModel();

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

    private void setupChipGroup() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener(
                (ChipGroup group, List<Integer> checkedIds) -> {
                    if (checkedIds.isEmpty()) {
                        return;
                    }
                    int checkedId = checkedIds.get(0);
                    viewModel.loadFacilities(mapChipIdToCategory(checkedId));
                });
    }

    private void restoreChipSelection(FacilityCategory category) {
        if (category == null) {
            binding.chipAll.setChecked(true);
            return;
        }

        Chip targetChip = null;
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

        if (targetChip != null) {
            targetChip.setChecked(true);
        } else {
            binding.chipAll.setChecked(true);
        }
    }

    @Nullable
    private FacilityCategory mapChipIdToCategory(int chipId) {
        if (chipId == R.id.chip_all) {
            return null;
        } else if (chipId == R.id.chip_hospital) {
            return FacilityCategory.HOSPITAL;
        } else if (chipId == R.id.chip_pharmacy) {
            return FacilityCategory.PHARMACY;
        } else if (chipId == R.id.chip_shelter) {
            return FacilityCategory.SHELTER;
        } else if (chipId == R.id.chip_restaurant) {
            return FacilityCategory.RESTAURANT;
        } else if (chipId == R.id.chip_cafe) {
            return FacilityCategory.CAFE;
        } else if (chipId == R.id.chip_hotel) {
            return FacilityCategory.HOTEL;
        } else if (chipId == R.id.chip_tourism) {
            return FacilityCategory.TOURISM;
        }
        return null;
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
