package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutumnFragment extends Fragment {

    private ViewFlipper viewFlipper;
    private FrameLayout leafContainer;
    private final Random random = new Random();
    private int screenWidth, screenHeight;
    private Handler typingHandler;
    private MediaPlayer typingPlayer;
    private Runnable typingRunnable;

    private View quoteContainer1, quoteContainer2, buttonLayout;
    private TextView quoteText1, quoteText2;
    private final List<ImageView> activeLeaves = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.autumn_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Screen dimensions
        if (getActivity() != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        }

        viewFlipper = view.findViewById(R.id.viewFlipper);
        leafContainer = view.findViewById(R.id.leafContainer);
        typingHandler = new Handler(Looper.getMainLooper());

        quoteContainer1 = view.findViewById(R.id.quoteContainer1);
        quoteContainer2 = view.findViewById(R.id.quoteContainer2);
        buttonLayout = view.findViewById(R.id.buttonLayout);
        quoteText1 = view.findViewById(R.id.quoteText1);
        quoteText2 = view.findViewById(R.id.quoteText2);

        // Start dialogue boxes hidden so they pop in like Summer
        if (quoteContainer1 != null) {
            quoteContainer1.setAlpha(0f);
            quoteContainer1.setScaleX(0f);
            quoteContainer1.setScaleY(0f);
            quoteContainer1.setVisibility(View.INVISIBLE);
        }
        if (quoteContainer2 != null) {
            quoteContainer2.setAlpha(0f);
            quoteContainer2.setScaleX(0f);
            quoteContainer2.setScaleY(0f);
            quoteContainer2.setVisibility(View.INVISIBLE);
        }

        // Spawn multiple leaves
        for (int i = 0; i < 15; i++) {
            spawnLeaf();
        }

        // Buttons on home page → go to first fragment
        TextView choice1 = view.findViewById(R.id.btnChoice1);
        TextView choice2 = view.findViewById(R.id.btnChoice2);
        TextView choice3 = view.findViewById(R.id.btnChoice3);

        View.OnClickListener goToFirstFragment = v -> shiftToNextFragment();
        if (choice1 != null) choice1.setOnClickListener(goToFirstFragment);
        if (choice2 != null) choice2.setOnClickListener(goToFirstFragment);
        if (choice3 != null) choice3.setOnClickListener(goToFirstFragment);

        // Start messaging transition
        startMessagingSequence();
    }

    private void startMessagingSequence() {
        if (getView() != null) {
            startDialogueSequence(getView(), quoteContainer1, quoteContainer2);
        }
    }

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

    private void startDialogueSequence(View view, View box1, View box2) {
        TextView quoteText1 = view.findViewById(R.id.quoteText1);
        TextView quoteText2 = view.findViewById(R.id.quoteText2);

        if (quoteText1 != null && quoteText2 != null) {
            // STEP 1: Box 1 Pop In
            typingHandler.postDelayed(() -> {
                if (box1 != null) {
                    box1.setAlpha(0f);
                    box1.animate().alpha(1f).setDuration(300).start();
                    popInView(box1, 1f);
                }

                // Start typing after a short delay to let the box "pop" first
                typingHandler.postDelayed(() -> {
                    typeText(quoteText1, "“SOMETIMES WE NEED A DIFFERENT ANGLE.“", () -> {
                        stopTypingSfx(); // Stop typing sound after first dialogue
                        // STEP 2: Wait 0.5s after first quote finishes
                        typingHandler.postDelayed(() -> {
                            // STEP 3: Box 2 Pop In
                            if (box2 != null) {
                                box2.setAlpha(0f);
                                box2.animate().alpha(1f).setDuration(300).start();
                                popInView(box2, 1f);
                            }

                            typingHandler.postDelayed(() -> {
                                typeText(quoteText2, "“WHAT IF WE LOOKED AT IT ANOTHER WAY?“", () -> {
                                    stopTypingSfx(); // Stop typing sound after second dialogue
                                    showChoices();
                                });
                            }, 300); // 300ms delay to let the box pop
                        }, 500);
                    });
                }, 400); // 400ms delay for typing to start
            }, 100);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTypingSfx();
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
    }

    private void showChoices() {
        if (buttonLayout != null) {
            buttonLayout.setAlpha(0f);
            buttonLayout.setVisibility(View.VISIBLE);
            buttonLayout.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .setStartDelay(200)
                    .start();
        }
    }

    private void typeText(TextView textView, String fullText, Runnable onFinish) {
        if (typingRunnable != null) typingHandler.removeCallbacks(typingRunnable);
        stopTypingSfx();
        textView.setText("");
        final int[] index = {0};
        startTypingSfx();
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    textView.setText(fullText.substring(0, index[0]));
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

    private void startFloating(ImageView image, int delayMs) {
        if (image == null) return;
        image.postDelayed(() -> {
            ObjectAnimator floatAnim = ObjectAnimator.ofFloat(image, "translationY", 0f, -20f);
            floatAnim.setDuration(1000);
            floatAnim.setRepeatCount(ValueAnimator.INFINITE);
            floatAnim.setRepeatMode(ValueAnimator.REVERSE);
            floatAnim.start();
        }, delayMs);
    }

    private void spawnLeaf() {
        if (getContext() == null) return;
        ImageView leaf = new ImageView(getContext());
        leaf.setImageResource(R.drawable.leaf);
        int size = random.nextInt(50) + 30;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.leftMargin = random.nextInt(screenWidth > 0 ? screenWidth : 1000);
        leaf.setLayoutParams(params);
        leaf.setRotation(random.nextInt(360));
        leafContainer.addView(leaf);
        activeLeaves.add(leaf);

        int duration = random.nextInt(4000) + 4000;
        ObjectAnimator fall = ObjectAnimator.ofFloat(leaf, "translationY", -100, (screenHeight > 0 ? screenHeight : 2000) + 100);
        fall.setDuration(duration);
        fall.setRepeatCount(ValueAnimator.INFINITE);
        fall.setRepeatMode(ValueAnimator.RESTART);
        fall.start();

        int amplitude = random.nextInt(100) + 50;
        ObjectAnimator wave = ObjectAnimator.ofFloat(leaf, "translationX",
                leaf.getX(), leaf.getX() + (random.nextBoolean() ? amplitude : -amplitude),
                leaf.getX(), leaf.getX() - (random.nextBoolean() ? amplitude : -amplitude));
        wave.setDuration(duration * 2);
        wave.setRepeatCount(ValueAnimator.INFINITE);
        wave.setRepeatMode(ValueAnimator.REVERSE);
        wave.start();

        ObjectAnimator rotate = ObjectAnimator.ofFloat(leaf, "rotation", leaf.getRotation(), leaf.getRotation() + 360f);
        rotate.setDuration(duration * 2);
        rotate.setRepeatCount(ValueAnimator.INFINITE);
        rotate.start();
    }

    private void setupFragmentTap(View fragmentView) {
        if (fragmentView == null) return;
        ImageView frameImage = fragmentView.findViewById(R.id.frameImage);
        if (frameImage != null) {
            frameImage.setOnClickListener(v -> onCenterImageTapped(frameImage));
        }
    }

    private void setupRestoredAnimation(View restoredView) {
        if (restoredView == null) return;
        
        // Animate the dialog container with pop-in effect
        View dialogContainer = restoredView.findViewById(R.id.dialogContainer);
        if (dialogContainer != null) {
            // Wait for layout to complete, then set pivot and animate
            dialogContainer.post(() -> {
                // Set pivot to center of the view
                dialogContainer.setPivotX(dialogContainer.getWidth() / 2f);
                dialogContainer.setPivotY(dialogContainer.getHeight() / 2f);
                
                // Set initial state
                dialogContainer.setScaleX(0f);
                dialogContainer.setScaleY(0f);
                dialogContainer.setAlpha(0f);

                // Animate
                dialogContainer.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(600)
                        .setStartDelay(100)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            });
        }
        
        // Animate the leaf with rotation
        ImageView leaf = restoredView.findViewById(R.id.leafIcon);
        if (leaf != null) {
            float scale = getResources().getDisplayMetrics().density;
            leaf.setCameraDistance(8000 * scale);
            ObjectAnimator animator = ObjectAnimator.ofFloat(leaf, "rotationY", 0f, 360f);
            animator.setDuration(6000);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.start();
        }

        // Tap restored view to return to RestoreEarthFragment
        restoredView.setOnClickListener(v -> {
            RestoreEarthFragment.markSeasonRestored(getContext(), "Autumn");
            if (getActivity() != null) {
                androidx.fragment.app.FragmentTransaction transaction = getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment_container, new RestoreEarthFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void onCenterImageTapped(ImageView image){
        if(image==null) return;
        int currentIndex = viewFlipper.getDisplayedChild();
        if (currentIndex == 1 || currentIndex >= 6) {
            shiftToNextFragment();
        } else {
            // Play sliding sound effect with softened audio
            playSlidingSfx();
            image.animate().rotationBy(90f).setDuration(400).start();
            image.postDelayed(this::shiftToNextFragment, 500);
        }
    }

    private void playSlidingSfx() {
        try {
            MediaPlayer sfxPlayer = MediaPlayer.create(getContext(), R.raw.sfx_sliding);
            if (sfxPlayer != null) {
                sfxPlayer.setVolume(0.4f, 0.4f); // Soften the audio to 40% volume
                sfxPlayer.start();
                sfxPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shiftToNextFragment() {
        int lastIndex = viewFlipper.getChildCount() - 1;
        int currentIndex = viewFlipper.getDisplayedChild();

        if (currentIndex < lastIndex) {
            viewFlipper.showNext();
            View currentView = viewFlipper.getCurrentView();
            if (viewFlipper.getDisplayedChild() == lastIndex) {
                setupRestoredAnimation(currentView);
            } else {
                setupFragmentTap(currentView);
            }
        }
    }
}
