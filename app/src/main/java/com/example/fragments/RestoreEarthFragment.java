package com.example.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.SoundPool;
import android.media.AudioAttributes;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RestoreEarthFragment extends Fragment {

    public static final String PREFS_NAME = "restore_progress";
    public static final String KEY_SPRING = "restored_spring";
    public static final String KEY_SUMMER = "restored_summer";
    public static final String KEY_AUTUMN = "restored_autumn";
    public static final String KEY_WINTER = "restored_winter";
    public static final String KEY_PENDING_COMPLETE = "pending_complete_transition";

    private ImageView earthImage;
    private FrameLayout earthContainer;
    private ImageView earthSpringReveal;
    private ImageView earthSummerReveal;
    private ImageView earthAutumnReveal;
    private ImageView earthWinterReveal;
    private ImageView springIcon;
    private ImageView summerIcon;
    private ImageView autumnIcon;
    private ImageView winterIcon;
    private TextView fragmentCounter;
    private TextView titleText;
    private Button restoreEarthButton;
    private ImageView kiboCharacter;
    private ImageView lumaCharacter;
    private View kiboDialogueContainer;
    private View lumaDialogueContainer;
    private TextView kiboDialogueText;
    private TextView lumaDialogueText;
    private View finalDialogueContainer;
    private TextView finalDialogueText;
    private View finalChoicesContainer;
    private View choiceRevisit;
    private View choiceNewJourney;
    private View choiceStay;
    private int fragmentsRestored = 0;
    private static boolean hasResetThisRun = false;
    private Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private SoundPool soundPool;
    private int typingSoundId = -1;

    public RestoreEarthFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restore_earth, container, false);

        earthImage = view.findViewById(R.id.earthImage);
        earthContainer = view.findViewById(R.id.earthContainer);
        earthSpringReveal = view.findViewById(R.id.earthSpringReveal);
        earthSummerReveal = view.findViewById(R.id.earthSummerReveal);
        earthAutumnReveal = view.findViewById(R.id.earthAutumnReveal);
        earthWinterReveal = view.findViewById(R.id.earthWinterReveal);
        springIcon = view.findViewById(R.id.springIcon);
        summerIcon = view.findViewById(R.id.summerIcon);
        autumnIcon = view.findViewById(R.id.autumnIcon);
        winterIcon = view.findViewById(R.id.winterIcon);
        fragmentCounter = view.findViewById(R.id.fragmentCounter);
        titleText = view.findViewById(R.id.titleText);
        restoreEarthButton = view.findViewById(R.id.restoreEarthButton);
        kiboCharacter = view.findViewById(R.id.kiboCharacter);
        lumaCharacter = view.findViewById(R.id.lumaCharacter);
        kiboDialogueContainer = view.findViewById(R.id.kiboDialogueContainer);
        lumaDialogueContainer = view.findViewById(R.id.lumaDialogueContainer);
        kiboDialogueText = view.findViewById(R.id.kiboDialogueText);
        lumaDialogueText = view.findViewById(R.id.lumaDialogueText);
        finalDialogueContainer = view.findViewById(R.id.finalDialogueContainer);
        finalDialogueText = view.findViewById(R.id.finalDialogueText);
        finalChoicesContainer = view.findViewById(R.id.finalChoicesContainer);
        choiceRevisit = view.findViewById(R.id.choiceRevisit);
        choiceNewJourney = view.findViewById(R.id.choiceNewJourney);
        choiceStay = view.findViewById(R.id.choiceStay);

        resetProgressForNewRun();
        applyGrayscaleEffect(earthImage);
        setupQuadrantClips();
        initTypingSoundPool();

        // Initialize visuals from saved progress
        loadProgressFromPrefs();

        if (restoreEarthButton != null) {
            restoreEarthButton.setOnClickListener(v -> startRestoreAnimation());
            setupRestoreButtonStyle();
        }

        // Set up icon click listeners
        setupIconListeners();
        setupFinalChoiceActions();

        // Start white flash animation
        startWhiteFlash(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProgressFromPrefs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private void initTypingSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1, android.media.AudioManager.STREAM_MUSIC, 0);
        }
        try {
            typingSoundId = soundPool.load(getContext(), R.raw.sfx_typing, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playTypingSound() {
        if (soundPool != null && typingSoundId != -1) {
            soundPool.play(typingSoundId, 0.3f, 0.3f, 1, 0, 1.0f);
        }
    }

    private void startWhiteFlash(View parentView) {
        View flashView = new View(getContext());
        flashView.setBackgroundColor(0xFFFFFFFF);
        flashView.setAlpha(1f);
        ((ViewGroup) parentView).addView(flashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Wait for layout to be positioned before starting animations
        parentView.post(() -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(flashView, "alpha", 1f, 0f);
            fadeOut.setDuration(800);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Animate icons zooming in as fade starts
                    animateIconsZoomIn();
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    ((ViewGroup) parentView).removeView(flashView);
                }
            });
            fadeOut.start();
        });
    }

    private void animateIconsZoomIn() {
        // Get screen center
        View rootView = getView();
        if (rootView == null) return;
        
        int screenCenterX = rootView.getWidth() / 2;
        int screenCenterY = rootView.getHeight() / 2;
        
        animateIconFromCenter(springIcon, screenCenterX, screenCenterY, 0);
        animateIconFromCenter(summerIcon, screenCenterX, screenCenterY, 100);
        animateIconFromCenter(autumnIcon, screenCenterX, screenCenterY, 200);
        animateIconFromCenter(winterIcon, screenCenterX, screenCenterY, 300);
    }

    private void animateIconFromCenter(ImageView icon, int centerX, int centerY, long delay) {
        // Get the icon's final position from layout
        int[] location = new int[2];
        icon.getLocationInWindow(location);
        
        // Store final position before moving
        final float finalX = icon.getX();
        final float finalY = icon.getY();
        
        // Calculate center position relative to parent
        float startX = centerX - icon.getWidth() / 2f;
        float startY = centerY - icon.getHeight() / 2f;
        
        // Position icon at screen center initially
        icon.setX(startX);
        icon.setY(startY);
        icon.setScaleX(0f);
        icon.setScaleY(0f);
        
        // Animate position to final location
        ObjectAnimator moveX = ObjectAnimator.ofFloat(icon, "x", startX, finalX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(icon, "y", startY, finalY);
        
        // Animate scale
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0f, 1f);

        moveX.setDuration(400);
        moveX.setStartDelay(delay);
        moveX.setInterpolator(new AccelerateDecelerateInterpolator());
        
        moveY.setDuration(400);
        moveY.setStartDelay(delay);
        moveY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.setDuration(400);
        scaleX.setStartDelay(delay);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleY.setDuration(400);
        scaleY.setStartDelay(delay);
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        moveX.start();
        moveY.start();
        scaleX.start();
        scaleY.start();
    }

    private void setupIconListeners() {
        View.OnClickListener listener = v -> handleIconClick((ImageView) v);
        springIcon.setOnClickListener(listener);
        summerIcon.setOnClickListener(listener);
        autumnIcon.setOnClickListener(listener);
        winterIcon.setOnClickListener(listener);
    }

    private void handleIconClick(ImageView icon) {
        if (icon == summerIcon) {
            showIntroScene();
            return;
        }
        
        if (icon == autumnIcon) {
            goToAutumn();
            return;
        }

        if (icon == winterIcon) {
            goToWinter();
            return;
        }

        if (icon == springIcon) {
            goToSpring();
            return;
        }
    }

    private void showIntroScene() {
        SummerFragment summerPopup = new SummerFragment();
        summerPopup.show(getParentFragmentManager(), "SummerPopup");
    }
    
    private void goToAutumn() {
        if (getActivity() != null) {
            AutumnFragment autumnFragment = new AutumnFragment();
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, autumnFragment);
            transaction.commit();
        }
    }

    private void goToWinter() {
        if (getActivity() != null) {
            WinterFragment winterFragment = new WinterFragment();
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, winterFragment);
            transaction.commit();
        }
    }

    private void goToSpring() {
        if (getActivity() != null) {
            MinigameFragment minigameFragment = new MinigameFragment();
            Bundle args = new Bundle();
            args.putString("season", "Spring");
            minigameFragment.setArguments(args);
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, minigameFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void incrementFragmentCounter() {
        fragmentsRestored++;
        updateCounter();

        if (fragmentsRestored == 4) {
            // All fragments restored - navigate to next scene
            goToNextScene();
        }
    }

    private void updateCounter() {
        fragmentCounter.setText(fragmentsRestored + "/4 FRAGMENTS RESTORED");
    }

    private void goToNextScene() {
        // Show characters and dialogue first
        showPostRestorationDialogue();
    }

    private void showPostRestorationDialogue() {
        // Show and fade in characters and dialogue simultaneously
        if (kiboCharacter != null) {
            kiboCharacter.setVisibility(View.VISIBLE);
            kiboCharacter.setAlpha(0f);
            kiboCharacter.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .withEndAction(() -> startFloatingAnimation(kiboCharacter, true))
                    .start();
        }

        // Show and fade in KIBO's dialogue at the same time as KIBO
        if (kiboDialogueContainer != null) {
            kiboDialogueContainer.setVisibility(View.VISIBLE);
            kiboDialogueContainer.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .withEndAction(() -> {
                        // Start typing KIBO's dialogue
                        typeKiboDialogue("IT LOOKS BETTER... CALMER.");
                        // Auto-advance to LUMA's dialogue after KIBO's text finishes + delay
                        typingHandler.postDelayed(() -> showLumaDialogue(), 1800);
                    })
                    .start();
        }
    }

    private void showLumaDialogue() {
        if (lumaCharacter != null) {
            lumaCharacter.setVisibility(View.VISIBLE);
            lumaCharacter.setAlpha(0f);
            lumaCharacter.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .withEndAction(() -> startFloatingAnimation(lumaCharacter, false))
                    .start();
        }

        if (lumaDialogueContainer != null) {
            lumaDialogueContainer.setVisibility(View.VISIBLE);
            lumaDialogueContainer.setAlpha(0f);
            lumaDialogueContainer.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .withEndAction(() -> {
                        typeLumaDialogue("BECAUSE YOU GAVE IT SPACE TO HEAL.");
                        // Wait for typing to finish, then wait 3 seconds before fading out
                        typingHandler.postDelayed(() -> fadeOutDialoguesAndTransition(), 2200 + 3000);
                    })
                    .start();
        }
    }

    private void fadeOutDialoguesAndTransition() {
        // Fade out all dialogues and characters simultaneously
        long fadeDuration = 1000;

        if (kiboCharacter != null) {
            kiboCharacter.animate()
                    .alpha(0f)
                    .setDuration(fadeDuration)
                    .start();
        }

        if (lumaCharacter != null) {
            lumaCharacter.animate()
                    .alpha(0f)
                    .setDuration(fadeDuration)
                    .start();
        }

        if (kiboDialogueContainer != null) {
            kiboDialogueContainer.animate()
                    .alpha(0f)
                    .setDuration(fadeDuration)
                    .start();
        }

        if (lumaDialogueContainer != null) {
            lumaDialogueContainer.animate()
                    .alpha(0f)
                    .setDuration(fadeDuration)
                    .withEndAction(() -> {
                        // After fade out completes, move earth higher
                        moveEarthHigherAndShowChoices();
                    })
                    .start();
        } else {
            // Fallback if dialogue container is null
            moveEarthHigherAndShowChoices();
        }
    }

    private void moveEarthHigherAndShowChoices() {
        if (earthContainer == null) return;

        // Get current translation and move up an additional amount
        float currentTranslation = earthContainer.getTranslationY();
        ObjectAnimator moveUp = ObjectAnimator.ofFloat(earthContainer, "translationY", 
                currentTranslation, currentTranslation - 220f);
        moveUp.setDuration(1200);
        moveUp.setInterpolator(new AccelerateDecelerateInterpolator());
        moveUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // After earth moves higher, show final dialogue and choices
                showFinalDialogueAndChoices();
            }
        });
        moveUp.start();
    }

    private void typeKiboDialogue(String text) {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        if (kiboDialogueText == null) return;
        kiboDialogueText.setText("");
        final int[] index = {0};
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= text.length()) {
                    kiboDialogueText.setText(text.substring(0, index[0]));
                    playTypingSound();
                    index[0]++;
                    typingHandler.postDelayed(this, 50);
                }
            }
        };
        typingHandler.post(typingRunnable);
    }

    private void typeLumaDialogue(String text) {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        if (lumaDialogueText == null) return;
        lumaDialogueText.setText("");
        final int[] index = {0};
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= text.length()) {
                    lumaDialogueText.setText(text.substring(0, index[0]));
                    playTypingSound();
                    index[0]++;
                    typingHandler.postDelayed(this, 50);
                }
            }
        };
        typingHandler.post(typingRunnable);
    }


    private void startFloatingAnimation(View view, boolean isKibo) {
        float startY = isKibo ? 0f : -20f;
        float endY = isKibo ? -20f : 0f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, startY, endY);
        animator.setDuration(1500);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void showFinalDialogueAndChoices() {
        if (finalDialogueContainer != null) {
            finalDialogueContainer.setVisibility(View.VISIBLE);
            finalDialogueContainer.setAlpha(0f);
            finalDialogueContainer.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .withEndAction(() -> typeNarratorDialogue("YOU ARE NOT BROKEN. YOU ARE JUST RESETTING."))
                    .start();
        }

        if (finalChoicesContainer != null) {
            finalChoicesContainer.setVisibility(View.VISIBLE);
            finalChoicesContainer.setAlpha(1f);
        }

        showFinalChoice(choiceRevisit, 200);
        showFinalChoice(choiceNewJourney, 350);
        showFinalChoice(choiceStay, 500);
    }

    private void showFinalChoice(@Nullable View choice, long delay) {
        if (choice == null) return;
        choice.setVisibility(View.VISIBLE);
        choice.setAlpha(0f);
        choice.animate()
                .alpha(1f)
                .setDuration(700)
                .setStartDelay(delay)
                .start();
        startChoiceFloat(choice, delay);
    }

    private void startChoiceFloat(View view, long delay) {
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(view, "translationY", -4f, 4f);
        floatAnim.setDuration(2200);
        floatAnim.setStartDelay(delay);
        floatAnim.setRepeatMode(ValueAnimator.REVERSE);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.start();
    }

    private void setupFinalChoiceActions() {
        if (choiceRevisit != null) choiceRevisit.setOnClickListener(v -> goToRestoreEarth());
        if (choiceNewJourney != null) choiceNewJourney.setOnClickListener(v -> goToAppStart());
        if (choiceStay != null) choiceStay.setOnClickListener(v -> {});  // Do nothing
    }

    private void goToRestoreEarth() {
        if (getActivity() != null) {
            RestoreEarthFragment restoreEarthFragment = new RestoreEarthFragment();
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, restoreEarthFragment);
            transaction.commit();
        }
    }

    private void goToAppStart() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void goToEarthScene() {
        if (getActivity() != null) {
            EarthSceneFragment earthSceneFragment = new EarthSceneFragment();
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, earthSceneFragment);
            transaction.commit();
        }
    }

    private void typeNarratorDialogue(String text) {
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }

        if (finalDialogueText == null) return;
        finalDialogueText.setText("");
        final int[] index = {0};
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= text.length()) {
                    finalDialogueText.setText(text.substring(0, index[0]));
                    playTypingSound();
                    index[0]++;
                    typingHandler.postDelayed(this, 50);
                }
            }
        };
        typingHandler.post(typingRunnable);
    }

    private void navigateToChoicesScene() {
        if (getActivity() != null) {
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, new ChoicesSceneFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void loadProgressFromPrefs() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = 0;
        if (prefs.getBoolean(KEY_SPRING, false)) count++;
        if (prefs.getBoolean(KEY_SUMMER, false)) count++;
        if (prefs.getBoolean(KEY_AUTUMN, false)) count++;
        if (prefs.getBoolean(KEY_WINTER, false)) count++;
        fragmentsRestored = count;
        updateCounter();
        updateEarthQuadrants(prefs);
        updateRestoreButtonVisibility();
    }

    private void applyGrayscaleEffect(ImageView imageView) {
        ColorMatrix saturation = new ColorMatrix();
        saturation.setSaturation(0f);
        ColorMatrix darken = new ColorMatrix(new float[] {
                0.75f, 0f, 0f, 0f, 0f,
                0f, 0.75f, 0f, 0f, 0f,
                0f, 0f, 0.75f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        });
        saturation.postConcat(darken);
        imageView.setColorFilter(new ColorMatrixColorFilter(saturation));
        imageView.setAlpha(1f);
    }

    private void setupQuadrantClips() {
        if (earthImage == null) return;
        earthImage.post(() -> {
            int width = earthImage.getWidth();
            int height = earthImage.getHeight();
            if (width == 0 || height == 0) return;
            Rect topLeft = new Rect(0, 0, width / 2, height / 2);
            Rect topRight = new Rect(width / 2, 0, width, height / 2);
            Rect bottomLeft = new Rect(0, height / 2, width / 2, height);
            Rect bottomRight = new Rect(width / 2, height / 2, width, height);

            if (earthSpringReveal != null) earthSpringReveal.setClipBounds(topLeft);
            if (earthSummerReveal != null) earthSummerReveal.setClipBounds(topRight);
            if (earthAutumnReveal != null) earthAutumnReveal.setClipBounds(bottomLeft);
            if (earthWinterReveal != null) earthWinterReveal.setClipBounds(bottomRight);
        });
    }

    private void updateEarthQuadrants(SharedPreferences prefs) {
        boolean spring = prefs.getBoolean(KEY_SPRING, false);
        boolean summer = prefs.getBoolean(KEY_SUMMER, false);
        boolean autumn = prefs.getBoolean(KEY_AUTUMN, false);
        boolean winter = prefs.getBoolean(KEY_WINTER, false);

        if (earthSpringReveal != null) earthSpringReveal.setVisibility(spring ? View.VISIBLE : View.INVISIBLE);
        if (earthSummerReveal != null) earthSummerReveal.setVisibility(summer ? View.VISIBLE : View.INVISIBLE);
        if (earthAutumnReveal != null) earthAutumnReveal.setVisibility(autumn ? View.VISIBLE : View.INVISIBLE);
        if (earthWinterReveal != null) earthWinterReveal.setVisibility(winter ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateRestoreButtonVisibility() {
        if (restoreEarthButton == null) return;
        restoreEarthButton.setVisibility(fragmentsRestored == 4 ? View.VISIBLE : View.GONE);
    }

    private void setupRestoreButtonStyle() {
        if (restoreEarthButton == null) return;
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF35C9FF, 0xFF6B3DFF}
        );
        gradient.setCornerRadius(12f * density);
        gradient.setStroke(Math.round(3f * density), 0xFF000000);
        restoreEarthButton.setBackground(gradient);
        restoreEarthButton.setBackgroundTintList(null);
    }

    private void startRestoreAnimation() {
        View rootView = getView();
        if (rootView == null || getContext() == null) return;

        // Hide all UI elements immediately
        if (titleText != null) titleText.setVisibility(View.GONE);
        if (springIcon != null) springIcon.setVisibility(View.GONE);
        if (summerIcon != null) summerIcon.setVisibility(View.GONE);
        if (autumnIcon != null) autumnIcon.setVisibility(View.GONE);
        if (winterIcon != null) winterIcon.setVisibility(View.GONE);
        if (fragmentCounter != null) fragmentCounter.setVisibility(View.GONE);
        if (restoreEarthButton != null) restoreEarthButton.setVisibility(View.GONE);

        // Create white flash overlay
        View whiteFlash = new View(getContext());
        whiteFlash.setBackgroundColor(0xFFFFFFFF);
        whiteFlash.setAlpha(0f);
        ((ViewGroup) rootView).addView(whiteFlash, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Fade in white flash
        whiteFlash.animate()
                .alpha(1f)
                .setDuration(800)
                .withEndAction(() -> {
                    // Hold white for 2 seconds, then fade out and start restore sequence
                    whiteFlash.postDelayed(() -> {
                        whiteFlash.animate()
                                .alpha(0f)
                                .setDuration(500)
                                .withEndAction(() -> {
                                    ((ViewGroup) rootView).removeView(whiteFlash);
                                    startEarthRestoreSequence();
                                })
                                .start();
                    }, 2000);
                })
                .start();
    }

    private void startEarthRestoreSequence() {
        // Hide reveal layers
        earthSpringReveal.setVisibility(View.GONE);
        earthSummerReveal.setVisibility(View.GONE);
        earthAutumnReveal.setVisibility(View.GONE);
        earthWinterReveal.setVisibility(View.GONE);

        // Remove grayscale and show full color earth_seasons
        earthImage.setColorFilter(null);
        earthImage.setAlpha(1f);
        earthImage.setImageResource(R.drawable.earth_seasons);

        // Start shake animation (4000ms)
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(earthImage, "translationX", 
                0f, -25f, 25f, -20f, 20f, -15f, 15f, -10f, 10f, -5f, 5f, 0f);
        shakeX.setDuration(4000);
        shakeX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startCounterclockwiseRotation();
            }
        });
        shakeX.start();

        // Fade and change to cracked earth at 2.5 seconds (during shake, not after)
        earthImage.postDelayed(() -> {
            earthImage.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        earthImage.setImageResource(R.drawable.earth_restore_crack);
                        earthImage.animate()
                                .alpha(1f)
                                .setDuration(250)
                                .start();
                    })
                    .start();
        }, 2500);
    }

    private void startCounterclockwiseRotation() {
        // Counterclockwise rotation (negative degrees) - 4000ms
        ObjectAnimator rotate = ObjectAnimator.ofFloat(earthImage, "rotation", 0f, -360f);
        rotate.setDuration(4000);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        rotate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                earthImage.setRotation(0f);
                // Wait to appreciate the restored earth, then hover upward
                earthImage.postDelayed(() -> startEarthHoverUpward(), 800);
            }
        });
        rotate.start();

        // Fade and change to final earth at 2.5 seconds (during rotation, not after)
        earthImage.postDelayed(() -> {
            earthImage.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        earthImage.setImageResource(R.drawable.earth);
                        earthImage.animate()
                                .alpha(1f)
                                .setDuration(250)
                                .start();
                    })
                    .start();
        }, 2500);
    }

    private void startEarthHoverUpward() {
        // Animate the entire earth container upward (includes earth image and all overlays)
        if (earthContainer == null) return;
        
        ObjectAnimator hoverUp = ObjectAnimator.ofFloat(earthContainer, "translationY", 0f, -220f);
        hoverUp.setDuration(1800);
        hoverUp.setInterpolator(new AccelerateDecelerateInterpolator());
        hoverUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // After hovering up, show dialogue
                goToNextScene();
            }
        });
        hoverUp.start();
    }

    private void resetProgressForNewRun() {
        if (getContext() == null) return;
        if (hasResetThisRun) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        hasResetThisRun = true;
    }

    public static void markSeasonRestored(@Nullable Context context, @NonNull String season) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = null;
        if ("Spring".equalsIgnoreCase(season)) key = KEY_SPRING;
        if ("Summer".equalsIgnoreCase(season)) key = KEY_SUMMER;
        if ("Autumn".equalsIgnoreCase(season)) key = KEY_AUTUMN;
        if ("Winter".equalsIgnoreCase(season)) key = KEY_WINTER;
        if (key == null || prefs.getBoolean(key, false)) return;
        boolean spring = KEY_SPRING.equals(key) || prefs.getBoolean(KEY_SPRING, false);
        boolean summer = KEY_SUMMER.equals(key) || prefs.getBoolean(KEY_SUMMER, false);
        boolean autumn = KEY_AUTUMN.equals(key) || prefs.getBoolean(KEY_AUTUMN, false);
        boolean winter = KEY_WINTER.equals(key) || prefs.getBoolean(KEY_WINTER, false);
        int count = 0;
        if (spring) count++;
        if (summer) count++;
        if (autumn) count++;
        if (winter) count++;
        SharedPreferences.Editor editor = prefs.edit().putBoolean(key, true);
        if (count == 4) {
            editor.putBoolean(KEY_PENDING_COMPLETE, true);
        }
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
    }
}
