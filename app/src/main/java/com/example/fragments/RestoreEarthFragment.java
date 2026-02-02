package com.example.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RestoreEarthFragment extends Fragment {

    private ImageView earthImage;
    private ImageView springIcon;
    private ImageView summerIcon;
    private ImageView autumnIcon;
    private ImageView winterIcon;
    private TextView fragmentCounter;
    private int fragmentsRestored = 0;

    public RestoreEarthFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restore_earth, container, false);

        earthImage = view.findViewById(R.id.earthImage);
        springIcon = view.findViewById(R.id.springIcon);
        summerIcon = view.findViewById(R.id.summerIcon);
        autumnIcon = view.findViewById(R.id.autumnIcon);
        winterIcon = view.findViewById(R.id.winterIcon);
        fragmentCounter = view.findViewById(R.id.fragmentCounter);

        // Apply grayscale to earth
        applyGrayscaleEffect(earthImage);

        // Set initial counter
        updateCounter();

        // Set up icon click listeners
        setupIconListeners();

        // Start white flash animation
        startWhiteFlash(view);

        return view;
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

        moveX.setDuration(600);
        moveX.setStartDelay(delay);
        moveX.setInterpolator(new AccelerateDecelerateInterpolator());
        
        moveY.setDuration(600);
        moveY.setStartDelay(delay);
        moveY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.setDuration(600);
        scaleX.setStartDelay(delay);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleY.setDuration(600);
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

        String season = "";
        if (icon == springIcon) {
            season = "SPRING";
        } else if (icon == autumnIcon) {
            season = "AUTUMN";
        } else if (icon == winterIcon) {
            season = "WINTER";
        }

        // Navigate to minigame with season parameter
        if (getActivity() != null) {
            MinigameFragment minigameFragment = new MinigameFragment();
            Bundle args = new Bundle();
            args.putString("season", season);
            minigameFragment.setArguments(args);

            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, minigameFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void showIntroScene() {
        SummerFragment summerPopup = new SummerFragment();
        summerPopup.show(getParentFragmentManager(), "SummerPopup");
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
        // Navigate to next scene
        if (getActivity() != null) {
            androidx.fragment.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, new ChoicesSceneFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void applyGrayscaleEffect(ImageView imageView) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        imageView.setColorFilter(new ColorMatrixColorFilter(matrix));
        imageView.setAlpha(0.6f);
    }
}
