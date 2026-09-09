package com.animalloo.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.animalloo.R;
import com.animalloo.data.model.PublicDataInfo;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentDataSourceBinding;
import com.animalloo.ui.common.BaseFragment;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.util.Date;

public class DataSourceFragment extends BaseFragment {

    private FragmentDataSourceBinding binding;
    private DataSourceViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDataSourceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DataSourceViewModel.class);

        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragment() instanceof MoreHost) {
                ((MoreHost) getParentFragment()).onMoreBackPressed();
            }
        });

        binding.btnFetchPublicData.setOnClickListener(v -> viewModel.fetchDemoPublicData());
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getPublicDataState().observe(getViewLifecycleOwner(), this::renderPublicDataState);
    }

    private void renderPublicDataState(UiState<PublicDataInfo> state) {
        if (state == null) {
            return;
        }

        binding.btnFetchPublicData.setEnabled(!state.isLoading());
        binding.layoutNetworkResult.setVisibility(View.GONE);
        binding.networkStateContainer.setVisibility(View.GONE);
        binding.networkStateContainer.removeAllViews();

        if (state.isLoading()) {
            binding.networkStateContainer.setVisibility(View.VISIBLE);
            View loadingView = getLayoutInflater().inflate(
                    R.layout.layout_loading, binding.networkStateContainer, false);
            binding.networkStateContainer.addView(loadingView);
            return;
        }

        if (state.isError()) {
            binding.networkStateContainer.setVisibility(View.VISIBLE);
            View errorView = getLayoutInflater().inflate(
                    R.layout.layout_error, binding.networkStateContainer, false);
            TextView messageView = errorView.findViewById(R.id.tv_error_message);
            MaterialButton retryButton = errorView.findViewById(R.id.btn_retry);
            messageView.setText(state.getErrorMessage());
            retryButton.setOnClickListener(v -> viewModel.fetchDemoPublicData());
            binding.networkStateContainer.addView(errorView);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            PublicDataInfo info = state.getData();
            binding.layoutNetworkResult.setVisibility(View.VISIBLE);
            binding.tvNetworkSource.setText(getString(
                    R.string.network_demo_source_format, info.getSourceName()));
            binding.tvNetworkTitle.setText(info.getTitle());
            binding.tvNetworkSummary.setText(info.getSummary());
            binding.tvNetworkFetchedAt.setText(getString(
                    R.string.network_demo_fetched_at_format,
                    DateFormat.getDateTimeInstance().format(new Date(info.getFetchedAtMillis()))));
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
