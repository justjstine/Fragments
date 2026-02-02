package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SummerGameFragment extends DialogFragment {

    private TextView tvFooterStatus;
    private final Set<Integer> clickedIds = new HashSet<>();
    private static final int TOTAL_SIGNALS = 6;

    // Audio variables
    private SoundPool soundPool;
    private final Map<Integer, Integer> soundMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summer_game, container, false);

        initAudio();
        setupGameLogic(view);

        // Background Animations
        startWaveAnimation(view);
        setupClouds(view);

        return view;
    }

    private void initAudio() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(TOTAL_SIGNALS)
                .setAudioAttributes(audioAttributes)
                .build();

        // Mapping button IDs to their specific sound files
        soundMap.put(R.id.btnBell, soundPool.load(getContext(), R.raw.bell, 1));
        soundMap.put(R.id.btnMail, soundPool.load(getContext(), R.raw.email, 1));
        soundMap.put(R.id.btnMegaphone, soundPool.load(getContext(), R.raw.megaphone, 1));
        soundMap.put(R.id.btnMessage, soundPool.load(getContext(), R.raw.message, 1));
        soundMap.put(R.id.btnMobile, soundPool.load(getContext(), R.raw.phone, 1));
        soundMap.put(R.id.btnTv, soundPool.load(getContext(), R.raw.tv, 1));
    }

    private void setupGameLogic(View view) {
        tvFooterStatus = view.findViewById(R.id.tvFooterStatus);

        int[] buttonIds = {
                R.id.btnMobile, R.id.btnMail, R.id.btnMessage,
                R.id.btnMegaphone, R.id.btnBell, R.id.btnTv
        };

        for (int id : buttonIds) {
            ImageButton btn = view.findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> handleIconClick((ImageButton) v));
            }
        }
    }

    private void handleIconClick(ImageButton btn) {
        int id = btn.getId();
        if (!clickedIds.contains(id)) {
            clickedIds.add(id);

            // Play the specific sound for this button
            Integer soundId = soundMap.get(id);
            if (soundId != null) {
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
            }

            // Grayscale + Dim effect
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            btn.setColorFilter(new ColorMatrixColorFilter(matrix));
            btn.setAlpha(0.2f);
            btn.setEnabled(false);

            updateStatusText();

            if (clickedIds.size() == TOTAL_SIGNALS) {
                onComplete();
            }
        }
    }

    private void updateStatusText() {
        int remaining = TOTAL_SIGNALS - clickedIds.size();
        if (tvFooterStatus != null) {
            tvFooterStatus.setText(remaining + " SIGNALS TO DIM");
        }
    }

    private void onComplete() {
        // 1. Visual Feedback
        if (tvFooterStatus != null) {
            tvFooterStatus.setText("SILENCED RESTORED.");
            tvFooterStatus.setTextColor(Color.GREEN);
        }

        // 2. Wait 1 second (1000ms) so user sees the success message, then switch
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) { // Check if fragment is still active
                // Close the current game fragment
                dismiss();

                // Open the Restored fragment
                SummerRestoredFragment restoredFragment = new SummerRestoredFragment();
                restoredFragment.show(getParentFragmentManager(), "SummerRestored");
            }
        }, 1000);
    }

    private void startWaveAnimation(View view) {
        View bg = view.findViewById(R.id.backgroundLayer);
        if (bg != null) {
            bg.setScaleX(1.1f); bg.setScaleY(1.1f);
            ObjectAnimator anim = ObjectAnimator.ofFloat(bg, "translationY", 0f, 40f);
            anim.setDuration(3500);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.start();
        }
    }

    /**
     * UPDATED: Sets up the cloud movement and positions them higher in the sky.
     */
    private void setupClouds(View view) {
        View clouds = view.findViewById(R.id.cloudsView);
        if (clouds != null) {
            clouds.setScaleX(1.2f);
            clouds.setScaleY(1.2f);

            // NEW: Move the clouds UP (-500 pixels) to sit in the sky area
            clouds.setTranslationY(-500f);

            ObjectAnimator cloudAnim = ObjectAnimator.ofFloat(clouds, "translationX", -40f, 40f);
            cloudAnim.setDuration(20000);
            cloudAnim.setRepeatCount(ValueAnimator.INFINITE);
            cloudAnim.setRepeatMode(ValueAnimator.REVERSE);
            cloudAnim.setInterpolator(new LinearInterpolator());
            cloudAnim.start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            View decor = window.getDecorView();
            decor.setAlpha(0f);
            decor.setScaleX(0.9f);
            decor.setScaleY(0.9f);
            decor.animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}