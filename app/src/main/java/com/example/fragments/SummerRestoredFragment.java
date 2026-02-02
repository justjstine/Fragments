package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SummerRestoredFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restored, container, false);

        // 1. Setup Animations
        setupClouds(view);
        // This method handles the sun rotation (Coin style)
        setupSunAnimation(view);

        // 2. Animate the Dialog Box Entry (Pop-up effect)
        View dialogContainer = view.findViewById(R.id.dialogContainer);
        if (dialogContainer != null) {
            dialogContainer.setScaleX(0f);
            dialogContainer.setScaleY(0f);
            dialogContainer.setAlpha(0f);

            dialogContainer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(200)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }

        // 3. Click anywhere to close
        view.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * UPDATED: Rotates the Sun on the Y-AXIS (Flipping like the coin).
     */
    private void setupSunAnimation(View view) {
        ImageView sun = view.findViewById(R.id.iconSun);

        if (sun != null) {
            // CHANGED: "rotationY" makes it flip like a coin.
            // (Use "rotation" if you want it to spin like a wheel).
            ObjectAnimator rotate = ObjectAnimator.ofFloat(sun, "rotationY", 0f, 360f);

            // Duration: 6 seconds for a nice steady flip
            rotate.setDuration(6000);

            rotate.setRepeatCount(ValueAnimator.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.start();
        }
    }

    private void setupClouds(View view) {
        View clouds = view.findViewById(R.id.cloudsView);
        if (clouds != null) {
            clouds.setScaleX(1.2f);
            clouds.setScaleY(1.2f);

            // Move clouds high up
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
        }
    }
}