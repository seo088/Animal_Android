package com.animalloo.ui.rescue;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class RescueFragment extends BaseFragment {

    private static final String TAG_RESCUED = "tag_rescued_animal";
    private static final long STATS_COUNT_ANIMATION_MS = 700L;

    private FragmentRescueBinding binding;
    private LayoutRescueStatsHeaderBinding statsHeaderBinding;
    private RescueViewModel rescueViewModel;
    private LostReportViewModel lostReportViewModel;
    private RescueTimelineAdapter timelineAdapter;
    private DetailNavigator detailNavigator;
    private RescuedAnimalFragment rescuedAnimalFragment;
    private boolean pendingOpenLostReport;
    private boolean pendingOpenRescued;

    private Animation livePulseAnimation;
    private Animation statsGlowAnimation;
    private ValueAnimator statsCountAnimator;
    private int displayedRescuedCount = -1;
    private int displayedLostCount = -1;

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
        setupFilterTabs();
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
            displayedRescuedCount = savedInstanceState.getInt("displayed_rescued_count", -1);
            displayedLostCount = savedInstanceState.getInt("displayed_lost_count", -1);
            boolean rescuedVisible = savedInstanceState.getBoolean("rescued_overlay_visible", false);
            if (rescuedVisible) {
                showRescuedOverlay();
            }
        }

        rescueViewModel.loadData();
        applyPendingActions();
    }

    @Override
    public void onResume() {
        super.onResume();
        startRealtimeAnimations();
    }

    @Override
    public void onPause() {
        stopRealtimeAnimations();
        super.onPause();
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

    private void setupFilterTabs() {
        TabLayout tabLayout = statsHeaderBinding.tabLayoutRescueFilter;
        if (tabLayout.getTabCount() == 0) {
            tabLayout.addTab(tabLayout.newTab().setText(R.string.alert_type_rescue));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.alert_type_lost));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    rescueViewModel.setFilter(RescueTimelineFilter.LOST);
                } else {
                    rescueViewModel.setFilter(RescueTimelineFilter.RESCUE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // no-op
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // no-op
            }
        });

        RescueTimelineFilter currentFilter = rescueViewModel.getFilterType().getValue();
        int tabIndex = currentFilter == RescueTimelineFilter.LOST ? 1 : 0;
        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
        if (tab != null && !tab.isSelected()) {
            tab.select();
        }
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
            if (filter == null || statsHeaderBinding == null) {
                return;
            }
            int tabIndex = filter == RescueTimelineFilter.LOST ? 1 : 0;
            TabLayout.Tab tab = statsHeaderBinding.tabLayoutRescueFilter.getTabAt(tabIndex);
            if (tab != null && !tab.isSelected()) {
                tab.select();
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
        if (statsHeaderBinding == null) {
            return;
        }
        if (state == null || !state.isSuccess() || state.getData() == null) {
            statsHeaderBinding.tvRescueStats.setText(R.string.rescue_stats_loading);
            return;
        }

        HomeStats stats = state.getData();
        animateStatsText(stats.getRescuedTodayCount(), stats.getLostReportCount());
    }

    private void animateStatsText(int rescuedCount, int lostCount) {
        if (statsCountAnimator != null) {
            statsCountAnimator.cancel();
        }

        int startRescued = displayedRescuedCount >= 0 ? displayedRescuedCount : 0;
        int startLost = displayedLostCount >= 0 ? displayedLostCount : 0;

        statsCountAnimator = ValueAnimator.ofFloat(0f, 1f);
        statsCountAnimator.setDuration(STATS_COUNT_ANIMATION_MS);
        statsCountAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            int currentRescued = startRescued + Math.round((rescuedCount - startRescued) * fraction);
            int currentLost = startLost + Math.round((lostCount - startLost) * fraction);
            updateStatsText(currentRescued, currentLost);
        });
        statsCountAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                displayedRescuedCount = rescuedCount;
                displayedLostCount = lostCount;
                updateStatsText(rescuedCount, lostCount);
            }
        });
        statsCountAnimator.start();
    }

    private void updateStatsText(int rescuedCount, int lostCount) {
        if (statsHeaderBinding == null) {
            return;
        }
        statsHeaderBinding.tvRescueStats.setText(getString(
                R.string.rescue_stats_format,
                rescuedCount,
                lostCount));
    }

    private void startRealtimeAnimations() {
        if (statsHeaderBinding == null) {
            return;
        }

        if (livePulseAnimation == null) {
            livePulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_live_pulse);
        }
        if (statsGlowAnimation == null) {
            statsGlowAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_stats_realtime_glow);
        }

        statsHeaderBinding.viewLiveDot.clearAnimation();
        statsHeaderBinding.tvRescueStats.clearAnimation();

        statsHeaderBinding.viewLiveDot.startAnimation(livePulseAnimation);
        statsHeaderBinding.tvRescueStats.startAnimation(statsGlowAnimation);
    }

    private void stopRealtimeAnimations() {
        if (statsHeaderBinding == null) {
            return;
        }
        statsHeaderBinding.viewLiveDot.clearAnimation();
        statsHeaderBinding.tvRescueStats.clearAnimation();
        if (statsCountAnimator != null) {
            statsCountAnimator.cancel();
        }
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
        outState.putInt("displayed_rescued_count", displayedRescuedCount);
        outState.putInt("displayed_lost_count", displayedLostCount);
    }

    @Override
    public void onDestroyView() {
        stopRealtimeAnimations();
        binding = null;
        statsHeaderBinding = null;
        livePulseAnimation = null;
        statsGlowAnimation = null;
        super.onDestroyView();
    }
}
