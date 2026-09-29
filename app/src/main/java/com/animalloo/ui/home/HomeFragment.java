package com.animalloo.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.animalloo.R;
import com.animalloo.adapter.AlertAdapter;
import com.animalloo.adapter.HomeStatCarouselAdapter;
import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.AlertType;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentHomeBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.ui.main.MainNavigator;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment {

    public static final int RESCUE_TAB_LOST = 0;
    public static final int RESCUE_TAB_RESCUED = 1;
    private static final long STAT_AUTO_SLIDE_MS = 4000L;

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private AlertAdapter alertAdapter;
    private HomeStatCarouselAdapter statCarouselAdapter;
    private MainNavigator mainNavigator;
    private DetailNavigator detailNavigator;
    private AlertNotification contextMenuAlert;
    private TabLayoutMediator statsTabMediator;
    private final Handler statSlideHandler = new Handler(Looper.getMainLooper());
    private final Runnable statSlideRunnable = new Runnable() {
        @Override
        public void run() {
            advanceStatsCarousel();
            statSlideHandler.postDelayed(this, STAT_AUTO_SLIDE_MS);
        }
    };

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof MainNavigator) {
            mainNavigator = (MainNavigator) context;
        } else {
            throw new IllegalStateException("Host Activity must implement MainNavigator");
        }
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
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        setupStatsCarousel();
        setupAlertsRecyclerView();
        setupSwipeRefresh();
        observeViewModel();
        viewModel.loadHomeData();
    }

    @Override
    public void onResume() {
        super.onResume();
        startStatsAutoSlide();
    }

    @Override
    public void onPause() {
        stopStatsAutoSlide();
        super.onPause();
    }

    public void refreshHomeData() {
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void setupStatsCarousel() {
        statCarouselAdapter = new HomeStatCarouselAdapter();
        binding.vpStats.setAdapter(statCarouselAdapter);
        binding.vpStats.setOffscreenPageLimit(1);
        binding.vpStats.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                restartStatsAutoSlide();
            }
        });

        RecyclerView recyclerView = (RecyclerView) binding.vpStats.getChildAt(0);
        if (recyclerView != null) {
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    private void setupAlertsRecyclerView() {
        alertAdapter = new AlertAdapter();
        binding.rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAlerts.setAdapter(alertAdapter);
        binding.rvAlerts.setHasFixedSize(false);

        alertAdapter.setOnAlertClickListener(new AlertAdapter.OnAlertClickListener() {
            @Override
            public void onAlertClick(AlertNotification alert) {
                detailNavigator.navigateToDetail(DetailType.ALERT, alert.getId());
            }

            @Override
            public void onAlertLongClick(AlertNotification alert, View anchorView) {
                showAlertContextMenu(alert, anchorView);
            }
        });
    }

    private void showAlertContextMenu(AlertNotification alert, View anchorView) {
        contextMenuAlert = alert;
        anchorView.setOnCreateContextMenuListener((menu, view, menuInfo) ->
                requireActivity().getMenuInflater().inflate(R.menu.context_menu_alert, menu));
        anchorView.showContextMenu();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (contextMenuAlert == null) {
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == R.id.context_share) {
            shareAlert(contextMenuAlert);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void shareAlert(AlertNotification alert) {
        String typeLabel = alert.getType() == AlertType.RESCUE
                ? getString(R.string.alert_type_rescue)
                : getString(R.string.alert_type_lost);
        String shareText = getString(
                R.string.home_alert_share_format,
                typeLabel,
                alert.getAnimalType(),
                alert.getRegion(),
                alert.getOccurredAt(),
                alert.getStatus());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.home_alert_share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.context_share)));
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void observeViewModel() {
        viewModel.getProfileState().observe(getViewLifecycleOwner(), this::renderProfileState);
        viewModel.getStatsState().observe(getViewLifecycleOwner(), this::renderStatsState);
        viewModel.getAlertsState().observe(getViewLifecycleOwner(), this::renderAlertsState);
    }

    private void renderProfileState(UiState<HomeProfile> state) {
        if (state == null || !state.isSuccess() || state.getData() == null) {
            return;
        }

        HomeProfile profile = state.getData();
        Glide.with(this)
                .load(profile.getUserPhotoUrl())
                .placeholder(profile.getUserPhotoFallbackResId())
                .error(profile.getUserPhotoFallbackResId())
                .circleCrop()
                .into(binding.layoutProfileHeader.ivUserProfile);
        binding.layoutProfileHeader.tvUserGreeting.setText(
                getString(R.string.home_greeting_format, profile.getUserDisplayName()));
        binding.layoutProfileHeader.tvPetName.setText(profile.getPetName());
        binding.layoutProfileHeader.tvPetInfo.setText(
                getString(R.string.home_pet_info_format, profile.getPetBreed(), profile.getPetAge()));
    }

    private void renderStatsState(UiState<HomeStats> state) {
        if (state == null) {
            updateSwipeRefreshState();
            return;
        }

        if (state.isLoading()) {
            binding.vpStats.setVisibility(View.GONE);
            binding.statsIndicator.setVisibility(View.GONE);
            statCarouselAdapter.setItems(null);
            detachStatsIndicator();
            showStateView(binding.statsStateContainer, R.layout.layout_loading, null);
            updateSwipeRefreshState();
            return;
        }

        binding.statsStateContainer.setVisibility(View.GONE);
        binding.statsStateContainer.removeAllViews();

        if (state.isError()) {
            binding.vpStats.setVisibility(View.GONE);
            binding.statsIndicator.setVisibility(View.GONE);
            statCarouselAdapter.setItems(null);
            detachStatsIndicator();
            showStateView(binding.statsStateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.refresh());
            });
            updateSwipeRefreshState();
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.vpStats.setVisibility(View.VISIBLE);
            binding.statsIndicator.setVisibility(View.VISIBLE);
            statCarouselAdapter.setItems(buildStatSlides(state.getData()));
            binding.vpStats.setCurrentItem(0, false);
            attachStatsIndicator();
            restartStatsAutoSlide();
        }

        updateSwipeRefreshState();
    }

    private List<HomeStatCarouselAdapter.StatSlide> buildStatSlides(HomeStats stats) {
        List<HomeStatCarouselAdapter.StatSlide> slides = new ArrayList<>();
        slides.add(new HomeStatCarouselAdapter.StatSlide(
                String.valueOf(stats.getProtectedCount()),
                getString(R.string.home_stats_protected),
                HomeStatSlideImages.PROTECTED,
                HomeStatSlideImages.fallbackForProtected(),
                0x990F3D28));
        slides.add(new HomeStatCarouselAdapter.StatSlide(
                String.valueOf(stats.getRescuedTodayCount()),
                getString(R.string.home_stats_rescued_today),
                HomeStatSlideImages.RESCUED_TODAY,
                HomeStatSlideImages.fallbackForRescued(),
                0x991B3D5C));
        slides.add(new HomeStatCarouselAdapter.StatSlide(
                String.valueOf(stats.getLostReportCount()),
                getString(R.string.home_stats_lost_report),
                HomeStatSlideImages.LOST_REPORT,
                HomeStatSlideImages.fallbackForLost(),
                0x995C3A12));
        slides.add(new HomeStatCarouselAdapter.StatSlide(
                String.valueOf(stats.getFacilityCount()),
                getString(R.string.home_stats_facilities),
                HomeStatSlideImages.FACILITIES,
                HomeStatSlideImages.fallbackForFacilities(),
                0x993D2F6B));
        return slides;
    }

    private void renderAlertsState(UiState<List<AlertNotification>> state) {
        if (state == null) {
            updateSwipeRefreshState();
            return;
        }

        if (state.isLoading()) {
            binding.rvAlerts.setVisibility(View.GONE);
            showStateView(binding.alertsStateContainer, R.layout.layout_loading, null);
            updateSwipeRefreshState();
            return;
        }

        binding.alertsStateContainer.removeAllViews();
        binding.alertsStateContainer.setVisibility(View.GONE);

        if (state.isError()) {
            binding.rvAlerts.setVisibility(View.GONE);
            showStateView(binding.alertsStateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.refresh());
            });
            updateSwipeRefreshState();
            return;
        }

        if (state.isEmpty()) {
            binding.rvAlerts.setVisibility(View.GONE);
            showStateView(binding.alertsStateContainer, R.layout.layout_empty, null);
            updateSwipeRefreshState();
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvAlerts.setVisibility(View.VISIBLE);
            alertAdapter.setItems(state.getData());
        }

        updateSwipeRefreshState();
    }

    private void startStatsAutoSlide() {
        stopStatsAutoSlide();
        if (binding == null || statCarouselAdapter == null || statCarouselAdapter.getItemCount() <= 1) {
            return;
        }
        statSlideHandler.postDelayed(statSlideRunnable, STAT_AUTO_SLIDE_MS);
    }

    private void restartStatsAutoSlide() {
        if (isResumed()) {
            startStatsAutoSlide();
        }
    }

    private void stopStatsAutoSlide() {
        statSlideHandler.removeCallbacks(statSlideRunnable);
    }

    private void attachStatsIndicator() {
        detachStatsIndicator();
        statsTabMediator = new TabLayoutMediator(
                binding.statsIndicator, binding.vpStats, (tab, position) -> { });
        statsTabMediator.attach();
    }

    private void detachStatsIndicator() {
        if (statsTabMediator != null) {
            statsTabMediator.detach();
            statsTabMediator = null;
        }
    }

    private void advanceStatsCarousel() {
        if (binding == null || statCarouselAdapter == null || statCarouselAdapter.getItemCount() == 0) {
            return;
        }
        int nextItem = (binding.vpStats.getCurrentItem() + 1) % statCarouselAdapter.getItemCount();
        binding.vpStats.setCurrentItem(nextItem, true);
    }

    private void updateSwipeRefreshState() {
        if (binding == null || viewModel == null) {
            return;
        }
        UiState<HomeStats> stats = viewModel.getStatsState().getValue();
        UiState<List<AlertNotification>> alerts = viewModel.getAlertsState().getValue();
        boolean statsLoading = stats != null && stats.isLoading();
        boolean alertsLoading = alerts != null && alerts.isLoading();
        binding.swipeRefresh.setRefreshing(statsLoading || alertsLoading);
    }

    private void showStateView(ViewGroup container, int layoutRes,
                               StateViewSetup setup) {
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);
        View stateView = getLayoutInflater().inflate(layoutRes, container, false);
        container.addView(stateView);
        if (setup != null) {
            setup.setup(stateView);
        }
    }

    private interface StateViewSetup {
        void setup(View stateView);
    }

    @Override
    public void onDestroyView() {
        stopStatsAutoSlide();
        detachStatsIndicator();
        binding = null;
        super.onDestroyView();
    }
}
