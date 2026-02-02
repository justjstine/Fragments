package com.example.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EarthSceneFragment extends Fragment {

    private ImageView earthImage;
    private ImageView earthCrackImage;
    private ImageView earthExplodeImage;
    private View blackFlashView;
    private View dialogueContainer;
    private ObjectAnimator rotationAnimator;
    private TextView dialogueText;
    private TextView speakerName;
    private View questionContainer;
    private TextView questionText;
    private View choice1;
    private View choice2;
    private View choice3;
    private boolean postChoiceSequenceStarted = false;
    private MediaPlayer explosionPlayer;
    private MediaPlayer crackingPlayer;
    private Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private MediaPlayer typingPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_earth_scene, container, false);

        earthImage = view.findViewById(R.id.earthImage);
        earthCrackImage = view.findViewById(R.id.earthCrackImage);
        earthExplodeImage = view.findViewById(R.id.earthExplodeImage);
        blackFlashView = view.findViewById(R.id.blackFlashView);
        dialogueContainer = view.findViewById(R.id.dialogueContainer);
        dialogueText = view.findViewById(R.id.dialogueText);
        speakerName = view.findViewById(R.id.speakerName);
        questionContainer = view.findViewById(R.id.questionContainer);
        questionText = view.findViewById(R.id.questionText);
        choice1 = view.findViewById(R.id.choice1);
        choice2 = view.findViewById(R.id.choice2);
        choice3 = view.findViewById(R.id.choice3);

        setupChoiceActions();

        // Start the animation sequence
        startAnimationSequence(view);

        return view;
    }

    private void setupChoiceActions() {
        View.OnClickListener listener = v -> startPostChoiceSequence();
        if (choice1 != null) choice1.setOnClickListener(listener);
        if (choice2 != null) choice2.setOnClickListener(listener);
        if (choice3 != null) choice3.setOnClickListener(listener);
    }

    private void startAnimationSequence(View view) {
        // 1. Flash white
        View flashView = new View(getContext());
        flashView.setBackgroundColor(0xFFFFFFFF);
        flashView.setAlpha(1f);
        ((ViewGroup) view).addView(flashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        flashView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ((ViewGroup) view).removeView(flashView);
                // 2. Earth zoom in (without rotation initially)
                earthImage.setVisibility(View.VISIBLE);
                AnimatorSet earthIntro = new AnimatorSet();
                ObjectAnimator zoomX = ObjectAnimator.ofFloat(earthImage, "scaleX", 0f, 1f);
                ObjectAnimator zoomY = ObjectAnimator.ofFloat(earthImage, "scaleY", 0f, 1f);

                earthIntro.playTogether(zoomX, zoomY);
                earthIntro.setDuration(2500);
                earthIntro.setInterpolator(new AccelerateDecelerateInterpolator());
                earthIntro.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Start rotation after zoom completes
                        rotationAnimator = ObjectAnimator.ofFloat(earthImage, "rotation", 0f, 360f);
                        rotationAnimator.setInterpolator(new LinearInterpolator());
                        rotationAnimator.setDuration(8000); // 8 seconds for slower rotation
                        rotationAnimator.start();
                    }
                });
                earthIntro.start();

                // 3. Dialogue appears after 4 seconds (KIBO speaking)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    dialogueContainer.setVisibility(View.VISIBLE);
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(dialogueContainer, "alpha", 0f, 1f);
                    fadeIn.setDuration(1500); // Slower fade-in for dialogue
                    fadeIn.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // KIBO dialogue is already showing from XML
                            // Wait 2 seconds then transition to LUMA
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                // Fade out text and speaker
                                dialogueText.animate().alpha(0f).setDuration(600).start();
                                speakerName.animate().alpha(0f).setDuration(600).withEndAction(() -> {
                                    // Switch to LUMA
                                    speakerName.setText("-LUMA");
                                    speakerName.setTextColor(0xFF90EE90);
                                    dialogueText.setText("\"SOMETHING'S WRONG...\"");

                                    // Fade in new dialogue
                                    dialogueText.setAlpha(1f);
                                    speakerName.setAlpha(0f);
                                    speakerName.animate().alpha(1f).setDuration(800).withEndAction(() -> {
                                        startGrayscaleAndFadeOut();
                                    }).start();
                                }).start();
                            }, 2000);
                        }
                    });
                    fadeIn.start();
                }, 4000);
            }
        }).start();
    }

    private void slowDownRotation() {
        // Already being handled by the 3-second initial rotation
        rotationAnimator.cancel();
    }

    private void startGrayscaleAndFadeOut() {
        // Grayscale animation for both images
        ValueAnimator grayscale = ValueAnimator.ofFloat(0f, 1f);
        grayscale.setDuration(2000);
        grayscale.addUpdateListener(animation -> {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(1 - (float) animation.getAnimatedValue());
            earthImage.setColorFilter(new ColorMatrixColorFilter(matrix));
            earthCrackImage.setColorFilter(new ColorMatrixColorFilter(matrix));
        });

        // Opacity animation for both images
        ObjectAnimator opacityFade = ObjectAnimator.ofFloat(earthImage, "alpha", 1f, 0.6f);
        opacityFade.setDuration(2000);

        ObjectAnimator crackOpacityFade = ObjectAnimator.ofFloat(earthCrackImage, "alpha", 1f, 0.6f);
        crackOpacityFade.setDuration(2000);

        AnimatorSet grayscaleSet = new AnimatorSet();
        grayscaleSet.playTogether(grayscale, opacityFade, crackOpacityFade);
        grayscaleSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Fade out dialogue then proceed to violent shake
                dialogueContainer.animate().alpha(0f).setDuration(800).withEndAction(() -> {
                    dialogueContainer.setVisibility(View.INVISIBLE);
                    performViolentShake();
                }).start();
            }
        });
        grayscaleSet.start();
    }

    private void performViolentShake() {
        // Play cracking sound
        if (getContext() != null) {
            crackingPlayer = MediaPlayer.create(getContext(), R.raw.sfx_cracking);
            if (crackingPlayer != null) {
                crackingPlayer.setVolume(0.20f, 0.20f);
                crackingPlayer.start();
            }
        }

        // Make earth_crack visible and start crossfade
        earthCrackImage.setVisibility(View.VISIBLE);

        // Crossfade from earth to earth_crack while maintaining the same 0.6f opacity
        ObjectAnimator earthFadeOut = ObjectAnimator.ofFloat(earthImage, "alpha", 0.6f, 0f);
        earthFadeOut.setDuration(1500);

        ObjectAnimator crackFadeIn = ObjectAnimator.ofFloat(earthCrackImage, "alpha", 0f, 0.6f);
        crackFadeIn.setDuration(1500);

        ObjectAnimator shakeX = ObjectAnimator.ofFloat(earthImage, "translationX",
                0, -40, 40, -40, 40, -30, 30, -20, 20, -15, 15, -10, 10, 0);
        shakeX.setDuration(1500);
        shakeX.setInterpolator(new LinearInterpolator());

        ObjectAnimator shakeY = ObjectAnimator.ofFloat(earthImage, "translationY",
                0, -30, 30, -30, 30, -20, 20, -15, 15, -10, 10, -5, 5, 0);
        shakeY.setDuration(1500);
        shakeY.setInterpolator(new LinearInterpolator());

        // Apply same shake to crack image
        ObjectAnimator shakeCrackX = ObjectAnimator.ofFloat(earthCrackImage, "translationX",
                0, -40, 40, -40, 40, -30, 30, -20, 20, -15, 15, -10, 10, 0);
        shakeCrackX.setDuration(1500);
        shakeCrackX.setInterpolator(new LinearInterpolator());

        ObjectAnimator shakeCrackY = ObjectAnimator.ofFloat(earthCrackImage, "translationY",
                0, -30, 30, -30, 30, -20, 20, -15, 15, -10, 10, -5, 5, 0);
        shakeCrackY.setDuration(1500);
        shakeCrackY.setInterpolator(new LinearInterpolator());

        AnimatorSet shakeSet = new AnimatorSet();
        shakeSet.playTogether(shakeX, shakeY, shakeCrackX, shakeCrackY, earthFadeOut, crackFadeIn);
        shakeSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // After shake ends, transition to question scene
                transitionToQuestionScene();
            }
        });
        shakeSet.start();
    }

    private void transitionToQuestionScene() {
        // 1. Fade out the dialogue
        ObjectAnimator dialogueFadeOut = ObjectAnimator.ofFloat(dialogueContainer, "alpha", 1f, 0f);
        dialogueFadeOut.setDuration(2500);
        dialogueFadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dialogueContainer.setVisibility(View.INVISIBLE);

                // 2. Pan both earth images upward smoothly
                ObjectAnimator earthPan = ObjectAnimator.ofFloat(earthImage, "translationY", 0f, -220f);
                earthPan.setDuration(1500);
                earthPan.setInterpolator(new AccelerateDecelerateInterpolator());

                ObjectAnimator crackPan = ObjectAnimator.ofFloat(earthCrackImage, "translationY", 0f, -220f);
                crackPan.setDuration(1500);
                crackPan.setInterpolator(new AccelerateDecelerateInterpolator());

                AnimatorSet panSet = new AnimatorSet();
                panSet.playTogether(earthPan, crackPan);
                panSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 3. Show question and choices
                        showQuestionAndChoices();
                    }
                });
                panSet.start();
            }
        });
        dialogueFadeOut.start();
    }

    private void showQuestionAndChoices() {
        questionContainer.setVisibility(View.VISIBLE);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(questionContainer, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.start();
    }

    private void startPostChoiceSequence() {
        if (postChoiceSequenceStarted) return;
        postChoiceSequenceStarted = true;

        if (choice1 != null) choice1.setEnabled(false);
        if (choice2 != null) choice2.setEnabled(false);
        if (choice3 != null) choice3.setEnabled(false);

        if (questionContainer != null) {
            questionContainer.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                questionContainer.setVisibility(View.INVISIBLE);
            }).start();
        }

        playBlackFlashThenShake();
    }

    private void playBlackFlashThenShake() {
        if (blackFlashView == null) {
            returnCrackToOriginThenShake();
            return;
        }
        blackFlashView.setVisibility(View.VISIBLE);
        blackFlashView.setAlpha(0f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(blackFlashView, "alpha", 0f, 1f);
        fadeIn.setDuration(250);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(blackFlashView, "alpha", 1f, 0f);
                    fadeOut.setDuration(400);
                    fadeOut.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            blackFlashView.setVisibility(View.GONE);
                            returnCrackToOriginThenShake();
                        }
                    });
                    fadeOut.start();
                }, 2000);
            }
        });
        fadeIn.start();
    }

    private void returnCrackToOriginThenShake() {
        float currentY = earthCrackImage.getTranslationY();
        earthExplodeImage.setTranslationY(currentY);

        if (Math.abs(currentY) < 1f) {
            startShakeWithExplosionOverlay();
            return;
        }

        ObjectAnimator crackReturn = ObjectAnimator.ofFloat(earthCrackImage, "translationY", currentY, 0f);
        ObjectAnimator explodeReturn = ObjectAnimator.ofFloat(earthExplodeImage, "translationY", currentY, 0f);
        AnimatorSet returnSet = new AnimatorSet();
        returnSet.playTogether(crackReturn, explodeReturn);
        returnSet.setDuration(1000);
        returnSet.setInterpolator(new AccelerateDecelerateInterpolator());
        returnSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startShakeWithExplosionOverlay();
            }
        });
        returnSet.start();
    }

    private void startShakeWithExplosionOverlay() {
        // Play cracking sound during violent shake before explosion
        if (getContext() != null) {
            MediaPlayer crackingBeforeExplosion = MediaPlayer.create(getContext(), R.raw.sfx_cracking);
            if (crackingBeforeExplosion != null) {
                crackingBeforeExplosion.setVolume(0.25f, 0.25f);
                crackingBeforeExplosion.start();
            }
        }

        earthCrackImage.setVisibility(View.VISIBLE);

        ObjectAnimator shakeCrackX = ObjectAnimator.ofFloat(earthCrackImage, "translationX",
                0, -60, 60, -60, 60, -50, 50, -60, 60, -40, 40, -50, 50, -30, 30, -40, 40, 0);
        shakeCrackX.setDuration(2200);
        shakeCrackX.setInterpolator(new LinearInterpolator());

        ObjectAnimator shakeCrackY = ObjectAnimator.ofFloat(earthCrackImage, "translationY",
                0, -50, 50, -50, 50, -40, 40, -50, 50, -30, 30, -40, 40, -20, 20, -30, 30, 0);
        shakeCrackY.setDuration(2200);
        shakeCrackY.setInterpolator(new LinearInterpolator());

        AnimatorSet shakeSet = new AnimatorSet();
        shakeSet.playTogether(shakeCrackX, shakeCrackY);
        shakeSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                replaceCrackWithExplode();
            }
        });
        shakeSet.start();
    }

    private void replaceCrackWithExplode() {
        if (getContext() != null) {
            explosionPlayer = MediaPlayer.create(getContext(), R.raw.sfx_explosion);
            if (explosionPlayer != null) {
                explosionPlayer.setVolume(0.4f, 0.4f);
                explosionPlayer.start();
            }
        }

        float targetAlpha = earthCrackImage.getAlpha();
        if (targetAlpha <= 0f) {
            targetAlpha = 0.6f;
        }
        earthExplodeImage.setColorFilter(earthCrackImage.getColorFilter());
        earthExplodeImage.setVisibility(View.VISIBLE);
        earthExplodeImage.setAlpha(0f);
        earthExplodeImage.setTranslationX(0f);
        earthExplodeImage.setTranslationY(0f);

        ObjectAnimator crackFadeOut = ObjectAnimator.ofFloat(earthCrackImage, "alpha", earthCrackImage.getAlpha(), 0f);
        ObjectAnimator explodeFadeIn = ObjectAnimator.ofFloat(earthExplodeImage, "alpha", 0f, targetAlpha);
        AnimatorSet crossFade = new AnimatorSet();
        crossFade.playTogether(crackFadeOut, explodeFadeIn);
        crossFade.setDuration(800);
        crossFade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                earthCrackImage.setVisibility(View.GONE);
                earthCrackImage.setTranslationX(0f);
                earthCrackImage.setTranslationY(0f);
                if (explosionPlayer != null) {
                    explosionPlayer.release();
                    explosionPlayer = null;
                }
                startPostExplosionDialogue();
            }
        });
        crossFade.start();
    }

    private void startPostExplosionDialogue() {
        // Clear any previous typing
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
        stopTypingSfx();

        dialogueContainer.setVisibility(View.VISIBLE);
        dialogueContainer.setAlpha(0f);
        dialogueText.setText("");
        speakerName.setText("");

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(dialogueContainer, "alpha", 0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showDialogue1();
            }
        });
        fadeIn.start();
    }

    private void showDialogue1() {
        speakerName.setText("-LUMA");
        speakerName.setTextColor(0xFF90EE90);

        dialogueText.setAlpha(1f);
        speakerName.setAlpha(0f);

        speakerName.animate().alpha(1f).setDuration(800).start();

        typeText("\"THE WORLD...\"", () -> new Handler(Looper.getMainLooper()).postDelayed(this::fadeToDialogue2, 2000));
    }

    private void fadeToDialogue2() {
        dialogueText.animate().alpha(0f).setDuration(600).start();
        speakerName.animate().alpha(0f).setDuration(600).withEndAction(this::showDialogue2).start();
    }

    private void showDialogue2() {
        speakerName.setText("-KIBO");
        speakerName.setTextColor(0xFFD4AF37);

        dialogueText.setAlpha(1f);
        speakerName.setAlpha(0f);

        speakerName.animate().alpha(1f).setDuration(800).start();

        typeText("\"IT'S BROKEN...\"", () -> new Handler(Looper.getMainLooper()).postDelayed(this::fadeToDialogue3, 2000));
    }

    private void fadeToDialogue3() {
        dialogueText.animate().alpha(0f).setDuration(600).start();
        speakerName.animate().alpha(0f).setDuration(600).withEndAction(this::showDialogue3).start();
    }

    private void showDialogue3() {
        speakerName.setText("-KIBO");
        speakerName.setTextColor(0xFFD4AF37);

        dialogueText.setAlpha(1f);
        speakerName.setAlpha(0f);

        speakerName.animate().alpha(1f).setDuration(800).start();

        typeText("\"IS THIS HOW YOU FEEL?\"", () -> new Handler(Looper.getMainLooper()).postDelayed(this::fadeToDialogue4, 2000));
    }

    private void fadeToDialogue4() {
        dialogueText.animate().alpha(0f).setDuration(600).start();
        speakerName.animate().alpha(0f).setDuration(600).withEndAction(this::showDialogue4).start();
    }

    private void showDialogue4() {
        speakerName.setText("-LUMA");
        speakerName.setTextColor(0xFF90EE90);

        dialogueText.setAlpha(1f);
        speakerName.setAlpha(0f);

        speakerName.animate().alpha(1f).setDuration(800).start();

        typeText("\"YOU MAY FEEL LIKE YOUR WORLD...\"", () ->
                new Handler(Looper.getMainLooper()).postDelayed(this::goToEarthToScene, 2000)
        );
    }

    private void goToEarthToScene() {
        if (getActivity() != null) {
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, new EarthToSceneFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void typeText(String text, @Nullable Runnable onComplete) {
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
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };
        typingHandler.post(typingRunnable);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (explosionPlayer != null) {
            explosionPlayer.release();
            explosionPlayer = null;
        }
        if (crackingPlayer != null) {
            crackingPlayer.release();
            crackingPlayer = null;
        }
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
        stopTypingSfx();
    }
}
