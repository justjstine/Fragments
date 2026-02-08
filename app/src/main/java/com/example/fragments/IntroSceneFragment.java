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
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class IntroSceneFragment extends Fragment {

    private TextView dialogueText;
    private TextView speakerName;
    private TextView tapToContinue;
    private int currentStep = 0;
    private Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private MediaPlayer typingPlayer;

    public IntroSceneFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_scene, container, false);
        
        ImageView dogImage = view.findViewById(R.id.dogImage);
        ImageView catImage = view.findViewById(R.id.catImage);
        dialogueText = view.findViewById(R.id.dialogueText);
        speakerName = view.findViewById(R.id.speakerName);
        tapToContinue = view.findViewById(R.id.tapToContinue);

        dogImage.setImageResource(R.drawable.pet_dog);
        catImage.setImageResource(R.drawable.pet_cat);

        startFloatingAnimation(dogImage, 3200, -15f, 15f);
        startFloatingAnimation(catImage, 3200, 15f, -15f);

        animateTapToContinue();

        typeText("\"HEY... YOU MADE IT.\"");

        view.setOnClickListener(v -> {
            advanceDialogue();
        });

        return view;
    }

    private void animateTapToContinue() {
        ObjectAnimator blinkAnimator = ObjectAnimator.ofFloat(tapToContinue, View.ALPHA, 0.3f, 1.0f);
        blinkAnimator.setDuration(1500);
        blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        blinkAnimator.setInterpolator(new LinearInterpolator());
        blinkAnimator.start();
    }

    private void typeText(String text) {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
        stopTypingSfx();
        
        dialogueText.setText("");
        final int[] index = {0};
        startTypingSfx();
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= text.length()) {
                    dialogueText.setText(text.substring(0, index[0]));
                    index[0]++;
                    typingHandler.postDelayed(this, 50);
                } else {
                    stopTypingSfx();
                }
            }
        };
        typingHandler.post(typingRunnable);
    }

    private void startFloatingAnimation(View view, int duration, float startY, float endY) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", startY, endY);
        animator.setDuration(duration);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void advanceDialogue() {
        if (currentStep == 0) {
            playTapSfx();
            typeText("\"THERE'S NO RUSH, JUST STAY WITH US.\"");
            speakerName.setText("-LUMA");
            speakerName.setTextColor(0xFF90EE90);
            currentStep++;
        } else {
            playTapSfx();
            showChoiceScene();
        }
    }

    private void showChoiceScene() {
        if (getActivity() != null) {
            ChoiceFragment choiceFragment = new ChoiceFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, choiceFragment);
            transaction.commit();
        }
    }

    private void playTapSfx() {
        if (getContext() != null) {
            MediaPlayer sfxPlayer = MediaPlayer.create(getContext(), R.raw.sfx_tap_to_start);
            if (sfxPlayer != null) {
                sfxPlayer.setVolume(0.05f, 0.05f);
                sfxPlayer.setOnCompletionListener(MediaPlayer::release);
                sfxPlayer.start();
            }
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