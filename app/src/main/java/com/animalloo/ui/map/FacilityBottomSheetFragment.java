package com.animalloo.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.animalloo.R;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.Facility;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.databinding.BottomSheetFacilityBinding;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.util.RepositoryProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FacilityBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_FACILITY_ID = "facility_id";

    private BottomSheetFacilityBinding binding;
    private FacilityRepository facilityRepository;
    private String facilityId;

    public static FacilityBottomSheetFragment newInstance(String facilityId) {
        FacilityBottomSheetFragment fragment = new FacilityBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FACILITY_ID, facilityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facilityRepository = RepositoryProvider.getInstance().getFacilityRepository();
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

        binding.btnSheetDetail.setOnClickListener(v -> {
            if (getActivity() instanceof DetailNavigator) {
                ((DetailNavigator) getActivity()).navigateToDetail(DetailType.FACILITY, facilityId);
            }
            dismiss();
        });

        loadFacility(facilityId);
    }

    private void loadFacility(String id) {
        facilityRepository.getFacilityById(id, new RepositoryCallback<Facility>() {
            @Override
            public void onSuccess(Facility facility) {
                if (binding == null || facility == null) {
                    return;
                }
                bindFacility(facility);
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });
    }

    private void bindFacility(Facility facility) {
        binding.tvSheetFacilityName.setText(facility.getName());
        binding.tvSheetCategory.setText(facility.getCategory().getDisplayName());
        binding.tvSheetAddress.setText(facility.getAddress());
        binding.tvSheetDistance.setText(getString(R.string.map_distance_format, facility.getDistanceKm()));
        binding.tvSheetPhone.setText(facility.getPhone());
        binding.tvSheetHours.setText(facility.getHours());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
