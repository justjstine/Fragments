package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ChoiceFragment extends Fragment {

    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private MediaPlayer typingPlayer;

    public ChoiceFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choices_scene, container, false);

        TextView questionText = view.findViewById(R.id.questionText);
        View choice1 = view.findViewById(R.id.choice1);
        View choice2 = view.findViewById(R.id.choice2);
        View choice3 = view.findViewById(R.id.choice3);
        ImageView dogImage = view.findViewById(R.id.kiboCharacter);
        ImageView catImage = view.findViewById(R.id.lumaCharacter);

        typeText(questionText, "HOW ARE YOU FEELING?");

        // Start floating animations
        startFloatingAnimation(dogImage, true);
        startFloatingAnimation(catImage, false);

        View.OnClickListener goToEarth = v -> showEarthScene();
        choice1.setOnClickListener(goToEarth);
        choice2.setOnClickListener(goToEarth);
        choice3.setOnClickListener(goToEarth);

        return view;
    }

    private void startFloatingAnimation(View view, boolean isKibo) {
        if (view == null) return;
        float startY = isKibo ? 0f : -20f;
        float endY = isKibo ? -20f : 0f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, startY, endY);
        animator.setDuration(1500);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void showEarthScene() {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, new EarthSceneFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void typeText(TextView target, String text) {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
        stopTypingSfx();

        target.setText("");
        final int[] index = {0};
        startTypingSfx();
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= text.length()) {
                    target.setText(text.substring(0, index[0]));
                    index[0]++;
                    typingHandler.postDelayed(this, 50);
                } else {
                    stopTypingSfx();
                }
            }
        };
        typingHandler.post(typingRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
        stopTypingSfx();
    }

    private void startTypingSfx() {
        if (getContext() == null) return;
        stopTypingSfx();
        typingPlayer = MediaPlayer.create(getContext(), R.raw.sfx_typing);
        if (typingPlayer != null) {
            typingPlayer.setLooping(true);
            typingPlayer.setVolume(0.3f, 0.3f);
            typingPlayer.start();
        }
    }

    private void stopTypingSfx() {
        if (typingPlayer != null) {
            if (typingPlayer.isPlaying()) {
                typingPlayer.stop();
            }
            typingPlayer.release();
            typingPlayer = null;
        }
    }
}
