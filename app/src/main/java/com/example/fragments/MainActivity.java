package com.example.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private ImageView nebulaLayer;
    private TextView tapToStartText;
    private View mainContent;
    private View flashOverlay;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the layout draw under the system bars (edge-to-edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        setContentView(R.layout.activity_main);

        nebulaLayer = findViewById(R.id.nebulaLayer);
        tapToStartText = findViewById(R.id.tapToStartText);
        mainContent = findViewById(R.id.mainContent);
        flashOverlay = findViewById(R.id.flashOverlay);

        // Initially hide content for the intro sequence
        mainContent.setAlpha(0f);
        mainContent.setScaleX(0.5f);
        mainContent.setScaleY(0.5f);
        tapToStartText.setAlpha(0f);

        animateNebula();
        hideSystemUI();
        startBackgroundMusic();

        // Start opening sequence
        startOpeningSequence();
    }

    private void startOpeningSequence() {
        flashOverlay.setVisibility(View.VISIBLE);
        flashOverlay.setAlpha(1f);

        // 1. Flash white then fade out
        ObjectAnimator flashOut = ObjectAnimator.ofFloat(flashOverlay, View.ALPHA, 1f, 0f);
        flashOut.setDuration(1000);
        flashOut.setStartDelay(200);

        // 2. Title Zoom In
        ObjectAnimator titleScaleX = ObjectAnimator.ofFloat(mainContent, View.SCALE_X, 0.5f, 1f);
        ObjectAnimator titleScaleY = ObjectAnimator.ofFloat(mainContent, View.SCALE_Y, 0.5f, 1f);
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(mainContent, View.ALPHA, 0f, 1f);

        AnimatorSet titleAnimation = new AnimatorSet();
        titleAnimation.playTogether(titleScaleX, titleScaleY, titleAlpha);
        titleAnimation.setDuration(1200);
        titleAnimation.setInterpolator(new AnticipateOvershootInterpolator(1.2f));

        // 3. Tap to Start appears
        ObjectAnimator tapAlpha = ObjectAnimator.ofFloat(tapToStartText, View.ALPHA, 0f, 1f);
        tapAlpha.setDuration(800);

        AnimatorSet sequence = new AnimatorSet();
        sequence.playSequentially(flashOut, titleAnimation, tapAlpha);

        sequence.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flashOverlay.setVisibility(View.GONE);
                animateTapToStart(); // Start pulsing animation

                // Enable click only after sequence is done
                findViewById(android.R.id.content).setOnClickListener(v -> {
                    playTapSfx();
                    performFlashAndProceed();
                });
            }
        });

        sequence.start();
    }

    private void performFlashAndProceed() {
        findViewById(android.R.id.content).setOnClickListener(null);

        flashOverlay.setVisibility(View.VISIBLE);
        ObjectAnimator flashIn = ObjectAnimator.ofFloat(flashOverlay, View.ALPHA, 0f, 1f);
        flashIn.setDuration(150);

        ObjectAnimator flashOut = ObjectAnimator.ofFloat(flashOverlay, View.ALPHA, 1f, 0f);
        flashOut.setDuration(500);

        flashIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showIntroScene();
                flashOut.start();
            }
        });

        flashOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flashOverlay.setVisibility(View.GONE);
            }
        });

        flashIn.start();
    }

    private void playTapSfx() {
        MediaPlayer sfxPlayer = MediaPlayer.create(this, R.raw.sfx_tap_to_start);
        if (sfxPlayer != null) {
            sfxPlayer.setVolume(0.05f, 0.05f);
            sfxPlayer.setOnCompletionListener(MediaPlayer::release);
            sfxPlayer.start();
        }
    }

    private void showIntroScene() {
        mainContent.setVisibility(View.GONE);
        tapToStartText.setVisibility(View.GONE);

        IntroSceneFragment introFragment = new IntroSceneFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, introFragment);
        transaction.commit();
    }

    private void startBackgroundMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.bg_maintheme);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(2.5f, 2.5f);
            mediaPlayer.start();
        }
    }

    private void animateNebula() {
        if (nebulaLayer == null) return;
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.03f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.03f);
        ObjectAnimator zoomAnimator = ObjectAnimator.ofPropertyValuesHolder(nebulaLayer, pvhX, pvhY);
        zoomAnimator.setDuration(180000);
        zoomAnimator.setRepeatCount(ValueAnimator.INFINITE);
        zoomAnimator.setRepeatMode(ValueAnimator.REVERSE);
        zoomAnimator.setInterpolator(new LinearInterpolator());
        zoomAnimator.start();

        ObjectAnimator shimmerAnimator = ObjectAnimator.ofFloat(nebulaLayer, View.ALPHA, 0.9f, 1.0f);
        shimmerAnimator.setDuration(5000);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        shimmerAnimator.setInterpolator(new LinearInterpolator());
        shimmerAnimator.start();
    }

    private void animateTapToStart() {
        ObjectAnimator pulseAnimator = ObjectAnimator.ofFloat(tapToStartText, View.ALPHA, 0.3f, 1.0f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.start();
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}
