package com.animalloo.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.animalloo.R;
import com.animalloo.databinding.ActivityMainBinding;
import com.animalloo.ui.diagnosis.DiagnosisFragment;
import com.animalloo.ui.home.HomeFragment;
import com.animalloo.ui.map.MapFragment;
import com.animalloo.ui.more.MoreFragment;
import com.animalloo.ui.rescue.RescueFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "tag_home";
    private static final String TAG_MAP = "tag_map";
    private static final String TAG_DIAGNOSIS = "tag_diagnosis";
    private static final String TAG_RESCUE = "tag_rescue";
    private static final String TAG_MORE = "tag_more";

    private ActivityMainBinding binding;

    private HomeFragment homeFragment;
    private MapFragment mapFragment;
    private DiagnosisFragment diagnosisFragment;
    private RescueFragment rescueFragment;
    private MoreFragment moreFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        initFragments(savedInstanceState);
        setupBottomNavigation();
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
            updateToolbarTitle(R.string.nav_home);
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(TAG_HOME);
            mapFragment = (MapFragment) fragmentManager.findFragmentByTag(TAG_MAP);
            diagnosisFragment = (DiagnosisFragment) fragmentManager.findFragmentByTag(TAG_DIAGNOSIS);
            rescueFragment = (RescueFragment) fragmentManager.findFragmentByTag(TAG_RESCUE);
            moreFragment = (MoreFragment) fragmentManager.findFragmentByTag(TAG_MORE);

            int selectedItemId = savedInstanceState.getInt("selected_nav_item", R.id.nav_home);
            binding.bottomNavigation.setSelectedItemId(selectedItemId);
            activeFragment = findActiveFragmentByNavId(selectedItemId);
            updateToolbarTitle(getTitleResForNavItem(selectedItemId));
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    switchFragment(homeFragment, R.string.nav_home);
                    return true;
                } else if (itemId == R.id.nav_map) {
                    switchFragment(mapFragment, R.string.nav_map);
                    return true;
                } else if (itemId == R.id.nav_diagnosis) {
                    switchFragment(diagnosisFragment, R.string.nav_diagnosis);
                    return true;
                } else if (itemId == R.id.nav_rescue) {
                    switchFragment(rescueFragment, R.string.nav_rescue);
                    return true;
                } else if (itemId == R.id.nav_more) {
                    switchFragment(moreFragment, R.string.nav_more);
                    return true;
                }
                return false;
            }
        });
    }

    private void switchFragment(Fragment targetFragment, int titleRes) {
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
        updateToolbarTitle(titleRes);
    }

    private void updateToolbarTitle(int titleRes) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleRes);
        } else {
            binding.toolbar.setTitle(titleRes);
        }
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

    private int getTitleResForNavItem(int navItemId) {
        if (navItemId == R.id.nav_map) {
            return R.string.nav_map;
        } else if (navItemId == R.id.nav_diagnosis) {
            return R.string.nav_diagnosis;
        } else if (navItemId == R.id.nav_rescue) {
            return R.string.nav_rescue;
        } else if (navItemId == R.id.nav_more) {
            return R.string.nav_more;
        }
        return R.string.nav_home;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_nav_item", binding.bottomNavigation.getSelectedItemId());
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
