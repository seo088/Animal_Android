package com.animalloo.ui.home;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.AlertAdapter;
import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentHomeBinding;
import com.animalloo.databinding.ItemHomeShortcutBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.main.MainNavigator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class HomeFragment extends BaseFragment {

    public static final int RESCUE_TAB_LOST = 0;
    public static final int RESCUE_TAB_RESCUED = 1;

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private AlertAdapter alertAdapter;
    private MainNavigator mainNavigator;
    private AlertNotification contextMenuAlert;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof MainNavigator) {
            mainNavigator = (MainNavigator) context;
        } else {
            throw new IllegalStateException("Host Activity must implement MainNavigator");
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
        setupShortcuts();
        setupAlertsRecyclerView();
        setupSwipeRefresh();
        observeViewModel();
        viewModel.loadHomeData();
    }

    public void refreshHomeData() {
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void setupShortcuts() {
        setupShortcut(binding.shortcutHospital, R.drawable.ic_map, R.string.shortcut_hospital,
                () -> mainNavigator.navigateToTab(R.id.nav_map));
        setupShortcut(binding.shortcutDiagnosis, R.drawable.ic_diagnosis, R.string.shortcut_diagnosis,
                () -> mainNavigator.navigateToTab(R.id.nav_diagnosis));
        setupShortcut(binding.shortcutLost, R.drawable.ic_rescue, R.string.shortcut_lost_report,
                () -> mainNavigator.navigateToRescueWithTab(RESCUE_TAB_LOST));
        setupShortcut(binding.shortcutRescued, R.drawable.ic_rescue, R.string.shortcut_rescued,
                () -> mainNavigator.navigateToRescueWithTab(RESCUE_TAB_RESCUED));
        setupShortcut(binding.shortcutPetFriendly, R.drawable.ic_more, R.string.shortcut_pet_friendly,
                () -> mainNavigator.navigateToTab(R.id.nav_more));
    }

    private void setupShortcut(ItemHomeShortcutBinding shortcutBinding, int iconRes, int labelRes,
                               Runnable action) {
        shortcutBinding.ivShortcutIcon.setImageResource(iconRes);
        shortcutBinding.tvShortcutLabel.setText(labelRes);
        shortcutBinding.cardShortcut.setOnClickListener(v -> action.run());
    }

    private void setupAlertsRecyclerView() {
        alertAdapter = new AlertAdapter();
        binding.rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAlerts.setAdapter(alertAdapter);
        binding.rvAlerts.setHasFixedSize(false);

        alertAdapter.setOnAlertClickListener(new AlertAdapter.OnAlertClickListener() {
            @Override
            public void onAlertClick(AlertNotification alert) {
                Snackbar.make(binding.getRoot(), R.string.home_alert_detail_prepare, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onAlertLongClick(AlertNotification alert, View anchorView) {
                showAlertContextMenu(alert, anchorView);
            }
        });
    }

    private void showAlertContextMenu(AlertNotification alert, View anchorView) {
        contextMenuAlert = alert;
        anchorView.setOnCreateContextMenuListener((menu, view, menuInfo) -> {
            requireActivity().getMenuInflater().inflate(R.menu.context_menu_alert, menu);
        });
        anchorView.showContextMenu();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (contextMenuAlert == null) {
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == R.id.context_share) {
            Snackbar.make(binding.getRoot(),
                    getString(R.string.home_alert_shared) + " (" + contextMenuAlert.getAnimalType() + ")",
                    Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.context_favorite) {
            Snackbar.make(binding.getRoot(), R.string.home_alert_favorited, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void observeViewModel() {
        viewModel.getStatsState().observe(getViewLifecycleOwner(), this::renderStatsState);
        viewModel.getAlertsState().observe(getViewLifecycleOwner(), this::renderAlertsState);
    }

    private void renderStatsState(UiState<HomeStats> state) {
        binding.swipeRefresh.setRefreshing(false);

        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            binding.gridStats.setVisibility(View.GONE);
            showStateView(binding.statsStateContainer, R.layout.layout_loading, null);
            return;
        }

        binding.statsStateContainer.setVisibility(View.GONE);
        binding.statsStateContainer.removeAllViews();

        if (state.isError()) {
            binding.gridStats.setVisibility(View.GONE);
            showStateView(binding.statsStateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.refresh());
            });
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.gridStats.setVisibility(View.VISIBLE);
            HomeStats stats = state.getData();
            binding.tvStatProtectedValue.setText(String.valueOf(stats.getProtectedCount()));
            binding.tvStatRescuedValue.setText(String.valueOf(stats.getRescuedTodayCount()));
            binding.tvStatLostValue.setText(String.valueOf(stats.getLostReportCount()));
            binding.tvStatFacilityValue.setText(String.valueOf(stats.getFacilityCount()));
        }
    }

    private void renderAlertsState(UiState<List<AlertNotification>> state) {
        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            binding.rvAlerts.setVisibility(View.GONE);
            showStateView(binding.alertsStateContainer, R.layout.layout_loading, null);
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
            return;
        }

        if (state.isEmpty()) {
            binding.rvAlerts.setVisibility(View.GONE);
            showStateView(binding.alertsStateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvAlerts.setVisibility(View.VISIBLE);
            alertAdapter.setItems(state.getData());
        }
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
        super.onDestroyView();
        binding = null;
    }
}
