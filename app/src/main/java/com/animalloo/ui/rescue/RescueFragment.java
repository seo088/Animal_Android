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
import com.animalloo.adapter.RescueTimelineAdapter;
import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.MatchResult;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentRescueBinding;
import com.animalloo.databinding.LayoutRescueStatsHeaderBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.ui.home.HomeFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class RescueFragment extends BaseFragment {

    private static final String TAG_RESCUED = "tag_rescued_animal";

    private FragmentRescueBinding binding;
    private LayoutRescueStatsHeaderBinding statsHeaderBinding;
    private RescueViewModel rescueViewModel;
    private LostReportViewModel lostReportViewModel;
    private RescueTimelineAdapter timelineAdapter;
    private DetailNavigator detailNavigator;
    private RescuedAnimalFragment rescuedAnimalFragment;
    private boolean pendingOpenLostReport;
    private boolean pendingOpenRescued;

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
        binding = FragmentRescueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rescueViewModel = new ViewModelProvider(this).get(RescueViewModel.class);
        lostReportViewModel = new ViewModelProvider(this).get(LostReportViewModel.class);

        statsHeaderBinding = LayoutRescueStatsHeaderBinding.bind(binding.rescueStatsHeader.getRoot());
        setupTimeline();
        setupFilterChips();
        setupActionBar();
        setupSwipeRefresh();
        observeViewModels();

        if (savedInstanceState == null) {
            rescuedAnimalFragment = new RescuedAnimalFragment();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.rescued_overlay_container, rescuedAnimalFragment, TAG_RESCUED)
                    .commit();
        } else {
            rescuedAnimalFragment = (RescuedAnimalFragment) getChildFragmentManager()
                    .findFragmentByTag(TAG_RESCUED);
            pendingOpenLostReport = savedInstanceState.getBoolean("pending_open_lost_report", false);
            pendingOpenRescued = savedInstanceState.getBoolean("pending_open_rescued", false);
            boolean rescuedVisible = savedInstanceState.getBoolean("rescued_overlay_visible", false);
            if (rescuedVisible) {
                showRescuedOverlay();
            }
        }

        rescueViewModel.loadData();
        applyPendingActions();
    }

    private void setupTimeline() {
        timelineAdapter = new RescueTimelineAdapter();
        binding.rvRescueTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRescueTimeline.setAdapter(timelineAdapter);
        timelineAdapter.setOnTimelineActionListener(new RescueTimelineAdapter.OnTimelineActionListener() {
            @Override
            public void onMyReportClick(LostAnimalReport report) {
                detailNavigator.navigateToDetail(DetailType.LOST_ANIMAL, report.getId());
            }

            @Override
            public void onViewMatchesClick(LostAnimalReport report) {
                showMatchingBottomSheet();
            }

            @Override
            public void onTimelineEventClick(AlertNotification alert) {
                detailNavigator.navigateToDetail(DetailType.ALERT, alert.getId());
            }
        });
    }

    private void setupFilterChips() {
        ChipGroup chipGroup = statsHeaderBinding.chipGroupRescueFilter;
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_filter_rescue) {
                rescueViewModel.setFilter(RescueTimelineFilter.RESCUE);
            } else if (checkedId == R.id.chip_filter_lost) {
                rescueViewModel.setFilter(RescueTimelineFilter.LOST);
            } else {
                rescueViewModel.setFilter(RescueTimelineFilter.ALL);
            }
        });
    }

    private void setupActionBar() {
        binding.btnLostReport.setOnClickListener(v -> showLostReportBottomSheet());
        binding.btnViewRescued.setOnClickListener(v -> showRescuedOverlay());
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshRescue.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefreshRescue.setOnRefreshListener(() -> rescueViewModel.refresh());
    }

    private void observeViewModels() {
        rescueViewModel.getStatsState().observe(getViewLifecycleOwner(), this::renderStats);
        rescueViewModel.getAlertsState().observe(getViewLifecycleOwner(), this::renderAlertsState);
        rescueViewModel.getTimelineItems().observe(getViewLifecycleOwner(), items -> {
            timelineAdapter.setItems(items);
            binding.swipeRefreshRescue.setRefreshing(false);
        });
        rescueViewModel.getFilterType().observe(getViewLifecycleOwner(), filter -> {
            if (filter == null) {
                return;
            }
            int chipId;
            switch (filter) {
                case RESCUE:
                    chipId = R.id.chip_filter_rescue;
                    break;
                case LOST:
                    chipId = R.id.chip_filter_lost;
                    break;
                case ALL:
                default:
                    chipId = R.id.chip_filter_all;
                    break;
            }
            if (statsHeaderBinding.chipGroupRescueFilter.getCheckedChipId() != chipId) {
                statsHeaderBinding.chipGroupRescueFilter.check(chipId);
            }
        });

        lostReportViewModel.getSubmitState().observe(getViewLifecycleOwner(), submitState -> {
            UiState<List<MatchResult>> matchState = lostReportViewModel.getMatchResultsState().getValue();
            if (submitState != null && submitState.isSuccess() && submitState.getData() != null) {
                rescueViewModel.updateMyReport(submitState.getData(), matchState);
            }
        });
        lostReportViewModel.getMatchResultsState().observe(getViewLifecycleOwner(), matchState -> {
            LostAnimalReport report = lostReportViewModel.getLastSubmittedReport();
            if (report != null) {
                rescueViewModel.updateMyReport(report, matchState);
            }
        });
    }

    private void renderStats(UiState<HomeStats> state) {
        if (state == null || !state.isSuccess() || state.getData() == null) {
            statsHeaderBinding.tvRescueStats.setText(R.string.rescue_stats_loading);
            return;
        }
        HomeStats stats = state.getData();
        statsHeaderBinding.tvRescueStats.setText(getString(
                R.string.rescue_stats_format,
                stats.getRescuedTodayCount(),
                stats.getLostReportCount()));
    }

    private void renderAlertsState(UiState<List<AlertNotification>> state) {
        if (state == null) {
            return;
        }

        binding.swipeRefreshRescue.setRefreshing(false);

        if (state.isLoading()) {
            binding.rvRescueTimeline.setVisibility(View.GONE);
            showStateView(R.layout.layout_loading, null);
            return;
        }

        hideStateView();

        if (state.isError()) {
            binding.rvRescueTimeline.setVisibility(View.GONE);
            showStateView(R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> rescueViewModel.refresh());
            });
            return;
        }

        binding.rvRescueTimeline.setVisibility(View.VISIBLE);
    }

    public void openAction(int actionIndex) {
        if (actionIndex == HomeFragment.RESCUE_TAB_RESCUED) {
            pendingOpenRescued = true;
            pendingOpenLostReport = false;
        } else {
            pendingOpenLostReport = true;
            pendingOpenRescued = false;
        }
        applyPendingActions();
    }

    private void applyPendingActions() {
        if (binding == null) {
            return;
        }
        if (pendingOpenLostReport) {
            pendingOpenLostReport = false;
            showLostReportBottomSheet();
        } else if (pendingOpenRescued) {
            pendingOpenRescued = false;
            showRescuedOverlay();
        }
    }

    public void onLostReportSubmitted() {
        LostAnimalReport report = lostReportViewModel.getLastSubmittedReport();
        UiState<List<MatchResult>> matchState = lostReportViewModel.getMatchResultsState().getValue();
        if (report != null) {
            rescueViewModel.updateMyReport(report, matchState);
        }
    }

    private void showLostReportBottomSheet() {
        if (getChildFragmentManager().findFragmentByTag("lost_report_bottom_sheet") != null) {
            return;
        }
        LostReportBottomSheet.newInstance().show(getChildFragmentManager(), "lost_report_bottom_sheet");
    }

    private void showMatchingBottomSheet() {
        if (lostReportViewModel.getLastSubmittedReport() == null) {
            return;
        }
        if (getChildFragmentManager().findFragmentByTag("matching_bottom_sheet") != null) {
            return;
        }
        MatchingBottomSheet.newInstance().show(getChildFragmentManager(), "matching_bottom_sheet");
    }

    private void showRescuedOverlay() {
        binding.rescueTimelineContainer.setVisibility(View.GONE);
        binding.rescuedOverlayContainer.setVisibility(View.VISIBLE);
        if (rescuedAnimalFragment != null) {
            rescuedAnimalFragment.setOverlayMode(true, this::hideRescuedOverlay);
        }
    }

    private void hideRescuedOverlay() {
        binding.rescuedOverlayContainer.setVisibility(View.GONE);
        binding.rescueTimelineContainer.setVisibility(View.VISIBLE);
    }

    private void showStateView(int layoutRes, StateViewSetup setup) {
        binding.rescueStateContainer.removeAllViews();
        binding.rescueStateContainer.setVisibility(View.VISIBLE);
        View stateView = getLayoutInflater().inflate(layoutRes, binding.rescueStateContainer, false);
        binding.rescueStateContainer.addView(stateView);
        if (setup != null) {
            setup.setup(stateView);
        }
    }

    private void hideStateView() {
        binding.rescueStateContainer.removeAllViews();
        binding.rescueStateContainer.setVisibility(View.GONE);
    }

    private interface StateViewSetup {
        void setup(View stateView);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("pending_open_lost_report", pendingOpenLostReport);
        outState.putBoolean("pending_open_rescued", pendingOpenRescued);
        outState.putBoolean("rescued_overlay_visible",
                binding != null && binding.rescuedOverlayContainer.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        statsHeaderBinding = null;
        super.onDestroyView();
    }
}
