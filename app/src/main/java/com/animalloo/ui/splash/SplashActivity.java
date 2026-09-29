package com.animalloo.ui.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.animalloo.databinding.ActivitySplashBinding;
import com.animalloo.ui.auth.AuthActivity;
import com.animalloo.ui.main.MainActivity;
import com.animalloo.util.RepositoryProvider;

public class SplashActivity extends AppCompatActivity {

    private static final long NAVIGATE_DELAY_MS = 800L;

    private ActivitySplashBinding binding;
    private AnimatorSet animatorSet;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivSplashLogo.setVisibility(View.VISIBLE);
        binding.ivSplashLogo.setAlpha(0f);
        binding.tvSplashTitle.setVisibility(View.VISIBLE);
        binding.tvSplashTitle.setAlpha(0f);

        startSplashAnimation();
    }

    private void startSplashAnimation() {
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(binding.ivSplashLogo, View.ALPHA, 0f, 1f);
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(binding.tvSplashTitle, View.ALPHA, 0f, 1f);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(logoAlpha, titleAlpha);
        animatorSet.setDuration(600L);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                handler.postDelayed(() -> navigateNext(), NAVIGATE_DELAY_MS);
            }
        });
        animatorSet.start();
    }

    private void navigateNext() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        boolean isLoggedIn = RepositoryProvider.getInstance().getAuthRepository().isLoggedIn();
        Intent intent = isLoggedIn
                ? new Intent(this, MainActivity.class)
                : new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
        binding = null;
        super.onDestroy();
    }
}
