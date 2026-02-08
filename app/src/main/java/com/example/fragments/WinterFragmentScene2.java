package com.example.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WinterFragmentScene2 extends Fragment {

    public static final String SELECTED_BUTTON_ID = "selected_button_id";
    private View lastSelectedButton = null;
    private int selectedButtonId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates the layout for the "1 MORE CHANCE" screen
        View view = inflater.inflate(R.layout.fragment_winter_scene2, container, false);

        if (getArguments() != null) {
            selectedButtonId = getArguments().getInt(SELECTED_BUTTON_ID);
        }

        // Path buttons
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

        View.OnClickListener pathListener = v -> {
            if (lastSelectedButton != null) {
                lastSelectedButton.setActivated(false);
            }
            v.setActivated(true);
            lastSelectedButton = v;
            navigateToFinalScene(v.getId());
        };

        quiet.setOnClickListener(pathListener);
        gentle.setOnClickListener(pathListener);
        peaceful.setOnClickListener(pathListener);
        calm.setOnClickListener(pathListener);

        return view;
    }

    private void navigateToFinalScene(int selectedButtonId) {
        // Transition to the final panel: WinterFragmentScene3
        WinterFragmentScene3 finalScene = new WinterFragmentScene3();
        Bundle args = new Bundle();
        args.putInt(SELECTED_BUTTON_ID, selectedButtonId);
        finalScene.setArguments(args);

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, finalScene)
                    .commit();
        }
    }
}