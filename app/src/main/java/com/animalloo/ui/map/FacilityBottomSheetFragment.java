package com.animalloo.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.animalloo.R;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.BottomSheetFacilityBinding;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.util.FacilityActions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class FacilityBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_FACILITY_ID = "facility_id";

    private BottomSheetFacilityBinding binding;
    private FacilityBottomSheetViewModel viewModel;
    private String facilityId;

    public static FacilityBottomSheetFragment newInstance(String facilityId) {
        FacilityBottomSheetFragment fragment = new FacilityBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FACILITY_ID, facilityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null) {
            dismiss();
            return;
        }

        facilityId = arguments.getString(ARG_FACILITY_ID);
        if (facilityId == null) {
            dismiss();
            return;
        }

        viewModel = new ViewModelProvider(this).get(FacilityBottomSheetViewModel.class);
        binding.btnSheetDetail.setOnClickListener(v -> {
            if (getActivity() instanceof DetailNavigator) {
                ((DetailNavigator) getActivity()).navigateToDetail(DetailType.FACILITY, facilityId);
            }
            dismiss();
        });

        viewModel.getFacilityState().observe(getViewLifecycleOwner(), this::renderFacility);
        viewModel.loadFacility(facilityId);
    }

    private void renderFacility(UiState<Facility> state) {
        if (state == null || binding == null) return;
        binding.sheetStateContainer.removeAllViews();
        binding.sheetContent.setVisibility(View.GONE);
        if (state.isSuccess() && state.getData() != null) {
            binding.sheetStateContainer.setVisibility(View.GONE);
            binding.sheetContent.setVisibility(View.VISIBLE);
            bindFacility(state.getData());
            return;
        }

        binding.sheetStateContainer.setVisibility(View.VISIBLE);
        int layout = state.isLoading() ? R.layout.layout_loading : R.layout.layout_error;
        View stateView = getLayoutInflater().inflate(layout, binding.sheetStateContainer, false);
        binding.sheetStateContainer.addView(stateView);
        if (!state.isLoading()) {
            ((TextView) stateView.findViewById(R.id.tv_error_message)).setText(
                    state.isEmpty() ? getString(R.string.empty_data) : state.getErrorMessage());
            ((MaterialButton) stateView.findViewById(R.id.btn_retry))
                    .setOnClickListener(v -> viewModel.loadFacility(facilityId));
        }
    }

    private void bindFacility(Facility facility) {
        binding.tvSheetFacilityName.setText(facility.getName());
        binding.tvSheetCategory.setText(facility.getCategory().getDisplayName());
        binding.tvSheetAddress.setText(facility.getAddress());
        binding.tvSheetDistance.setText(getString(R.string.map_distance_format, facility.getDistanceKm()));
        binding.tvSheetPhone.setText(facility.getPhone());
        binding.tvSheetHours.setText(facility.getHours());
        binding.btnSheetCall.setEnabled(facility.getPhone() != null
                && !facility.getPhone().trim().isEmpty());
        binding.btnSheetCall.setOnClickListener(v ->
                FacilityActions.dial(this, binding.getRoot(), facility));
        binding.btnSheetDirections.setOnClickListener(v ->
                FacilityActions.directions(this, binding.getRoot(), facility));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
