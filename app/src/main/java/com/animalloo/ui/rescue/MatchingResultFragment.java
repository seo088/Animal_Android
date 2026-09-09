package com.animalloo.ui.rescue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.MatchResultAdapter;
import com.animalloo.data.model.MatchResult;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentMatchingResultBinding;
import com.animalloo.ui.common.BaseFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MatchingResultFragment extends BaseFragment {

    private FragmentMatchingResultBinding binding;
    private LostReportViewModel viewModel;
    private MatchResultAdapter matchResultAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMatchingResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(LostReportViewModel.class);
        setupRecyclerView();
        binding.btnBackToReport.setOnClickListener(v -> {
            viewModel.resetSubmissionState();
            requireParentFragment().getChildFragmentManager().popBackStack();
        });
        observeViewModel();
    }

    private void setupRecyclerView() {
        matchResultAdapter = new MatchResultAdapter();
        binding.rvMatchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMatchResults.setAdapter(matchResultAdapter);
        matchResultAdapter.setOnMatchClickListener(new MatchResultAdapter.OnMatchClickListener() {
            @Override
            public void onMatchClick(MatchResult result) {
                Snackbar.make(binding.getRoot(), R.string.rescued_detail_prepare, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onMatchLongClick(MatchResult result, View anchorView) {
                // Context menu optional for match results
            }
        });
    }

    private void observeViewModel() {
        viewModel.getMatchResultsState().observe(getViewLifecycleOwner(), this::renderMatchState);
    }

    private void renderMatchState(UiState<List<MatchResult>> state) {
        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            binding.rvMatchResults.setVisibility(View.GONE);
            showStateView(R.layout.layout_loading, null);
            return;
        }

        hideStateView();

        if (state.isError()) {
            binding.rvMatchResults.setVisibility(View.GONE);
            showStateView(R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setVisibility(View.GONE);
            });
            return;
        }

        if (state.isEmpty()) {
            binding.rvMatchResults.setVisibility(View.GONE);
            showStateView(R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvMatchResults.setVisibility(View.VISIBLE);
            matchResultAdapter.setItems(state.getData());
        }
    }

    private void showStateView(int layoutRes, StateViewSetup setup) {
        binding.matchingStateContainer.removeAllViews();
        binding.matchingStateContainer.setVisibility(View.VISIBLE);
        View stateView = getLayoutInflater().inflate(layoutRes, binding.matchingStateContainer, false);
        binding.matchingStateContainer.addView(stateView);
        if (setup != null) {
            setup.setup(stateView);
        }
    }

    private void hideStateView() {
        binding.matchingStateContainer.removeAllViews();
        binding.matchingStateContainer.setVisibility(View.GONE);
    }

    private interface StateViewSetup {
        void setup(View stateView);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
