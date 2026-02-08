package com.example.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WinterFragmentScene3 extends Fragment {

    public static final String SELECTED_BUTTON_ID = "selected_button_id";
    private int selectedButtonId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates the layout that says "PATH FOUND."
        View view = inflater.inflate(R.layout.fragment_winter_scene3, container, false);

        if (getArguments() != null) {
            selectedButtonId = getArguments().getInt(SELECTED_BUTTON_ID);
        }

        if (selectedButtonId != -1) {
            View selectedButton = view.findViewById(selectedButtonId);
            if (selectedButton != null) {
                selectedButton.setActivated(true);
            }
        }

        // This Handler waits 3000ms (3 seconds) and then runs the transition
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) { // Safety check to ensure fragment is still active
                navigateToRestored();
            }
        }, 3000);

        return view;
    }

    private void navigateToRestored() {
        WinterFragmentRestored finalFragment = new WinterFragmentRestored();
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, finalFragment)
                    .commit();
        }
    }
}