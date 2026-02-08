package com.example.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



public class WinterFragment extends Fragment {



    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private MediaPlayer typingPlayer;

    private TextView dogDialogue, catDialogue;
    private ImageView petDog, petCat;
    private View choicesLayout;
    private View lastSelectedButton = null;



    @Nullable

    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_winter_choice, container, false);



        dogDialogue = view.findViewById(R.id.dogDialogueText);

        catDialogue = view.findViewById(R.id.catDialogueText);

        choicesLayout = view.findViewById(R.id.buttonLayout);



// Reset state: Hide the second dialogue, pet_cat, and buttons initially

        catDialogue.setVisibility(View.INVISIBLE);

        if (petCat != null) petCat.setVisibility(View.INVISIBLE);

        choicesLayout.setVisibility(View.INVISIBLE);

        choicesLayout.setAlpha(0f);



// Click listeners for choices

        View.OnClickListener clickListener = v -> {
            if (lastSelectedButton != null) {
                lastSelectedButton.setActivated(false);
            }
            v.setActivated(true);
            lastSelectedButton = v;
            playSparkleSound();
            navigateToScene();
        };

        view.findViewById(R.id.btnChoice1).setOnClickListener(clickListener);
        view.findViewById(R.id.btnChoice2).setOnClickListener(clickListener);
        view.findViewById(R.id.btnChoice3).setOnClickListener(clickListener);



// --- ANIMATION SEQUENCE ---



// 1. Dog starts floating immediately

        startFloatingAnimation(petDog);



// 2. Dog starts typing its dialogue

        typeText(dogDialogue, "“SMALL CHOICES. NO PRESSURE.”", () -> {



// 3. Pause for 500ms then show Cat

            typingHandler.postDelayed(() -> {

                if (petCat != null) {

                    petCat.setVisibility(View.VISIBLE);

                    startFloatingAnimation(petCat); // Cat starts floating when it appears

                }

                catDialogue.setVisibility(View.VISIBLE);



// 4. Cat starts typing its dialogue

                typeText(catDialogue, "“WHICH WAY FEELS RIGHT IN THIS COLD?”", () -> {

// 5. Final reveal of the choice buttons

                    choicesLayout.setVisibility(View.VISIBLE);

                    choicesLayout.animate().alpha(1f).setDuration(500).start();

                });

            }, 500);

        });



        return view;

    }



    private void startFloatingAnimation(View view) {

        if (view == null) return;

// Animation moves up 30 pixels and reverses forever

        TranslateAnimation floatAnim = new TranslateAnimation(0, 0, 0, -30);

        floatAnim.setDuration(1000);

        floatAnim.setRepeatCount(Animation.INFINITE);

        floatAnim.setRepeatMode(Animation.REVERSE);

        view.startAnimation(floatAnim);

    }



    private void typeText(TextView target, String text, Runnable onFinish) {
        if (typingRunnable != null) typingHandler.removeCallbacks(typingRunnable);
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
                    if (onFinish != null) onFinish.run();
                }
            }
        };
        typingHandler.post(typingRunnable);
    }

    private void startTypingSfx() {
        if (typingPlayer != null) return;
        typingPlayer = MediaPlayer.create(getContext(), R.raw.sfx_typing);
        if (typingPlayer != null) {
            typingPlayer.setLooping(true);
            typingPlayer.start();
        }
    }

    private void stopTypingSfx() {
        if (typingPlayer != null) {
            typingPlayer.stop();
            typingPlayer.release();
            typingPlayer = null;
        }
    }



    private void navigateToScene() {

        if (getParentFragmentManager() != null) {

            getParentFragmentManager().beginTransaction()

                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)

                    .replace(R.id.fragment_container, new WinterFragmentScene())

                    .commit();

        }

    }

    private void playSparkleSound() {
        try {
            MediaPlayer sparklePlayer = MediaPlayer.create(getContext(), R.raw.sfx_sparkle);
            if (sparklePlayer != null) {
                sparklePlayer.setVolume(0.4f, 0.4f);
                sparklePlayer.setOnCompletionListener(MediaPlayer::release);
                sparklePlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override

    public void onDestroyView() {

        super.onDestroyView();

        typingHandler.removeCallbacksAndMessages(null);

        if (petDog != null) petDog.clearAnimation();

        if (petCat != null) petCat.clearAnimation();

    }

}