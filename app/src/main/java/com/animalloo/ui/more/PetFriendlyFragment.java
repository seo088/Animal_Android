package com.animalloo.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.PetFriendlyFacilityAdapter;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.PetFriendlyFilter;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentPetFriendlyBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.ui.map.FacilityBottomSheetFragment;
import com.animalloo.util.MapsAvailabilityChecker;
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
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class PetFriendlyFragment extends BaseFragment implements OnMapReadyCallback {

    private static final LatLng DEFAULT_SEOUL = new LatLng(37.5665, 126.9780);
    private static final float DEFAULT_ZOOM = 11f;

    private FragmentPetFriendlyBinding binding;
    private PetFriendlyViewModel viewModel;
    private PetFriendlyFacilityAdapter facilityAdapter;
    private GoogleMap googleMap;
    private boolean mapsAvailable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPetFriendlyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PetFriendlyViewModel.class);
        mapsAvailable = MapsAvailabilityChecker.isMapsAvailable(requireContext());
        if (savedInstanceState != null) {
            viewModel.restoreFilters(new PetFriendlyFilter(
                    savedInstanceState.getString("pet_type", "전체"),
                    savedInstanceState.getString("size_limit", "전체"),
                    readNullableBoolean(savedInstanceState, "indoor"),
                    readNullableBoolean(savedInstanceState, "carrier"),
                    readNullableBoolean(savedInstanceState, "leash")),
                    savedInstanceState.getBoolean("map_mode", false));
        }
        if (!mapsAvailable) viewModel.setMapViewMode(false);

        setupToolbar();
        setupFilters();
        setupRecyclerView();
        setupMapContainer();
        setupViewToggle();
        observeViewModel();

        viewModel.loadFacilities();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragment() instanceof MoreHost) {
                ((MoreHost) getParentFragment()).onMoreBackPressed();
            }
        });
    }

    private void setupFilters() {
        ArrayAdapter<CharSequence> petTypeAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.pet_types, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPetType.setAdapter(petTypeAdapter);
        PetFriendlyFilter filter = viewModel.getFilter();
        binding.spinnerPetType.setSelection(Math.max(0, petTypeAdapter.getPosition(filter.getPetType())));
        binding.spinnerPetType.setOnItemSelectedListener(new SimpleItemSelectedListener(value ->
                viewModel.setPetTypeFilter(value)));

        ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.size_limits, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSizeLimit.setAdapter(sizeAdapter);
        binding.spinnerSizeLimit.setSelection(Math.max(0, sizeAdapter.getPosition(filter.getSizeLimit())));
        binding.spinnerSizeLimit.setOnItemSelectedListener(new SimpleItemSelectedListener(value ->
                viewModel.setSizeLimitFilter(value)));

        selectTriState(binding.chipGroupIndoor, filter.getIndoorAllowed(),
                R.id.chip_indoor_all, R.id.chip_indoor_yes, R.id.chip_indoor_no);
        selectTriState(binding.chipGroupCarrier, filter.getCarrierRequired(),
                R.id.chip_carrier_all, R.id.chip_carrier_yes, R.id.chip_carrier_no);
        selectTriState(binding.chipGroupLeash, filter.getLeashRequired(),
                R.id.chip_leash_all, R.id.chip_leash_yes, R.id.chip_leash_no);

        binding.chipGroupIndoor.setOnCheckedStateChangeListener(
                (ChipGroup group, List<Integer> checkedIds) ->
                        viewModel.setIndoorAllowedFilter(mapTriStateFilter(checkedIds,
                                R.id.chip_indoor_all, R.id.chip_indoor_yes, R.id.chip_indoor_no)));

        binding.chipGroupCarrier.setOnCheckedStateChangeListener(
                (ChipGroup group, List<Integer> checkedIds) ->
                        viewModel.setCarrierRequiredFilter(mapTriStateFilter(checkedIds,
                                R.id.chip_carrier_all, R.id.chip_carrier_yes, R.id.chip_carrier_no)));

        binding.chipGroupLeash.setOnCheckedStateChangeListener(
                (ChipGroup group, List<Integer> checkedIds) ->
                        viewModel.setLeashRequiredFilter(mapTriStateFilter(checkedIds,
                                R.id.chip_leash_all, R.id.chip_leash_yes, R.id.chip_leash_no)));
    }

    private void selectTriState(ChipGroup group, Boolean value, int allId, int yesId, int noId) {
        group.check(value == null ? allId : value ? yesId : noId);
    }

    @Nullable
    private Boolean readNullableBoolean(Bundle state, String key) {
        return state.containsKey(key) ? state.getBoolean(key) : null;
    }

    @Nullable
    private Boolean mapTriStateFilter(List<Integer> checkedIds, int allId, int yesId, int noId) {
        if (checkedIds == null || checkedIds.isEmpty()) {
            return null;
        }
        int checkedId = checkedIds.get(0);
        if (checkedId == yesId) {
            return Boolean.TRUE;
        } else if (checkedId == noId) {
            return Boolean.FALSE;
        }
        return null;
    }

    private void setupRecyclerView() {
        facilityAdapter = new PetFriendlyFacilityAdapter();
        binding.rvPetFriendlyList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPetFriendlyList.setAdapter(facilityAdapter);
        facilityAdapter.setOnFacilityClickListener(facility -> {
            if (requireActivity() instanceof DetailNavigator) {
                ((DetailNavigator) requireActivity()).navigateToDetail(
                        DetailType.FACILITY, facility.getId());
            }
        });
    }

    private void setupMapContainer() {
        if (mapsAvailable) {
            SupportMapFragment existingMapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
            if (existingMapFragment != null) {
                existingMapFragment.getMapAsync(this);
            } else {
                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.map_container, mapFragment)
                        .commit();
                mapFragment.getMapAsync(this);
            }
        }
    }

    private void setupViewToggle() {
        binding.btnToggleView.setOnClickListener(v -> viewModel.toggleViewMode());
        updateToggleButtonLabel();
    }

    private void updateToggleButtonLabel() {
        boolean showMap = Boolean.TRUE.equals(viewModel.getMapViewMode().getValue()) && mapsAvailable;
        binding.btnToggleView.setText(showMap
                ? R.string.pet_friendly_view_list
                : R.string.pet_friendly_view_map);
        binding.btnToggleView.setEnabled(mapsAvailable || showMap);
    }

    private void observeViewModel() {
        viewModel.getFacilitiesState().observe(getViewLifecycleOwner(), this::renderFacilitiesState);
        viewModel.getMapViewMode().observe(getViewLifecycleOwner(), this::renderViewMode);
    }

    private void renderViewMode(Boolean mapMode) {
        boolean showMap = Boolean.TRUE.equals(mapMode) && mapsAvailable;
        updateToggleButtonLabel();
        binding.mapContainer.setVisibility(showMap ? View.VISIBLE : View.GONE);
        binding.rvPetFriendlyList.setVisibility(showMap ? View.GONE : View.VISIBLE);

        if (Boolean.TRUE.equals(mapMode) && !mapsAvailable) {
            binding.tvMapFallbackMessage.setVisibility(View.VISIBLE);
        } else {
            binding.tvMapFallbackMessage.setVisibility(mapsAvailable ? View.GONE : View.VISIBLE);
        }

        UiState<List<Facility>> currentState = viewModel.getFacilitiesState().getValue();
        if (currentState != null && currentState.isSuccess() && currentState.getData() != null) {
            if (showMap) {
                updateMapMarkers(currentState.getData());
            } else {
                facilityAdapter.setItems(currentState.getData());
            }
        }
    }

    private void renderFacilitiesState(UiState<List<Facility>> state) {
        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            if (googleMap != null) {
                googleMap.clear();
            }
            facilityAdapter.setItems(null);
            showOverlayState(binding.stateContainer, R.layout.layout_loading, null);
            return;
        }

        hideOverlayState(binding.stateContainer);

        if (state.isError()) {
            if (googleMap != null) googleMap.clear();
            facilityAdapter.setItems(null);
            showOverlayState(binding.stateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.refresh());
            });
            return;
        }

        if (state.isEmpty()) {
            if (googleMap != null) {
                googleMap.clear();
            }
            facilityAdapter.setItems(null);
            showOverlayState(binding.stateContainer, R.layout.layout_pet_friendly_empty, stateView ->
                    stateView.findViewById(R.id.btn_reset_filters).setOnClickListener(v -> {
                        viewModel.resetFilters();
                        binding.spinnerPetType.setSelection(0);
                        binding.spinnerSizeLimit.setSelection(0);
                        binding.chipGroupIndoor.check(R.id.chip_indoor_all);
                        binding.chipGroupCarrier.check(R.id.chip_carrier_all);
                        binding.chipGroupLeash.check(R.id.chip_leash_all);
                    }));
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            List<Facility> facilities = state.getData();
            Boolean mapMode = viewModel.getMapViewMode().getValue();
            if (Boolean.TRUE.equals(mapMode) && mapsAvailable) {
                updateMapMarkers(facilities);
            } else {
                facilityAdapter.setItems(facilities);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        if (binding == null) return;
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_SEOUL, DEFAULT_ZOOM));
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof String) {
                showFacilityBottomSheet((String) tag);
            }
            return true;
        });

        UiState<List<Facility>> currentState = viewModel.getFacilitiesState().getValue();
        Boolean mapMode = viewModel.getMapViewMode().getValue();
        if (Boolean.TRUE.equals(mapMode) && currentState != null
                && currentState.isSuccess() && currentState.getData() != null) {
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

    private void showFacilityBottomSheet(Facility facility) {
        showFacilityBottomSheet(facility.getId());
    }

    private void showFacilityBottomSheet(String facilityId) {
        if (getParentFragmentManager().findFragmentByTag("pet_friendly_bottom_sheet") != null) return;
        FacilityBottomSheetFragment bottomSheet = FacilityBottomSheetFragment.newInstance(facilityId);
        bottomSheet.showNow(getParentFragmentManager(), "pet_friendly_bottom_sheet");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        PetFriendlyFilter filter = viewModel.getFilter();
        outState.putString("pet_type", filter.getPetType());
        outState.putString("size_limit", filter.getSizeLimit());
        if (filter.getIndoorAllowed() != null) outState.putBoolean("indoor", filter.getIndoorAllowed());
        if (filter.getCarrierRequired() != null) outState.putBoolean("carrier", filter.getCarrierRequired());
        if (filter.getLeashRequired() != null) outState.putBoolean("leash", filter.getLeashRequired());
        outState.putBoolean("map_mode", Boolean.TRUE.equals(viewModel.getMapViewMode().getValue()));
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

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {

        interface OnItemSelectedAction {
            void onSelected(String value);
        }

        private final OnItemSelectedAction action;

        SimpleItemSelectedListener(OnItemSelectedAction action) {
            this.action = action;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            Object item = parent.getItemAtPosition(position);
            if (item != null) {
                action.onSelected(item.toString());
            }
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
            // no-op
        }
    }

    @Override
    public void onDestroyView() {
        googleMap = null;
        binding = null;
        super.onDestroyView();
    }
}
