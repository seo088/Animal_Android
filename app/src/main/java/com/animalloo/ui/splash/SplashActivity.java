package com.animalloo.ui.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.animalloo.R;
import com.animalloo.databinding.ActivitySplashBinding;
import com.animalloo.ui.main.MainActivity;

/**
 * AnimalLoo brand splash screen.
 * Circular Reveal, ObjectAnimator, AnimatorSet, MediaPlayer evaluation elements included.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long NAVIGATE_DELAY_MS = 600L;

    private ActivitySplashBinding binding;
    private MediaPlayer mediaPlayer;
    private AnimatorSet animatorSet;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivSplashDog.setVisibility(View.VISIBLE);
        binding.ivSplashDog.setScaleX(0f);
        binding.ivSplashDog.setScaleY(0f);
        binding.ivSplashDog.setAlpha(0f);
        binding.tvSplashTitle.setAlpha(0f);

        binding.splashRoot.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.splashRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        startSplashAnimation();
                    }
                });
    }

    private void startSplashAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.ivSplashDog, View.SCALE_X, 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.ivSplashDog, View.SCALE_Y, 0f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(binding.ivSplashDog, View.ALPHA, 0f, 1f);

        AnimatorSet appearSet = new AnimatorSet();
        appearSet.playTogether(scaleX, scaleY, alpha);
        appearSet.setDuration(500L);
        appearSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                playCircularReveal();
            }
        });
        appearSet.start();
    }

    private void playCircularReveal() {
        View root = binding.splashRoot;
        int centerX = root.getWidth() / 2;
        int centerY = root.getHeight() / 2;
        float endRadius = (float) Math.hypot(centerX, centerY);

        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(
                root, centerX, centerY, 0f, endRadius);
        revealAnimator.setDuration(600L);
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                playBarkAnimation();
            }
        });
        revealAnimator.start();
    }

    private void playBarkAnimation() {
        binding.ivSplashDog.setImageResource(R.drawable.ic_dog_bark);
        playBarkSound();

        binding.tvSplashTitle.setVisibility(View.VISIBLE);

        ObjectAnimator bounceScaleX = ObjectAnimator.ofFloat(binding.ivSplashDog, View.SCALE_X, 1f, 1.25f, 1f);
        ObjectAnimator bounceScaleY = ObjectAnimator.ofFloat(binding.ivSplashDog, View.SCALE_Y, 1f, 1.25f, 1f);
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(binding.tvSplashTitle, View.ALPHA, 0f, 1f);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(bounceScaleX, bounceScaleY, titleAlpha);
        animatorSet.setDuration(800L);
        animatorSet.setInterpolator(new OvershootInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                handler.postDelayed(() -> navigateToMain(), NAVIGATE_DELAY_MS);
            }
        });
        animatorSet.start();
    }

    private void playBarkSound() {
        releaseMediaPlayer();
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.dog_bark);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
                mediaPlayer.start();
            }
        } catch (Exception ignored) {
            releaseMediaPlayer();
        }
    }

    private void navigateToMain() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
                // Player may already be stopped.
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        releaseMediaPlayer();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
        releaseMediaPlayer();
        binding = null;
        super.onDestroy();
    }
}
