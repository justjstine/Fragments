package com.example.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WinterFragmentScene1 extends Fragment {

    public static final String SELECTED_BUTTON_ID = "selected_button_id";
    private View lastSelectedButton = null;
    private int selectedButtonId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates the layout for the "2 MORE CHANCES" screen
        View view = inflater.inflate(R.layout.fragment_winter_scene1, container, false);

        if (getArguments() != null) {
            selectedButtonId = getArguments().getInt(SELECTED_BUTTON_ID);
        }

        // Find the path buttons
        View quiet = view.findViewById(R.id.btnQuietPath);
        View gentle = view.findViewById(R.id.btnGentlePath);
        View peaceful = view.findViewById(R.id.btnPeacefulPath);
        View calm = view.findViewById(R.id.btnCalmPath);

        if (selectedButtonId != -1) {
            View selectedButton = view.findViewById(selectedButtonId);
            if (selectedButton != null) {
                selectedButton.setActivated(true);
                lastSelectedButton = selectedButton;
            }
        }

        // Listener to trigger transition to Scene 2
        View.OnClickListener pathListener = v -> {
            if (lastSelectedButton != null) {
                lastSelectedButton.setActivated(false);
            }
            v.setActivated(true);
            lastSelectedButton = v;
            navigateToNextScene(v.getId());
        };

        quiet.setOnClickListener(pathListener);
        gentle.setOnClickListener(pathListener);
        peaceful.setOnClickListener(pathListener);
        calm.setOnClickListener(pathListener);

        return view;
    }

    private void navigateToNextScene(int selectedButtonId) {
        // Create the next fragment in the sequence
        WinterFragmentScene2 nextScene = new WinterFragmentScene2();
        Bundle args = new Bundle();
        args.putInt(SELECTED_BUTTON_ID, selectedButtonId);
        nextScene.setArguments(args);

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, nextScene)
                    .commit();
        }
    }
}