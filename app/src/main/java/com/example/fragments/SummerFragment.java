package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;

public class SummerFragment extends DialogFragment {

    private Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private MediaPlayer typingPlayer;
    private LinearLayout choicesContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summer, container, false);

        choicesContainer = view.findViewById(R.id.choicesContainer);
        if (choicesContainer != null) {
            choicesContainer.setAlpha(0f);
            choicesContainer.setVisibility(View.INVISIBLE);
            setupChoices(view);
        }

        setupWaves(view);

        // NEW: Setup Cloud Animation (Updated to move them up)
        setupClouds(view);

        // 1. Find and HIDE Pets and Dialog Boxes initially
        ImageView dogImage = view.findViewById(R.id.dogImage);
        ImageView catImage = view.findViewById(R.id.catImage);
        View questionBox1 = view.findViewById(R.id.questionBox1); // The Dog's box
        View questionBox2 = view.findViewById(R.id.questionBox2); // The Cat's box

        if (dogImage != null) {
            dogImage.setAlpha(0f);
            dogImage.setScaleX(0f);
            dogImage.setScaleY(0f);
            animatePet(dogImage);
        }
        if (questionBox1 != null) {
            questionBox1.setAlpha(0f);
            questionBox1.setScaleX(0f);
            questionBox1.setScaleY(0f);
        }

        if (catImage != null) {
            catImage.setAlpha(0f);
            catImage.setScaleX(0f);
            catImage.setScaleY(0f);
            animatePet(catImage);
        }
        if (questionBox2 != null) {
            questionBox2.setAlpha(0f);
            questionBox2.setScaleX(0f);
            questionBox2.setScaleY(0f);
        }

        // 2. Start Dialogue (Passing boxes so we can animate them)
        startDialogueSequence(view, dogImage, catImage, questionBox1, questionBox2);

        return view;
    }

    // --- POP UP ANIMATION HELPER (Shared for Pets and Boxes) ---
    private void popInView(View v, float targetScaleX) {
        v.setVisibility(View.VISIBLE);
        v.animate()
                .alpha(1f)
                .scaleX(targetScaleX)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void startDialogueSequence(View view, ImageView dogImage, ImageView catImage, View box1, View box2) {
        TextView dogText = view.findViewById(R.id.dialogueDog);
        TextView catText = view.findViewById(R.id.dialogueCat);

        if (dogText != null && catText != null) {

            // STEP 1: Dog and Box 1 Pop In
            typingHandler.postDelayed(() -> {
                if (dogImage != null) popInView(dogImage, 1f);
                if (box1 != null) popInView(box1, 1f);

                // Start typing after a short delay to let the box "pop" first
                typingHandler.postDelayed(() -> {
                    typeText(dogText, "“TOO MUCH LIGHT CAN BLIND. STEP BACK.”", () -> {

                        // STEP 2: Wait 0.5s after Dog finishes
                        typingHandler.postDelayed(() -> {

                            // STEP 3: Cat and Box 2 Pop In
                            if (catImage != null) popInView(catImage, -1f); // Flipped Cat
                            if (box2 != null) popInView(box2, 1f);

                            typingHandler.postDelayed(() -> {
                                typeText(catText, "“THERE IS SO MUCH ENERGY HERE...”", () -> {
                                    showChoices();
                                });
                            }, 300); // 300ms delay to let the Cat's box pop

                        }, 500);
                    });
                }, 400); // 400ms delay for typing to start
            }, 100);
        }
    }

    // --- THE REST OF YOUR LOGIC (Transitions, Waves, etc.) ---

    private void setupChoices(View view) {
        TextView choice1 = view.findViewById(R.id.choice1);
        TextView choice2 = view.findViewById(R.id.choice2);
        TextView choice3 = view.findViewById(R.id.choice3);
        View.OnClickListener listener = v -> handleSelection();
        if (choice1 != null) choice1.setOnClickListener(listener);
        if (choice2 != null) choice2.setOnClickListener(listener);
        if (choice3 != null) choice3.setOnClickListener(listener);
    }

    private void handleSelection() {
        stopTypingSfx();
        View view = getView();
        if (view != null) {
            view.animate().alpha(0f).scaleX(0.95f).scaleY(0.95f).setDuration(300).withEndAction(() -> {
                dismiss();
                openGameFragment();
            }).start();
        } else {
            dismiss();
            openGameFragment();
        }
    }

    private void openGameFragment() {
        SummerGameFragment gameFragment = new SummerGameFragment();
        gameFragment.show(getParentFragmentManager(), "SummerGame");
    }

    private void showChoices() {
        if (choicesContainer != null) {
            typingHandler.postDelayed(() -> {
                choicesContainer.setVisibility(View.VISIBLE);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(choicesContainer, View.ALPHA, 0f, 1f);
                fadeIn.setDuration(1000);
                fadeIn.start();
            }, 300);
        }
    }

    private void typeText(TextView targetView, String text, Runnable onComplete) {
        if (typingRunnable != null) typingHandler.removeCallbacks(typingRunnable);
        stopTypingSfx();
        targetView.setText("");
        final int[] index = {0};
        startTypingSfx();
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= text.length()) {
                    targetView.setText(text.substring(0, index[0]));
                    index[0]++;
                    typingHandler.postDelayed(this, 50);
                } else {
                    stopTypingSfx();
                    if (onComplete != null) onComplete.run();
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
            if (typingPlayer.isPlaying()) typingPlayer.stop();
            typingPlayer.release();
            typingPlayer = null;
        }
    }

    private void setupWaves(View view) {
        View background = view.findViewById(R.id.backgroundView);
        if (background != null) {
            ObjectAnimator waveAnim = ObjectAnimator.ofFloat(background, "translationY", 0f, 40f);
            waveAnim.setDuration(3500);
            waveAnim.setRepeatCount(ValueAnimator.INFINITE);
            waveAnim.setRepeatMode(ValueAnimator.REVERSE);
            waveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            background.setScaleX(1.1f); background.setScaleY(1.1f);
            waveAnim.start();
        }
    }

    /**
     * UPDATED: Sets up the cloud movement and positions them higher in the sky.
     */
    private void setupClouds(View view) {
        ImageView clouds = view.findViewById(R.id.cloudsView);

        if (clouds != null) {
            clouds.setScaleX(1.2f);
            clouds.setScaleY(1.2f);

            // NEW: Move the clouds UP (-500 pixels) to sit in the sky area
            // Adjust this number (-500f) if you want them higher or lower
            clouds.setTranslationY(-500f);

            ObjectAnimator cloudAnim = ObjectAnimator.ofFloat(clouds, "translationX", -40f, 40f);
            cloudAnim.setDuration(20000);
            cloudAnim.setRepeatCount(ValueAnimator.INFINITE);
            cloudAnim.setRepeatMode(ValueAnimator.REVERSE);
            cloudAnim.setInterpolator(new LinearInterpolator());
            cloudAnim.start();
        }
    }

    private void animatePet(View pet) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(pet, View.TRANSLATION_Y, 0f, -20f);
        animator.setDuration(1500);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
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
        }
    }
}
