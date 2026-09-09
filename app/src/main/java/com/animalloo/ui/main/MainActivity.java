package com.animalloo.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.animalloo.databinding.ActivityMainBinding;

/**
 * Main screen placeholder with BottomNavigationView shell.
 * Phase 4에서 Fragment 전환 로직을 구현합니다.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
