package com.animalloo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.animalloo.R;
import com.animalloo.databinding.ActivityMainBinding;
import com.animalloo.data.model.DetailType;
import com.animalloo.notification.NotificationHelper;
import com.animalloo.ui.detail.DetailFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.ui.diagnosis.DiagnosisFragment;
import com.animalloo.ui.home.HomeFragment;
import com.animalloo.ui.map.MapFragment;
import com.animalloo.ui.more.MoreFragment;
import com.animalloo.ui.more.MoreSectionListener;
import com.animalloo.ui.auth.AuthActivity;
import com.animalloo.ui.rescue.RescueFragment;
import com.animalloo.util.RepositoryProvider;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainActivity extends AppCompatActivity implements MainNavigator, DetailNavigator, MoreSectionListener {

    private static final class DetailEntry {
        final DetailType type;
        final String itemId;

        DetailEntry(DetailType type, String itemId) {
            this.type = type;
            this.itemId = itemId;
        }
    }

    private static final String TAG_HOME = "tag_home";
    private static final String TAG_MAP = "tag_map";
    private static final String TAG_DIAGNOSIS = "tag_diagnosis";
    private static final String TAG_RESCUE = "tag_rescue";
    private static final String TAG_MORE = "tag_more";
    private static final String TAG_DETAIL = "tag_detail";

    private ActivityMainBinding binding;

    private HomeFragment homeFragment;
    private MapFragment mapFragment;
    private DiagnosisFragment diagnosisFragment;
    private RescueFragment rescueFragment;
    private MoreFragment moreFragment;

    private Fragment activeFragment;
    private int pendingRescueTabIndex = -1;
    private int pendingMoreSection = -1;
    private boolean detailVisible;
    private OnBackPressedCallback detailBackCallback;
    private final Deque<DetailEntry> detailBackStack = new ArrayDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RepositoryProvider.getInstance().getAuthRepository().isLoggedIn()) {
            redirectToAuth();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragments(savedInstanceState);
        setupBottomNavigation();
        setupDetailBackHandler();
        handleNotificationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void redirectToAuth() {
        Intent authIntent = new Intent(this, AuthActivity.class);
        Intent currentIntent = getIntent();
        if (currentIntent != null) {
            if (currentIntent.hasExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB)) {
                authIntent.putExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB,
                        currentIntent.getIntExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB, 0));
            }
            if (currentIntent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_HOME, false)) {
                authIntent.putExtra(NotificationHelper.EXTRA_OPEN_HOME, true);
            }
        }
        authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authIntent);
        finish();
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        if (intent.hasExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB)) {
            int tabIndex = intent.getIntExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB, 0);
            navigateToRescueWithTab(tabIndex);
            intent.removeExtra(NotificationHelper.EXTRA_OPEN_RESCUE_TAB);
            return;
        }

        if (intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_HOME, false)) {
            switchToTabProgrammatically(R.id.nav_home);
            intent.removeExtra(NotificationHelper.EXTRA_OPEN_HOME);
        }
    }

    private void initFragments(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            mapFragment = new MapFragment();
            diagnosisFragment = new DiagnosisFragment();
            rescueFragment = new RescueFragment();
            moreFragment = new MoreFragment();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container, homeFragment, TAG_HOME);
            transaction.add(R.id.fragment_container, mapFragment, TAG_MAP);
            transaction.add(R.id.fragment_container, diagnosisFragment, TAG_DIAGNOSIS);
            transaction.add(R.id.fragment_container, rescueFragment, TAG_RESCUE);
            transaction.add(R.id.fragment_container, moreFragment, TAG_MORE);
            transaction.hide(mapFragment);
            transaction.hide(diagnosisFragment);
            transaction.hide(rescueFragment);
            transaction.hide(moreFragment);
            transaction.commit();

            activeFragment = homeFragment;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(TAG_HOME);
            mapFragment = (MapFragment) fragmentManager.findFragmentByTag(TAG_MAP);
            diagnosisFragment = (DiagnosisFragment) fragmentManager.findFragmentByTag(TAG_DIAGNOSIS);
            rescueFragment = (RescueFragment) fragmentManager.findFragmentByTag(TAG_RESCUE);
            moreFragment = (MoreFragment) fragmentManager.findFragmentByTag(TAG_MORE);
            detailVisible = savedInstanceState.getBoolean("detail_visible", false);
            pendingRescueTabIndex = savedInstanceState.getInt("pending_rescue_tab", -1);
            pendingMoreSection = savedInstanceState.getInt("pending_more_section", -1);

            int selectedItemId = savedInstanceState.getInt("selected_nav_item", R.id.nav_home);
            binding.bottomNavigation.setSelectedItemId(selectedItemId);
            activeFragment = findActiveFragmentByNavId(selectedItemId);
            restoreDetailOverlayIfNeeded(fragmentManager);
        }
    }

    private void restoreDetailOverlayIfNeeded(FragmentManager fragmentManager) {
        DetailFragment detailFragment = (DetailFragment) fragmentManager.findFragmentByTag(TAG_DETAIL);
        if (detailFragment != null) {
            detailVisible = true;
            binding.detailContainer.setVisibility(View.VISIBLE);
            binding.bottomNavigation.setVisibility(View.GONE);
            updateDetailBackHandlerEnabled();
        }
    }

    private void setupDetailBackHandler() {
        detailBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                closeDetail();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, detailBackCallback);
    }

    private void updateDetailBackHandlerEnabled() {
        if (detailBackCallback != null) {
            detailBackCallback.setEnabled(detailVisible);
        }
    }

    @Override
    public void navigateToDetail(DetailType type, String itemId) {
        if (detailVisible) {
            Fragment currentDetail = getSupportFragmentManager().findFragmentByTag(TAG_DETAIL);
            if (currentDetail instanceof DetailFragment) {
                DetailFragment detailFragment = (DetailFragment) currentDetail;
                if (detailFragment.getDetailType() != null && detailFragment.getItemId() != null) {
                    detailBackStack.push(new DetailEntry(
                            detailFragment.getDetailType(),
                            detailFragment.getItemId()));
                }
            }
        }

        showDetail(type, itemId);
    }

    @Override
    public void closeDetail() {
        if (!detailVisible) {
            return;
        }

        if (!detailBackStack.isEmpty()) {
            DetailEntry previous = detailBackStack.pop();
            showDetail(previous.type, previous.itemId);
            return;
        }

        detailVisible = false;
        Fragment detailFragment = getSupportFragmentManager().findFragmentByTag(TAG_DETAIL);
        if (detailFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(detailFragment)
                    .commit();
        }

        binding.detailContainer.setVisibility(View.GONE);
        binding.bottomNavigation.setVisibility(View.VISIBLE);
        updateDetailBackHandlerEnabled();
    }

    private void showDetail(DetailType type, String itemId) {
        detailVisible = true;
        DetailFragment detailFragment = DetailFragment.newInstance(type, itemId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_container, detailFragment, TAG_DETAIL)
                .commit();

        binding.detailContainer.setVisibility(View.VISIBLE);
        binding.bottomNavigation.setVisibility(View.GONE);
        updateDetailBackHandlerEnabled();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    switchFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.nav_map) {
                    switchFragment(mapFragment);
                    return true;
                } else if (itemId == R.id.nav_diagnosis) {
                    switchFragment(diagnosisFragment);
                    return true;
                } else if (itemId == R.id.nav_rescue) {
                    switchFragment(rescueFragment);
                    applyPendingRescueTabIfNeeded();
                    return true;
                } else if (itemId == R.id.nav_more) {
                    switchFragment(moreFragment);
                    applyPendingMoreSectionIfNeeded();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void navigateToTab(int bottomNavItemId) {
        switchToTabProgrammatically(bottomNavItemId);
    }

    @Override
    public void navigateToRescueWithTab(int rescueTabIndex) {
        pendingRescueTabIndex = rescueTabIndex;
        if (activeFragment == rescueFragment) {
            rescueFragment.openAction(rescueTabIndex);
            pendingRescueTabIndex = -1;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_rescue);
        } else {
            switchToTabProgrammatically(R.id.nav_rescue);
        }
    }

    @Override
    public void navigateToMoreSection(int section) {
        pendingMoreSection = section;
        if (activeFragment == moreFragment) {
            moreFragment.openSection(section);
            pendingMoreSection = -1;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_more);
        } else {
            switchToTabProgrammatically(R.id.nav_more);
        }
    }

    @Override
    public void onMoreSectionChanged(int section) {
        // Header removed; section changes rely on system back navigation.
    }

    private void applyPendingMoreSectionIfNeeded() {
        if (pendingMoreSection >= 0 && moreFragment != null) {
            moreFragment.openSection(pendingMoreSection);
            pendingMoreSection = -1;
        }
    }

    private void applyPendingRescueTabIfNeeded() {
        if (pendingRescueTabIndex >= 0 && rescueFragment != null) {
            rescueFragment.openAction(pendingRescueTabIndex);
            pendingRescueTabIndex = -1;
        }
    }

    private void switchFragment(Fragment targetFragment) {
        if (targetFragment == null || targetFragment == activeFragment) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }
        transaction.show(targetFragment);
        transaction.commit();

        activeFragment = targetFragment;
    }

    public void switchToTabProgrammatically(int bottomNavItemId) {
        if (bottomNavItemId == R.id.nav_map) {
            switchFragment(mapFragment);
        } else if (bottomNavItemId == R.id.nav_diagnosis) {
            switchFragment(diagnosisFragment);
        } else if (bottomNavItemId == R.id.nav_rescue) {
            switchFragment(rescueFragment);
            applyPendingRescueTabIfNeeded();
        } else if (bottomNavItemId == R.id.nav_more) {
            switchFragment(moreFragment);
            applyPendingMoreSectionIfNeeded();
        } else {
            switchFragment(homeFragment);
            bottomNavItemId = R.id.nav_home;
        }
        binding.bottomNavigation.setSelectedItemId(bottomNavItemId);
    }

    private Fragment findActiveFragmentByNavId(int navItemId) {
        if (navItemId == R.id.nav_map) {
            return mapFragment;
        } else if (navItemId == R.id.nav_diagnosis) {
            return diagnosisFragment;
        } else if (navItemId == R.id.nav_rescue) {
            return rescueFragment;
        } else if (navItemId == R.id.nav_more) {
            return moreFragment;
        }
        return homeFragment;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_nav_item", binding.bottomNavigation.getSelectedItemId());
        outState.putInt("pending_rescue_tab", pendingRescueTabIndex);
        outState.putInt("pending_more_section", pendingMoreSection);
        outState.putBoolean("detail_visible", detailVisible);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
