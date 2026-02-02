package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ChoicesSceneFragment extends Fragment {

    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private MediaPlayer typingPlayer;

    public ChoicesSceneFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choices_scene, container, false);

        ImageView dogImage = view.findViewById(R.id.dogImage);
        ImageView catImage = view.findViewById(R.id.catImage);
        TextView questionText = view.findViewById(R.id.questionText);
        View choice1 = view.findViewById(R.id.choice1);
        View choice2 = view.findViewById(R.id.choice2);
        View choice3 = view.findViewById(R.id.choice3);

        dogImage.setImageResource(R.drawable.pet_dog);
        catImage.setImageResource(R.drawable.pet_cat);

        // Characters float
        startFloatingAnimation(dogImage, 0);
        startFloatingAnimation(catImage, 400);

        // Type the question text similar to the intro
        typeText(questionText, "HOW ARE YOU FEELING?");

        // Apply hover/interact effect to choices
        setupChoiceHover(choice1);
        setupChoiceHover(choice2);
        setupChoiceHover(choice3);

        choice1.setOnClickListener(v -> goToEarthScene());
        choice2.setOnClickListener(v -> goToEarthScene());
        choice3.setOnClickListener(v -> goToEarthScene());

        return view;
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

    private void startFloatingAnimation(View view, long delay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -15f, 15f);
        animator.setDuration(3200);
        animator.setStartDelay(delay);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void setupChoiceHover(View choice) {
        // Subtle floating animation for the button itself to give a "hovering in space" feel
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(choice, "translationY", -5f, 5f);
        floatAnim.setDuration(2000 + (long)(Math.random() * 1000));
        floatAnim.setRepeatMode(ValueAnimator.REVERSE);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.start();

        // Scale effect on touch (Interaction Hover)
        choice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    private void goToEarthScene() {
        if (getActivity() != null) {
            EarthSceneFragment earthSceneFragment = new EarthSceneFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, earthSceneFragment);
            transaction.commit();
        }
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
