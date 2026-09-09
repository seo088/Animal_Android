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
import com.animalloo.util.MapsAvailabilityChecker;
import com.animalloo.util.PermissionHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;

public class MapFragment extends BaseFragment implements OnMapReadyCallback {

    private static final LatLng DEFAULT_SEOUL = new LatLng(37.5665, 126.9780);
    private static final float DEFAULT_ZOOM = 11f;

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    private GoogleMap googleMap;
    private FacilityListAdapter facilityListAdapter;
    private boolean mapsAvailable;
    private boolean mapFragmentAdded;
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

        setupChipGroup();
        setupFallbackList();
        setupMapContainer();
        observeViewModel();

        if (savedInstanceState == null) {
            viewModel.loadFacilities(null);
        } else {
            restoreChipSelection(viewModel.getSelectedCategory().getValue());
            viewModel.refresh();
        }
    }

    private void setupMapContainer() {
        if (mapsAvailable) {
            binding.layoutMapFallback.setVisibility(View.GONE);
            binding.mapContainer.setVisibility(View.VISIBLE);

            SupportMapFragment existingMapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
            if (existingMapFragment != null) {
                existingMapFragment.getMapAsync(this);
                mapFragmentAdded = true;
            } else if (!mapFragmentAdded) {
                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.map_container, mapFragment)
                        .commit();
                mapFragment.getMapAsync(this);
                mapFragmentAdded = true;
            }
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
            if (mapsAvailable && googleMap != null) {
                googleMap.clear();
            } else if (!mapsAvailable) {
                facilityListAdapter.setItems(null);
            }
            showOverlayState(stateContainer, R.layout.layout_loading, null);
            return;
        }

        hideOverlayState(stateContainer);

        if (state.isError()) {
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
            if (mapsAvailable && googleMap != null) {
                googleMap.clear();
            }
            if (!mapsAvailable) {
                facilityListAdapter.setItems(null);
            }
            showOverlayState(stateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            List<Facility> facilities = state.getData();
            if (mapsAvailable) {
                updateMapMarkers(facilities);
            } else {
                facilityListAdapter.setItems(facilities);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_SEOUL, DEFAULT_ZOOM));
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof String) {
                showFacilityBottomSheet((String) tag);
            }
            return false;
        });

        enableMyLocationIfPossible();

        UiState<List<Facility>> currentState = viewModel.getFacilitiesState().getValue();
        if (currentState != null && currentState.isSuccess() && currentState.getData() != null) {
            updateMapMarkers(currentState.getData());
        }
    }

    private void updateMapMarkers(List<Facility> facilities) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasMarker = false;

        for (Facility facility : facilities) {
            LatLng position = new LatLng(facility.getLatitude(), facility.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(facility.getName())
                    .snippet(facility.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerHue(facility.getCategory()))));
            if (marker != null) {
                marker.setTag(facility.getId());
                boundsBuilder.include(position);
                hasMarker = true;
            }
        }

        if (hasMarker) {
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
            } catch (IllegalStateException exception) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_SEOUL, DEFAULT_ZOOM));
            }
        }
    }

    private float getMarkerHue(FacilityCategory category) {
        if (category == FacilityCategory.HOSPITAL) {
            return BitmapDescriptorFactory.HUE_RED;
        } else if (category == FacilityCategory.PHARMACY) {
            return BitmapDescriptorFactory.HUE_AZURE;
        } else if (category == FacilityCategory.SHELTER) {
            return BitmapDescriptorFactory.HUE_ORANGE;
        } else if (category == FacilityCategory.RESTAURANT) {
            return BitmapDescriptorFactory.HUE_ROSE;
        } else if (category == FacilityCategory.CAFE) {
            return BitmapDescriptorFactory.HUE_VIOLET;
        } else if (category == FacilityCategory.HOTEL) {
            return BitmapDescriptorFactory.HUE_BLUE;
        } else if (category == FacilityCategory.TOURISM) {
            return BitmapDescriptorFactory.HUE_GREEN;
        }
        return BitmapDescriptorFactory.HUE_GREEN;
    }

    private void showFacilityBottomSheet(String facilityId) {
        FacilityBottomSheetFragment bottomSheet = FacilityBottomSheetFragment.newInstance(facilityId);
        bottomSheet.show(getChildFragmentManager(), "facility_bottom_sheet");
    }

    private void handleLocationPermissionResult(Map<String, Boolean> result) {
        Boolean fineGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
        Boolean coarseGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)
                || PermissionHelper.hasLocationPermission(requireContext())) {
            enableMyLocationLayer();
        } else if (binding != null) {
            Snackbar.make(binding.getRoot(), R.string.map_location_denied, Snackbar.LENGTH_LONG).show();
        }
    }

    private void enableMyLocationIfPossible() {
        if (googleMap == null || !mapsAvailable || binding == null) {
            return;
        }

        if (PermissionHelper.hasLocationPermission(requireContext())) {
            enableMyLocationLayer();
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

    private void enableMyLocationLayer() {
        if (googleMap == null || binding == null) {
            return;
        }
        try {
            googleMap.setMyLocationEnabled(true);
            Snackbar.make(binding.getRoot(), R.string.map_location_enabled, Snackbar.LENGTH_SHORT).show();
        } catch (SecurityException exception) {
            Snackbar.make(binding.getRoot(), R.string.map_location_denied, Snackbar.LENGTH_SHORT).show();
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
        googleMap = null;
        binding = null;
        super.onDestroyView();
    }
}
