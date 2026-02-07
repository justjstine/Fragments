package com.example.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {

    private Handler handler;
    private Runnable typingRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        handler = new Handler(Looper.getMainLooper());

        ImageView character1 = view.findViewById(R.id.character1);
        TextView dialogue = view.findViewById(R.id.dialogue);
        Button continueBtn = view.findViewById(R.id.btnContinue);

        if (character1 != null) {
            startFloatingAnimation(character1);
        }

        typeText(dialogue, "Choose your path...", () -> {
            continueBtn.setVisibility(View.VISIBLE);
            continueBtn.animate().alpha(1f).setDuration(300).start();
        });

        continueBtn.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new BloomScene())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void startFloatingAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, -20f, 0f);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    private void typeText(TextView textView, String text, Runnable onComplete) {
        if (typingRunnable != null) {
            handler.removeCallbacks(typingRunnable);
        }

        textView.setText("");
        int[] index = {0};

        typingRunnable = () -> {
            if (index[0] <= text.length()) {
                textView.setText(text.substring(0, index[0]));
                index[0]++;
                handler.postDelayed(typingRunnable, 50);
            } else if (onComplete != null) {
                onComplete.run();
            }
        };

        handler.post(typingRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (typingRunnable != null) {
            handler.removeCallbacks(typingRunnable);
        }
    }
}
