package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
    // Maps Button ID -> Sound Resource ID (R.raw.xxx)
    private final Map<Integer, Integer> soundResourceMap = new HashMap<>();
    // Maps Button ID -> Active Stream ID (The actual playing sound instance)
    private final Map<Integer, Integer> activeStreamMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summer_game, container, false);

        // Background Animations
        startWaveAnimation(view);
        setupClouds(view);

        // Audio and Logic
        initAudio(); // Starts the noise
        setupGameLogic(view);

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

        // 1. Set a listener to play the sound immediately when it is loaded
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) { // 0 means success
                // Find which button this sound belongs to
                for (Map.Entry<Integer, Integer> entry : soundResourceMap.entrySet()) {
                    if (entry.getValue() == sampleId) {
                        int buttonId = entry.getKey();

                        // Play immediately with Loop set to -1 (Infinite)
                        int streamId = pool.play(sampleId, 0.5f, 0.5f, 1, -1, 1.0f);

                        // Store the Stream ID so we can stop it later
                        activeStreamMap.put(buttonId, streamId);
                        break;
                    }
                }
            }
        });

        // 2. Load the sounds (This triggers the listener above)
        soundResourceMap.put(R.id.btnBell, soundPool.load(getContext(), R.raw.bell, 1));
        soundResourceMap.put(R.id.btnMail, soundPool.load(getContext(), R.raw.email, 1));
        soundResourceMap.put(R.id.btnMegaphone, soundPool.load(getContext(), R.raw.megaphone, 1));
        soundResourceMap.put(R.id.btnMessage, soundPool.load(getContext(), R.raw.message, 1));
        soundResourceMap.put(R.id.btnMobile, soundPool.load(getContext(), R.raw.phone, 1));
        soundResourceMap.put(R.id.btnTv, soundPool.load(getContext(), R.raw.tv, 1));
    }

    private void setupGameLogic(View view) {
        tvFooterStatus = view.findViewById(R.id.tvFooterStatus);

        // Update initial text to indicate chaos
        if(tvFooterStatus != null) {
            tvFooterStatus.setText("IT'S TOO LOUD! TAP TO MUTE.");
        }

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

            // --- STOP THE NOISE ---
            Integer streamId = activeStreamMap.get(id);
            if (streamId != null) {
                soundPool.stop(streamId);
                activeStreamMap.remove(id); // Remove from map
            }

            // Visual Feedback: Grayscale + Dim effect
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
            if(remaining > 0) {
                tvFooterStatus.setText(remaining + " NOISES LEFT...");
            } else {
                tvFooterStatus.setText("SILENCE RESTORED.");
            }
        }
    }

    private void onComplete() {
        // 1. Visual Feedback
        if (tvFooterStatus != null) {
            tvFooterStatus.setText("SILENCE RESTORED.");
            tvFooterStatus.setTextColor(Color.GREEN);
        }

        // 2. Wait 1 second (1000ms) so user sees the success message, then switch
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) { // Check if fragment is still active
                dismiss();
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

    private void setupClouds(View view) {
        View clouds = view.findViewById(R.id.cloudsView);
        if (clouds != null) {
            clouds.setScaleX(1.2f);
            clouds.setScaleY(1.2f);
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
            WindowCompat.setDecorFitsSystemWindows(window, false);
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(window, window.getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
                windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }

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
        // Vital: Stop all noises if the user closes the app or dialog prematurely
        if (soundPool != null) {
            for (int streamId : activeStreamMap.values()) {
                soundPool.stop(streamId);
            }
            soundPool.release();
            soundPool = null;
        }
    }
}
